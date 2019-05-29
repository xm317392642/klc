package com.xr.ychat.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
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

import com.blankj.utilcode.util.RegexUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.string.StringTextWatcher;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
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

/**
 * 登录/注册界面
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class SwitchAccountActivity extends UI implements OnKeyListener {
    private static final String ACCOUNT = "Account";
    private TextView switchLogin;
    private TextView loginAccount;
    private TextView userAgreement;

    private TextView switchModeBtn;  // 注册/登录切换按钮
    private XEditText loginPasswordEdit;
    private TextView loginForgetPassword;
    private XEditText registerPasswordEdit;
    private TextView registerSendCode;
    private View loginLayout;
    private View registerLayout;
    private Button commit;
    private HeadImageView fromAvatar;
    private TimeCount timeCount;

    private String account;
    private String avatar;
    private AbortableFuture<LoginInfo> loginRequest;
    private boolean registerMode = false; // 注册模式
    private boolean registerPanelInited = false; // 注册面板是否初始化

    public static void start(Context context, String account) {
        Intent intent = new Intent(context, SwitchAccountActivity.class);
        intent.putExtra(ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
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
        setActivityView(R.layout.activity_switch_account);
        account = Preferences.getUserPhone(SwitchAccountActivity.this);
        avatar = getIntent().getStringExtra(ACCOUNT);
        initRightTopBtn();
        setupLoginPanel();
        setupRegisterPanel();
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
     * 返回到登录页面
     */
    private void backLoginPage() {
        finish();
        LoginActivity.start(this);
    }

    @Override
    public void onBackPressed() {
        backLoginPage();
    }

    /**
     * ActionBar 右上角按钮
     */
    private void initRightTopBtn() {
        fromAvatar = (HeadImageView) findViewById(R.id.login_icon);
        fromAvatar.loadAvatar(avatar);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> backLoginPage());
        switchLogin = findView(R.id.switch_account);
        switchLogin.setOnClickListener(v ->
                backLoginPage()
        );
        loginAccount = (TextView) findViewById(R.id.login_account);
        loginAccount.setText(account);
        commit = (Button) findViewById(R.id.login_commit);
        commit.setOnClickListener(v -> {
            if (registerMode) {
                register();
            } else {
                fakeLogin();
            }
        });
        userAgreement = (TextView) findViewById(R.id.switch_account_agreement);
        userAgreement.setOnClickListener(v -> startActivity(new Intent(this, UserProtocolActivity.class)));
        SpannableString spannableString = new SpannableString("点击登录即表示阅读并同意《用户使用协议》");
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#BE6913")), 12, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        userAgreement.setText(spannableString);
    }

    /**
     * 登录面板
     */
    private void setupLoginPanel() {
        loginPasswordEdit = findView(R.id.edit_login_password);

        loginPasswordEdit.addTextChangedListener(new StringTextWatcher(18, loginPasswordEdit));
        loginPasswordEdit.setOnKeyListener(this);

        loginForgetPassword = findView(R.id.edit_login_forget);
        loginForgetPassword.setOnClickListener(v -> {
            ResetPasswordActivity.start(SwitchAccountActivity.this, false);
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

        if (!NetworkUtil.isNetAvailable(SwitchAccountActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }

        DialogMaker.showProgressDialog(this, getString(R.string.logining), false);

        // 注册流程
        final String password = registerPasswordEdit.getText().toString();

        ContactHttpClient.getInstance().register(account, password, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
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
                            DemoCache.setAccount(aVoid.getAccid());
                            Preferences.saveWeiranToken(SwitchAccountActivity.this, aVoid.getMytoken());
                            Preferences.saveWeiranUid(SwitchAccountActivity.this, aVoid.getUid());
                            saveLoginInfo(aVoid.getAccid(), aVoid.getToken(), account);
                            // 初始化消息提醒配置
                            initNotificationConfig();
                            // 进入主界面
                            MainActivity.start(SwitchAccountActivity.this, null);
                            finish();
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
                    DownloadTipsActivity.start(SwitchAccountActivity.this);
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
        // 密码检查
        String token = registerPasswordEdit.getEditableText().toString().trim();
        if (TextUtils.isEmpty(token)) {
            YchatToastUtils.showShort("请输入验证码");
            return false;
        }
        return true;
    }

    /**
     * 注册/登录切换
     */
    private void switchMode() {
        registerMode = !registerMode;
        if (registerMode && !registerPanelInited) {
            registerPasswordEdit = findView(R.id.edit_register_password);
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
     * 密码登录
     */
    private void fakeLogin() {
        String token = loginPasswordEdit.getEditableText().toString().trim();
        if (!RegexUtils.isMobileExact(account)) {
            YchatToastUtils.showShort("请输入正确的手机号码");
            return;
        }
        if (TextUtils.isEmpty(token)) {
            YchatToastUtils.showShort("请输入登录密码");
            return;
        }
        if (!NetworkUtil.isNetAvailable(SwitchAccountActivity.this)) {
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
                            DemoCache.setAccount(aVoid.getAccid());
                            Preferences.saveWeiranToken(SwitchAccountActivity.this, aVoid.getMytoken());
                            Preferences.saveWeiranUid(SwitchAccountActivity.this, aVoid.getUid());
                            saveLoginInfo(aVoid.getAccid(), aVoid.getToken(), account);
                            // 初始化消息提醒配置
                            initNotificationConfig();
                            // 进入主界面
                            MainActivity.start(SwitchAccountActivity.this, null);
                            finish();
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
        if (!NetworkUtil.isNetAvailable(SwitchAccountActivity.this)) {
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
                    DownloadTipsActivity.start(SwitchAccountActivity.this);
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
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            runOnUiThread(() -> {
                registerSendCode.setEnabled(true);
                registerSendCode.setTextColor(ContextCompat.getColor(SwitchAccountActivity.this, R.color.color_be6913));
                registerSendCode.setText("获取验证码");
            });
        }

        @Override
        public void onTick(long millisUntilFinished) {
            runOnUiThread(() -> {
                registerSendCode.setEnabled(false);
                registerSendCode.setTextColor(ContextCompat.getColor(SwitchAccountActivity.this, R.color.color_a9a9a9));
                registerSendCode.setText("重新获取" + (millisUntilFinished + 1000) / 1000 + "s");
            });
        }
    }

}
