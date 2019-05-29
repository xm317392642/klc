package com.xr.ychat;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.microquation.linkedme.android.LinkedME;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.business.contact.core.query.PinYin;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.swipeback.ActivityLifecycleHelper;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.mixpush.NIMPushClient;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.tendcloud.tenddata.TCAgent;
import com.xr.ychat.config.preference.UserPreferences;
import com.xr.ychat.contact.ContactHelper;
import com.xr.ychat.main.activity.MiddleActivity;
import com.xr.ychat.mixpush.DemoMixPushMessageHandler;
import com.xr.ychat.mixpush.DemoPushContentProvider;
import com.xr.ychat.redpacket.NIMRedPacketClient;
import com.xr.ychat.session.NimDemoLocationProvider;
import com.xr.ychat.session.SessionHelper;

public class NimApplication extends Application {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        YchatToastUtils.showShort("NimApplication onTerminate");
    }

    /**
     * 注意：每个进程都会创建自己的Application 然后调用onCreate() 方法，
     * 如果用户有自己的逻辑需要写在Application#onCreate()（还有Application的其他方法）中，一定要注意判断进程，不能把业务逻辑写在core进程，
     * 理论上，core进程的Application#onCreate()（还有Application的其他方法）只能做与im sdk 相关的工作
     */
    @Override
    public void onCreate() {
        super.onCreate();
        DemoCache.setContext(this);
        // 此处 certificate 请传入为开发者配置好的小米证书名称
        // 4.6.0 开始，第三方推送配置入口改为 SDKOption#mixPushConfig，旧版配置方式依旧支持。
        NIMClient.init(this, getLoginInfo(), NimSDKOptionConfig.getSDKOptions(this));
        // crash handler
        CrashReport.initCrashReport(getApplicationContext(), com.xr.ychat.BuildConfig.BUGLY_APPID, false);

        registerActivityLifecycleCallbacks(ActivityLifecycleHelper.build());

        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {
            // 注册自定义推送消息处理，这个是可选项
            NIMPushClient.registerMixPushMessageHandler(new DemoMixPushMessageHandler());
            // 初始化红包模块，在初始化UIKit模块之前执行
            NIMRedPacketClient.init(this);
            // init pinyin
            PinYin.init(this);
            PinYin.validate();
            // 初始化UIKit模块
            initUIKit();
            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
            //关闭撤回消息提醒
//            NIMClient.toggleRevokeMessageNotification(false);
            // 云信sdk相关业务初始化
            NIMInitManager.getInstance().init(true);
            // 初始化音视频模块
            //initAVChatKit();
        }
        // 初始化SDK
        LinkedME.getInstance(this);
        if (BuildConfig.DEBUG) {

            //设置debug模式下打印LinkedME日志
            LinkedME.getInstance().setDebug();
        }
        //初始时请设置为false
        LinkedME.getInstance().setImmediate(false);
        //设置处理跳转逻辑的中转页，MiddleActivity详见后续配置
        LinkedME.getInstance().setHandleActivity(MiddleActivity.class.getName());
        if (BuildConfig.DEBUG) {
            TCAgent.LOG_ON = true;
        }
        TCAgent.init(this);
        TCAgent.setReportUncaughtExceptions(true);
    }

    private LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount(this);
        String token = Preferences.getUserToken(this);

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    private void initUIKit() {
        // 初始化
        NimUIKit.init(this, buildUIKitOptions());
        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());
        // IM 会话窗口的定制初始化。
        SessionHelper.init();
        // 通讯录列表定制初始化
        ContactHelper.init();
        // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
        NimUIKit.setCustomPushContentProvider(new DemoPushContentProvider());
        //NimUIKit.setOnlineStateContentProvider(new DemoOnlineStateContentProvider());
    }

    private UIKitOptions buildUIKitOptions() {
        UIKitOptions options = new UIKitOptions();
        // 设置app图片/音频/日志等缓存目录
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(this) + "/app";
        return options;
    }



}
