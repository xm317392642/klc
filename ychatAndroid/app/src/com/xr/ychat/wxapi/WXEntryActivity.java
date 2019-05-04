package com.xr.ychat.wxapi;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.common.util.WxShareUtils;
import com.xr.ychat.config.preference.UserPreferences;
import com.xr.ychat.login.BindPhoneNumActivity;
import com.xr.ychat.login.DownloadTipsActivity;
import com.xr.ychat.login.LoginActivity;
import com.xr.ychat.login.LoginAuthorizeActivity;
import com.xr.ychat.login.SwitchAccountActivity;
import com.xr.ychat.main.activity.MainActivity;
import com.xr.ychat.main.activity.MainShareActivity;

import java.util.ArrayList;

public class WXEntryActivity extends UI implements IWXAPIEventHandler {

    private IWXAPI wxapi;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        wxapi.handleIntent(intent, this);
    }

    private View getView() {
        FrameLayout parentView = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        parentView.setBackgroundColor(Color.WHITE);
        parentView.setLayoutParams(params);

        ProgressBar progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params1.bottomMargin = ScreenUtil.dip2px(16f);
        params1.gravity = Gravity.CENTER;
        if (Build.VERSION.SDK_INT >= 21) {
            ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_be6913));
            progressBar.setIndeterminateTintList(colorStateList);
        }
        progressBar.setLayoutParams(params1);

        TextView textView = new TextView(this);
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.topMargin = ScreenUtil.dip2px(16f);
        params2.gravity = Gravity.CENTER;
        textView.setLayoutParams(params2);
        textView.setText("加载中...");
        parentView.addView(progressBar);
        parentView.addView(textView);
        return parentView;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getView());

        wxapi = WXAPIFactory.createWXAPI(this, WxShareUtils.APP_ID);
        wxapi.handleIntent(getIntent(), this);
    }

    /**
     * 微信发送请求到第三方应用时，会回调到该方法
     */
    @Override
    public void onReq(BaseReq baseReq) {
        // 这里不作深究

    }


    /**
     * 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
     * app发送消息给微信，处理返回消息的回调
     */
    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            // 正确返回
            case BaseResp.ErrCode.ERR_OK://用户同意
                switch (baseResp.getType()) {
                    case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:// ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX是微信分享，api自带
                        finish();
                        break;
                    case ConstantsAPI.COMMAND_SENDAUTH://授权登录

                        String wxCode = ((SendAuth.Resp) baseResp).code;
                        //授权登录code：0810QszF1vGaB80ek1BF1RgqzF10Qszc  0213JtBc1vAuYw0mWMCc1yzlBc13JtBR
                        //DialogMaker.showProgressDialog(this, "登录中...",false);
                        ContactHttpClient.getInstance().wxRegister(wxCode, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                            @Override
                            public void onSuccess(RequestInfo loginInfo) {
                                AbortableFuture<LoginInfo> loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(loginInfo.getAccid(), loginInfo.getToken()));
                                loginRequest.setCallback(new RequestCallbackWrapper() {
                                    @Override
                                    public void onResult(int code, Object result, Throwable exception) {
                                        if (code == ResponseCode.RES_SUCCESS) {
                                            NimUIKit.loginSuccess(loginInfo.getAccid());
                                            DemoCache.setAccount(loginInfo.getAccid());
                                            Preferences.saveWeiranToken(WXEntryActivity.this, loginInfo.getMytoken());
                                            Preferences.saveWeiranUid(WXEntryActivity.this, loginInfo.getUid());
                                            saveLoginInfo(loginInfo.getAccid(), loginInfo.getToken(), wxCode);
                                            // 初始化消息提醒配置
                                            initNotificationConfig();
                                            dealAutoAddEvent(loginInfo.getAccid());
                                            // 只是第一次登录时弹出绑定手机号
                                            if (loginInfo.getIsfirstlogin() == 1) {
                                                BindPhoneNumActivity.start(WXEntryActivity.this, null);//首次登录成功，用户没有绑定手机号，跳转到绑定页面
                                                finishPage();
                                            } else {
                                                NimUIKit.getUserInfoProvider().getUserInfoAsync(NimUIKit.getAccount(), (success, responseResult, responseCode) -> {
                                                    NimUserInfo userInfo = (NimUserInfo) responseResult;
                                                    if (success && userInfo != null && !TextUtils.isEmpty(userInfo.getMobile())) {
                                                        Preferences.saveUserPhone(WXEntryActivity.this, userInfo.getMobile());//说明该用户已绑定手机号
                                                    }
                                                });

                                                jumpToMainOrShare();
                                            }
                                        } else {
                                            YchatToastUtils.showShort("登录失败" + code);
                                            finish();
                                        }
                                    }
                                });
                                //DialogMaker.dismissProgressDialog();
                            }

                            @Override
                            public void onFailed(int code, String errorMsg) {
                                //DialogMaker.dismissProgressDialog();
                                if (code == 100001) {
                                    YchatToastUtils.showShort("请求缺少参数");
                                } else if (code == 100006) {
                                    YchatToastUtils.showShort("验证码错误");
                                } else if (code == 100007) {
                                    YchatToastUtils.showShort("账户数据异常");
                                } else if (code == 100009) {
                                    YchatToastUtils.showShort("手机号不对");
                                } else if (code == 100008) {
                                    YchatToastUtils.showShort("手机号没有在平台绑定账户");
                                    DownloadTipsActivity.start(WXEntryActivity.this);
                                } else {
                                    YchatToastUtils.showShort(code + "注册失败");
                                }
                                finish();
                            }
                        });
                        break;
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权
                finish();
                break;
            case BaseResp.ErrCode.ERR_COMM://其他错误
                finish();
                break;
        }
    }

    private void saveLoginInfo(final String account, final String token, final String phone) {
        Preferences.saveUserAccount(this, account);
        Preferences.saveUserToken(this, token);
        Preferences.saveUserPhone(this, phone);
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

    /**
     * 跳转到主界面或者分享界面
     */
    public void jumpToMainOrShare() {
        if (com.netease.nim.uikit.impl.preference.UserPreferences.getShare()) {
            MainShareActivity.start(this, null);
        } else {
            MainActivity.start(this, null);
        }
        finishPage();
    }

    public void finishPage() {
        finish();
        if (ActivityUtils.isActivityExistsInStack(LoginAuthorizeActivity.class)) {
            ActivityUtils.finishActivity(LoginAuthorizeActivity.class);
        }
        if (ActivityUtils.isActivityExistsInStack(LoginActivity.class)) {
            ActivityUtils.finishActivity(LoginActivity.class);
        }
        if (ActivityUtils.isActivityExistsInStack(SwitchAccountActivity.class)) {
            ActivityUtils.finishActivity(SwitchAccountActivity.class);
        }


    }
}