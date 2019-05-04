package com.xr.ychat.redpacket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;

import com.alipay.sdk.app.AuthTask;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.xr.ychat.R;

import java.util.Map;

public class BindAlipayActivity extends SwipeBackUI {
    private static final int SDK_AUTH_FLAG = 2;
    private String uid;
    private String mytoken;
    private Button commit;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_AUTH_FLAG: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();
                    // 判断resultStatus 为“9000”且result_code为“200”则代表授权成功
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        saveAlipayAccount(uid, authResult.getUserId(), authResult.getAuthCode(), mytoken);
                    } else {
                        // 其他状态值则为授权失败
                        YchatToastUtils.showShort("授权失败");
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_alipay);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        uid = Preferences.getWeiranUid(this);
        mytoken = Preferences.getWeiranToken(this);
        commit = (Button) findViewById(R.id.bind_alipay_commit);
        commit.setOnClickListener(v -> {
            getAuthParameter(uid, mytoken);
        });
    }

    private void getAuthParameter(String uid, String mytoken) {
        DialogMaker.showProgressDialog(this, "获取授权参数", false);
        commit.setEnabled(false);
        ContactHttpClient.getInstance().getAuthParameter(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                commit.setEnabled(true);
                if (aVoid != null && !TextUtils.isEmpty(aVoid.getPara())) {
                    Runnable authRunnable = new Runnable() {

                        @Override
                        public void run() {
                            // 调用授权接口，获取授权结果
                            AuthTask authTask = new AuthTask(BindAlipayActivity.this);
                            Map<String, String> result = authTask.authV2(aVoid.getPara(), true);
                            Message msg = new Message();
                            msg.what = SDK_AUTH_FLAG;
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }
                    };
                    Thread authThread = new Thread(authRunnable);
                    authThread.start();
                } else {
                    YchatToastUtils.showShort("获取授权参数失败");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                commit.setEnabled(true);
                YchatToastUtils.showShort("获取授权参数失败");
            }
        });
    }

    private void saveAlipayAccount(String uid, String aliuid, String authcode, String mytoken) {
        DialogMaker.showProgressDialog(this, "保存授权参数", false);
        commit.setEnabled(false);
        ContactHttpClient.getInstance().saveAlipayAccount(uid, aliuid, authcode, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                commit.setEnabled(true);
                if (aVoid != null && aVoid.getAliinf() != null) {
                    String nickname = aVoid.getAliinf().getNick_name();
                    nickname = TextUtils.isEmpty(nickname) ? aVoid.getAliuid() : nickname;
                    bindAlipayAccount(uid, aVoid.getAliuid(), nickname, aVoid.getAliinf().getAvatar(), mytoken);
                } else {
                    YchatToastUtils.showShort("保存授权参数失败");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                commit.setEnabled(true);
                YchatToastUtils.showShort("保存授权参数失败");
            }
        });
    }

    private void bindAlipayAccount(String uid, String aliuid, String alinickname, String aliavatar, String mytoken) {
        DialogMaker.showProgressDialog(this, "正在绑定支付宝", false);
        commit.setEnabled(false);
        ContactHttpClient.getInstance().bindAlipayAccount(uid, aliuid, alinickname, aliavatar, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                commit.setEnabled(true);
                YchatToastUtils.showShort("支付宝绑定成功");
                finish();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                commit.setEnabled(true);
                YchatToastUtils.showShort("支付宝绑定失败");
            }
        });
    }

    public static void start(Activity context) {
        Intent intent = new Intent(context, BindAlipayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

}
