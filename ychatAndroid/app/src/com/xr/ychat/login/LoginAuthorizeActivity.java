package com.xr.ychat.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CacheMemoryUtils;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.CustomClickListener;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xr.ychat.R;
import com.xr.ychat.common.util.WxShareUtils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 授权登录（目前只有微信登录）
 */
@RuntimePermissions
public class LoginAuthorizeActivity extends UI {
    private View btnWxLogin;
    private static final String KICK_OUT = "KICK_OUT";
    private boolean isAllowPermissions = false;

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, LoginAuthorizeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
    }

    /**
     * 版本检测，判断是否更新
     * isForce(1：强制更新0：不需要强制更新.)
     */
    /**
     * 2，需要WRITE_EXTERNAL_STORAGE权限，在对应的方法是标明
     */
    @NeedsPermission({
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void queryAppVersionRequest() {
        isAllowPermissions = true;
//        SPUtils.getInstance().put("server_version", "");
//        ContactHttpClient.getInstance().queryAppVersion(new ContactHttpClient.ContactHttpCallback<UpdateInfo>() {
//            @Override
//            public void onSuccess(UpdateInfo updateInfo) {
//                if ("1".equals(updateInfo.getUpdate())) {//需要更新
//                    CommonUtil.setUpdateInfo(updateInfo.getIsForce(), updateInfo.getUpdate(), updateInfo.getVersion(), updateInfo.getDownUrl());
//                    update(updateInfo);
//                }
//
//            }
//
//            @Override
//            public void onFailed(int code, String errorMsg) {
//
//            }
//        });
    }

    /**
     * ,3，对需要该权限的解释
     */
    @OnShowRationale({
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showWhy(final PermissionRequest request) {
        EasyAlertDialog dialog = new EasyAlertDialog(this);
        dialog.setMessage("我们需要存储权限，否则不能正常使用?");
        dialog.setCancelable(false);
        dialog.addPositiveButton("授权", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                v -> {
                    request.proceed();
                    dialog.dismiss();

                });
        dialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                v -> {
                    request.cancel();
                    dialog.dismiss();
                });
        dialog.show();
    }

    /**
     * ,4，当用户拒绝获取权限的提示
     */
    @OnPermissionDenied({
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showDenied() {
        //Toast.makeText(this, "无法获得权限", Toast.LENGTH_SHORT).show();
        YchatToastUtils.showShort("点击登录进行授权");
    }

    /**
     * ,5，当用户勾选不再提示并且拒绝的时候调用的方法
     */
    @OnNeverAskAgain({
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showNeverAskAgain() {
        EasyAlertDialog dialog = new EasyAlertDialog(ActivityUtils.getTopActivity());
        dialog.setMessage("应用权限被拒绝,为了不影响您的正常使用，请在 权限 中开启对应权限");
        dialog.setCancelable(false);
        dialog.addPositiveButton("进入设置", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                v -> {
                    //引导用户至设置页手动授权
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    dialog.dismiss();

                });
        dialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                v -> {
                    dialog.dismiss();
                });
        dialog.show();
    }

    /**
     * ,6，权限回调，调用PermissionsDispatcher的回调方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LoginAuthorizeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            isAllowPermissions = true;//6.0
            queryAppVersionRequest();
        } else {
            LoginAuthorizeActivityPermissionsDispatcher.queryAppVersionRequestWithPermissionCheck(LoginAuthorizeActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityUtils.getActivityList().size() == 0) {
            CacheMemoryUtils.getInstance().put("cancel", "0");
        }
    }

    /**
     * ,6，权限回调，调用PermissionsDispatcher的回调方法
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_authorize_login);
        onParseIntent();
        TextView txLoginProtocol = findViewById(R.id.login_protocol);
        txLoginProtocol.setOnClickListener(v -> startActivity(new Intent(this, UserProtocolActivity.class)));
        SpannableString spannableString = new SpannableString("点击登录即表示阅读并同意《用户使用协议》");
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#BE6913")), 12, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txLoginProtocol.setText(spannableString);
        btnWxLogin = findViewById(R.id.btn_wx_login);
        btnWxLogin.setOnClickListener(new CustomClickListener() {
            @Override
            protected void onSingleClick(View v) {
                if (isAllowPermissions) {
                    sendAuthRequest(); //微信登录
                } else {
                    LoginAuthorizeActivityPermissionsDispatcher.queryAppVersionRequestWithPermissionCheck(LoginAuthorizeActivity.this);
                }
            }
        });
        findViewById(R.id.tx_phone_login).setOnClickListener(new CustomClickListener() {
            @Override
            protected void onSingleClick(View v) {
                if (isAllowPermissions) {
                    LoginActivity.start(LoginAuthorizeActivity.this); //手机号登录
                    finish();
                } else {
                    LoginAuthorizeActivityPermissionsDispatcher.queryAppVersionRequestWithPermissionCheck(LoginAuthorizeActivity.this);
                }
            }
        });

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
        EasyAlertDialogHelper.showOneButtonDiolag(this,
                getString(R.string.kickout_notify),
                String.format(getString(R.string.kickout_content), client),
                getString(R.string.ok),
                true,
                null);

    }

    /**
     * send oauth request
     */
    public void sendAuthRequest() {
        IWXAPI wxapi = WXAPIFactory.createWXAPI(this, WxShareUtils.APP_ID);
        if (wxapi == null || !wxapi.isWXAppInstalled()) {
            YchatToastUtils.showShort("您没有安装微信");
            return;
        }
        wxapi.registerApp(WxShareUtils.APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.openId = WxShareUtils.APP_ID;
        req.scope = "snsapi_userinfo";//如获取用户个人信息则填写snsapi_userinfo
        req.state = "com.xr.ychat.wx.state";
        wxapi.sendReq(req);
    }

}


