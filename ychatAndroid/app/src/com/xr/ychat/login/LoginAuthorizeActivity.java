package com.xr.ychat.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.common.util.WxShareUtils;

import org.json.JSONObject;

/**
 * 授权登录（目前只有微信登录）
 */
public class  LoginAuthorizeActivity extends UI {
    private static final String KICK_OUT = "KICK_OUT";

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, LoginAuthorizeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize_login);
        onParseIntent();
        TextView txLoginProtocol = findViewById(R.id.login_protocol);
        txLoginProtocol.setOnClickListener(v -> startActivity(new Intent(this, UserProtocolActivity.class)));
        SpannableString spannableString = new SpannableString("点击登录即表示阅读并同意《用户使用协议》");
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#BE6913")), 12, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txLoginProtocol.setText(spannableString);

        findViewById(R.id.btn_wx_login).setOnClickListener(v -> {
            //微信登录
            sendAuthRequest();
        });
        findViewById(R.id.tx_phone_login).setOnClickListener(v -> {
            //手机号登录
            LoginActivity.start(this);
            finish();
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
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";//如获取用户个人信息则填写snsapi_userinfo
        req.state = "com.xr.ychat";
        wxapi.sendReq(req);
    }
}


