package com.xr.ychat.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.AppUtils;
import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.avchatkit.constant.AVChatExtras;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.UpdateInfo;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.mixpush.MixPushService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.common.util.sys.SysInfoUtil;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.xr.ychat.login.DownloadUtils;
import com.xr.ychat.login.LoginAuthorizeActivity;
import com.xr.ychat.mixpush.DemoMixPushMessageHandler;

import java.util.ArrayList;
import java.util.Map;

/**
 * 欢迎/导航页（app启动Activity）
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class WelcomeActivity extends UI {
    private boolean customSplash = false;
    private static boolean firstEnter = true; // 是否首次进入

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //处理首次安装点击打开切到后台,点击桌面图标再回来重启的问题及通过应用宝唤起在特定条件下重走逻辑的问题
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }
        DemoCache.setMainTaskLaunching(true);
        if (savedInstanceState != null) {
            setIntent(new Intent()); // 从堆栈恢复，不再重复解析之前的intent
        }
        if (!firstEnter) {
            onIntent(); // APP进程还在，Activity被重新调度起来
        } else {
            showSplashView(); // APP进程重新起来
        }
    }

    private void showSplashView() {
        // 首次进入，打开欢迎界面
        getWindow().setBackgroundDrawableResource(R.drawable.splash_bg);
        customSplash = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /*
         * 如果Activity在，不会走到onCreate，而是onNewIntent，这时候需要setIntent
         * 场景：点击通知栏跳转到此，会收到Intent
         */
        setIntent(intent);
        if (!customSplash) {
            onIntent();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstEnter) {
            firstEnter = false;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!NimUIKit.isInitComplete()) {
                        new Handler().postDelayed(this, 100);
                        return;
                    }
                    customSplash = false;
                    queryAppVersion();
                }
            };
            if (customSplash) {
                new Handler().postDelayed(runnable, 1000);
            } else {
                runnable.run();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DemoCache.setMainTaskLaunching(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    // 处理收到的Intent
    private void onIntent() {
        if (TextUtils.isEmpty(DemoCache.getAccount())) {
            // 判断当前app是否正在运行0
            if (!SysInfoUtil.stackResumed(this)) {
                //LoginActivity.start(this);
                LoginAuthorizeActivity.start(this);
            }
            finish();
        } else {
            // 已经登录过了，处理过来的请求
            Intent intent = getIntent();
            if (intent != null) {
                if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
                    parseNotifyIntent(intent);
                    return;
                } else if (NIMClient.getService(MixPushService.class).isFCMIntent(intent)) {
                    parseFCMNotifyIntent(NIMClient.getService(MixPushService.class).parseFCMPayload(intent));
                } else if (intent.hasExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION) || intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT)) {
                    parseNormalIntent(intent);
                }
            }

            if (!firstEnter && intent == null) {
                finish();
            } else {
                showMainActivity();
            }
        }
    }

    /**
     * 已经登陆过，自动登陆
     */
    private boolean canAutoLogin() {
        String account = Preferences.getUserAccount(this);
        String token = Preferences.getUserToken(this);
        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token);
    }

    private void parseNotifyIntent(Intent intent) {
        ArrayList<IMMessage> messages = (ArrayList<IMMessage>) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
        if (messages == null || messages.size() > 1) {
            showMainActivity(null);
        } else {
            showMainActivity(new Intent().putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, messages.get(0)));
        }
    }

    private void parseFCMNotifyIntent(String payloadString) {
        Map<String, String> payload = JSON.parseObject(payloadString, Map.class);
        String sessionId = payload.get(DemoMixPushMessageHandler.PAYLOAD_SESSION_ID);
        String type = payload.get(DemoMixPushMessageHandler.PAYLOAD_SESSION_TYPE);
        if (sessionId != null && type != null) {
            int typeValue = Integer.valueOf(type);
            IMMessage message = MessageBuilder.createEmptyMessage(sessionId, SessionTypeEnum.typeOfValue(typeValue), 0);
            showMainActivity(new Intent().putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, message));
        } else {
            showMainActivity(null);
        }
    }

    private void parseNormalIntent(Intent intent) {
        showMainActivity(intent);
    }

    private void showMainActivity() {
        showMainActivity(null);
    }

    private void showMainActivity(Intent intent) {
        MainActivity.start(WelcomeActivity.this, intent);

        //MainShareActivity.start(WelcomeActivity.this, intent);
        finish();
    }

    /**
     * 版本检测，判断是否更新
     * isForce(1：强制更新0：不需要强制更新)
     */
    private void queryAppVersion() {
        ContactHttpClient.getInstance().queryAppVersion(new ContactHttpClient.ContactHttpCallback<UpdateInfo>() {
            @Override
            public void onSuccess(UpdateInfo updateInfo) {
                if (updateInfo.getUpdate() == 1) {
                    //需要更新
                    int isForce = updateInfo.getIsForce();
                    //强制更新只有一个确定按钮（非强制更新则有确定和取消）
                    final EasyAlertDialog alertDialog = new EasyAlertDialog(WelcomeActivity.this);
                    alertDialog.setMessage("有最新版本了,是否更新?");
                    alertDialog.setCancelable(false);
                    alertDialog.addPositiveButton("确定", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                            v -> {
                                alertDialog.dismiss();
                                new DownloadUtils(WelcomeActivity.this, AppUtils.getAppName(), updateInfo.getDownUrl(), "konglechui" + updateInfo.getVersion() + ".apk");
                                nextStep();
                            });
                    if (isForce == 0) {
                        alertDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                                v -> {
                                    alertDialog.dismiss();
                                    nextStep();
                                });
                    }
                    alertDialog.show();
                } else {
                    nextStep();//不需要更新
                }

            }

            @Override
            public void onFailed(int code, String errorMsg) {
                nextStep();
            }
        });
    }

    private void nextStep() {
        if (canAutoLogin()) {
            onIntent();
        } else {
            //LoginActivity.start(WelcomeActivity.this);
            LoginAuthorizeActivity.start(WelcomeActivity.this);
            finish();
        }
    }
}
