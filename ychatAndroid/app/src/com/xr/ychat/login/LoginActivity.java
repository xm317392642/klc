package com.xr.ychat.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.microquation.linkedme.android.LinkedME;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.UnsentRedPacketCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.string.StringTextWatcher;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.team.TeamService;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tendcloud.tenddata.TCAgent;
import com.tendcloud.tenddata.TDAccount;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.common.ui.XEditText;
import com.xr.ychat.common.util.WxShareUtils;
import com.xr.ychat.config.preference.UserPreferences;
import com.xr.ychat.main.activity.MainActivity;
import com.xr.ychat.main.activity.MainShareActivity;

/**
 * 登录/注册界面
 *  feature_z
 */
public class LoginActivity extends UI implements OnKeyListener {

    private static final String KICK_OUT = "KICK_OUT";
    private final int BASIC_PERMISSION_REQUEST_CODE = 110;

    private TextView switchModeBtn;  // 注册/登录切换按钮
    private TextView userAgreement;

    private XEditText loginAccountEdit;
    private XEditText loginPasswordEdit;
    private TextView loginForgetPassword;

    private XEditText registerAccountEdit;
    private XEditText registerPasswordEdit;
    private TextView registerSendCode;

    private View loginLayout;
    private View registerLayout;
    private Button commit;

    private AbortableFuture<LoginInfo> loginRequest;
    private boolean registerMode = false; // 注册模式
    private boolean registerPanelInited = false; // 注册面板是否初始化
    private TimeCount timeCount;

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
        //目前暂时切换到  微信授权登录
//        LoginAuthorizeActivity.start(context);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setActivityView(R.layout.activity_login);

        requestBasicPermission();

        onParseIntent();
        initRightTopBtn();
        setupLoginPanel();
        setupRegisterPanel();
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private void requestBasicPermission() {
        MPermission.with(LoginActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {

    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {

    }

    private void onParseIntent() {
        if (!getIntent().getBooleanExtra(KICK_OUT, false)) {
            return;
        }
        int type = NIMClient.getService(AuthService.class).getKickedClientType();
        String client;
        switch (type) {
            case ClientType.Web:
                client = "网页端";
                break;
            case ClientType.Windows:
            case ClientType.MAC:
                client = "电脑端";
                break;
            case ClientType.REST:
                client = "服务端";
                break;
            default:
                client = "移动端";
                break;
        }
        EasyAlertDialogHelper.showOneButtonDiolag(LoginActivity.this,
                getString(R.string.kickout_notify),
                String.format(getString(R.string.kickout_content), client),
                getString(R.string.ok),
                true,
                null);

    }

    /**
     * ActionBar 右上角按钮
     */
    private void initRightTopBtn() {
        commit = (Button) findViewById(R.id.login_commit);
        commit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (registerMode) {
                    register();
                } else {
                    fakeLogin();
                }
            }
        });
        userAgreement = (TextView) findViewById(R.id.login_user_agreement);
        userAgreement.setOnClickListener(v -> startActivity(new Intent(this, UserProtocolActivity.class)));
        SpannableString spannableString = new SpannableString("点击登录即表示阅读并同意《用户使用协议》");
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#BE6913")), 12, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        userAgreement.setText(spannableString);
        //微信登录 send oauth request
        findView(R.id.wechat_login).setOnClickListener(v -> {
            IWXAPI wxapi = WXAPIFactory.createWXAPI(this, WxShareUtils.APP_ID);
            if (wxapi == null || !wxapi.isWXAppInstalled()) {
                YchatToastUtils.showShort("您没有安装微信");
                return;
            }
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";//如获取用户个人信息则填写snsapi_userinfo
            req.state = "com.xr.ychat";
            wxapi.sendReq(req);
        });
    }

    /**
     * 登录面板
     */
    private void setupLoginPanel() {
        loginAccountEdit = findView(R.id.edit_login_account);
        loginPasswordEdit = findView(R.id.edit_login_password);

        loginAccountEdit.addTextChangedListener(new StringTextWatcher(11, loginAccountEdit));
        loginAccountEdit.setOnKeyListener(this);
        loginPasswordEdit.addTextChangedListener(new StringTextWatcher(18, loginPasswordEdit));
        loginPasswordEdit.setOnKeyListener(this);

        loginForgetPassword = findView(R.id.edit_login_forget);
        loginForgetPassword.setOnClickListener(v -> {
            ResetPasswordActivity.start(LoginActivity.this, false);
        });
    }

    /**
     * 注册面板
     */
    private void setupRegisterPanel() {
        loginLayout = findView(R.id.login_layout);
        registerLayout = findView(R.id.register_layout);
        switchModeBtn = findView(R.id.login_type);
        switchModeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode();
            }
        });
    }

    private void initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
        // 加载状态栏配置
        StatusBarNotificationConfig statusBarNotificationConfig = UserPreferences.getStatusConfig();
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.getNotificationConfig();
            UserPreferences.setStatusConfig(statusBarNotificationConfig);
        }
        // 更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig);
    }

    private void onLoginDone() {
        loginRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    private void saveLoginInfo(final String account, final String token, final String phone) {
        Preferences.saveUserAccount(this, account);
        Preferences.saveUserToken(this, token);
        Preferences.saveUserPhone(this, phone);
    }

    private String tokenFromPassword(String password) {
        return MD5.getStringMD5(password);
    }

    /**
     * ***************************************** 验证码登录 **************************************
     */
    private void register() {
        if (!registerMode || !registerPanelInited) {
            return;
        }
        if (!checkRegisterContentValid()) {
            return;
        }

        if (!NetworkUtil.isNetAvailable(LoginActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }

        DialogMaker.showProgressDialog(this, getString(R.string.logining), false);

        // 注册流程
        final String phone = registerAccountEdit.getText().toString();
        final String password = registerPasswordEdit.getText().toString();

        ContactHttpClient.getInstance().register(phone, password, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(aVoid.getAccid(), aVoid.getToken()));
                loginRequest.setCallback(new RequestCallbackWrapper() {
                    @Override
                    public void onResult(int code, Object result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            TCAgent.onLogin(aVoid.getAccid(), TDAccount.AccountType.WEIXIN, aVoid.getUid());
                            loginRequest = null;
                            NimUIKit.loginSuccess(aVoid.getAccid());
                            SPUtils.getInstance().remove(CommonUtil.ALIPAYUID);
                            SPUtils.getInstance().remove(CommonUtil.YCHAT_ACCOUNT);
                            SPUtils.getInstance().remove(UnsentRedPacketCache.TAG);
                            DemoCache.setAccount(aVoid.getAccid());
                            Preferences.saveWeiranToken(LoginActivity.this, aVoid.getMytoken());
                            Preferences.saveWeiranUid(LoginActivity.this, aVoid.getUid());
                            saveLoginInfo(aVoid.getAccid(), aVoid.getToken(), phone);
                            // 初始化消息提醒配置
                            initNotificationConfig();
                            dealAutoAddEvent(aVoid.getAccid());
                            // 进入主界面
                            jumpToMainOrShare();
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                if (code == 100001) {
                    YchatToastUtils.showShort("请求缺少参数");
                } else if (code == 100006) {
                    YchatToastUtils.showShort("验证码错误");
                } else if (code == 100009) {
                    YchatToastUtils.showShort("手机号不对");
                } else if (code == 100008) {
                    YchatToastUtils.showShort("手机号没有在平台绑定账户");
                    DownloadTipsActivity.start(LoginActivity.this);
                } else if (code == 100036) {
                    YchatToastUtils.showToastLong("该手机号未注册,微信登录绑定手机号即可");
                } else if (code == 100030) {
                    YchatToastUtils.showToastLong("网络繁忙，请重试");
                } else {
                    YchatToastUtils.showShort("注册失败");
                }
            }
        });
    }

    private boolean checkRegisterContentValid() {
        if (!registerMode || !registerPanelInited) {
            return false;
        }
        String account = registerAccountEdit.getEditableText().toString().trim();
        String token = registerPasswordEdit.getEditableText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            YchatToastUtils.showShort("请输入手机号码");
            return false;
        }
        if (!RegexUtils.isMobileExact(account)) {
            YchatToastUtils.showShort("请输入正确的手机号码");
            return false;
        }
        if (TextUtils.isEmpty(token)) {
            YchatToastUtils.showShort("请输入验证码");
            return false;
        }
        return true;
    }

    /**
     * ***************************************** 注册/登录切换 **************************************
     */
    private void switchMode() {
        registerMode = !registerMode;

        if (registerMode && !registerPanelInited) {
            registerAccountEdit = findView(R.id.edit_register_account);
            registerPasswordEdit = findView(R.id.edit_register_password);

            registerAccountEdit.addTextChangedListener(new StringTextWatcher(11, registerAccountEdit));
            registerAccountEdit.setOnKeyListener(this);
            registerPasswordEdit.addTextChangedListener(new StringTextWatcher(6, registerPasswordEdit));
            registerPasswordEdit.setOnKeyListener(this);

            registerSendCode = findView(R.id.edit_login_verification_code);
            registerSendCode.setOnClickListener(v -> {
                sendVerfyCode();
            });

            registerPanelInited = true;
        }

        loginLayout.setVisibility(registerMode ? View.GONE : View.VISIBLE);
        registerLayout.setVisibility(registerMode ? View.VISIBLE : View.GONE);
        switchModeBtn.setText(registerMode ? "密码登录" : "验证码登录");
    }

    /**
     * 跳转到主界面或者分享界面
     */
    public void jumpToMainOrShare() {
        if (com.netease.nim.uikit.impl.preference.UserPreferences.getShare()) {
            MainShareActivity.start(LoginActivity.this, null);
        } else {
            MainActivity.start(LoginActivity.this, null);
        }
        finish();
    }

    /**
     * 密码登录
     */
    private void fakeLogin() {
        String account = loginAccountEdit.getEditableText().toString().trim();
        String token = loginPasswordEdit.getEditableText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            YchatToastUtils.showShort("请输入手机号码");
            return;
        }
        if (!RegexUtils.isMobileExact(account)) {
            YchatToastUtils.showShort("请输入正确的手机号码");
            return;
        }
        if (TextUtils.isEmpty(token)) {
            YchatToastUtils.showShort("请输入登录密码");
            return;
        }
        if (!NetworkUtil.isNetAvailable(LoginActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, getString(R.string.logining), false);
        ContactHttpClient.getInstance().login(account, tokenFromPassword(token), new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(aVoid.getAccid(), aVoid.getToken()));
                loginRequest.setCallback(new RequestCallbackWrapper() {
                    @Override
                    public void onResult(int code, Object result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            TCAgent.onLogin(aVoid.getAccid(), TDAccount.AccountType.WEIXIN, aVoid.getUid());
                            loginRequest = null;
                            NimUIKit.loginSuccess(aVoid.getAccid());
                            SPUtils.getInstance().remove(CommonUtil.ALIPAYUID);
                            SPUtils.getInstance().remove(CommonUtil.YCHAT_ACCOUNT);
                            SPUtils.getInstance().remove(UnsentRedPacketCache.TAG);
                            DemoCache.setAccount(aVoid.getAccid());
                            Preferences.saveWeiranToken(LoginActivity.this, aVoid.getMytoken());
                            Preferences.saveWeiranUid(LoginActivity.this, aVoid.getUid());
                            saveLoginInfo(aVoid.getAccid(), aVoid.getToken(), account);
                            // 初始化消息提醒配置
                            initNotificationConfig();
                            dealAutoAddEvent(aVoid.getAccid());
                            // 进入主界面
                            jumpToMainOrShare();
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("账号或密码错误");
            }
        });

    }

    /**
     * 获取验证码
     */
    private void sendVerfyCode() {
        String account = registerAccountEdit.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            YchatToastUtils.showShort("请输入手机号码");
            return;
        }
        if (!RegexUtils.isMobileExact(account)) {
            YchatToastUtils.showShort("请输入正确的手机号码");
            return;
        }
        if (!NetworkUtil.isNetAvailable(LoginActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "正在获取验证码", false);
        startCountdown();
        ContactHttpClient.getInstance().sendVerifyCode(account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("验证码获取成功");
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                if (code == 100001) {
                    YchatToastUtils.showShort("请求缺少参数");
                } else if (code == 100009) {
                    YchatToastUtils.showShort("手机号不对");
                } else if (code == 100008) {
                    YchatToastUtils.showShort("手机号没有在平台绑定账户");
                    DownloadTipsActivity.start(LoginActivity.this);
                } else {
                    YchatToastUtils.showShort("验证码获取失败");
                }
            }
        });
    }

    public void startCountdown() {
        if (timeCount != null) {
            timeCount.cancel();
            timeCount = null;
        }
        timeCount = new TimeCount(60000, 1000);
        timeCount.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeCount != null) {
            timeCount.cancel();
            timeCount = null;
        }
        if (ActivityUtils.getActivityList().size() == 0) {
            CommonUtil.setCancelValue(false);
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            runOnUiThread(() -> {
                registerSendCode.setEnabled(true);
                registerSendCode.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.color_be6913));
                registerSendCode.setText("获取验证码");
            });
        }

        @Override
        public void onTick(long millisUntilFinished) {
            runOnUiThread(() -> {
                registerSendCode.setEnabled(false);
                registerSendCode.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.color_a9a9a9));
                registerSendCode.setText("重新获取" + (millisUntilFinished + 1000) / 1000 + "s");
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinkedME.getInstance().setImmediate(true);
    }

    private void dealAutoAddEvent(String account) {
        int type = SPUtils.getInstance().getInt(CommonUtil.AUTO_ADD_TYPE, 0);
        if (type == 1) {
            String accid = SPUtils.getInstance().getString(CommonUtil.AUTO_ADD_VALUE);
            ContactHttpClient.getInstance().querySearching(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), 3, accid, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo aVoid) {
                    if (!TextUtils.isEmpty(aVoid.getAccid()) && aVoid.getAccid().length() > 1) {
                        if (!NIMClient.getService(FriendService.class).isMyFriend(aVoid.getAccid())) {
                            NIMClient.getService(FriendService.class).addFriend(new AddFriendData(aVoid.getAccid(), VerifyType.VERIFY_REQUEST, "我是" + UserInfoHelper.getUserName(account)));
                            SPUtils.getInstance().remove(CommonUtil.AUTO_ADD_TYPE);
                            SPUtils.getInstance().remove(CommonUtil.AUTO_ADD_VALUE);
                        }
                    }
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                }
            });
        } else if (type == 2) {
            String groupid = SPUtils.getInstance().getString(CommonUtil.AUTO_ADD_VALUE);
            if (!TeamHelper.isTeamMember(groupid, account)) {
                NIMClient.getService(TeamService.class).applyJoinTeam(groupid, "我是" + UserInfoHelper.getUserName(account));
                SPUtils.getInstance().remove(CommonUtil.AUTO_ADD_TYPE);
                SPUtils.getInstance().remove(CommonUtil.AUTO_ADD_VALUE);
            }
        }
    }
}
