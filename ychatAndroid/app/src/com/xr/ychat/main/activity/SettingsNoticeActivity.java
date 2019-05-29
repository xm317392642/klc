package com.xr.ychat.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.mixpush.MixPushService;
import com.netease.nimlib.sdk.settings.SettingsServiceObserver;
import com.xr.ychat.R;
import com.xr.ychat.config.preference.UserPreferences;
import com.xr.ychat.main.adapter.SettingsAdapter;
import com.xr.ychat.main.model.SettingTemplate;
import com.xr.ychat.main.model.SettingType;

import java.util.ArrayList;
import java.util.List;

/**
 * 新消息通知
 * Created by hzxuwen on 2015/6/26.
 */
public class SettingsNoticeActivity extends SwipeBackUI implements SettingsAdapter.SwitchChangeListener {
    private static final int TAG_NOTICE = 2;
    private static final int TAG_SPEAKER = 7;
    private static final int TAG_RING = 11;
    private static final int TAG_NOTIFICATION_STYLE = 21; // 通知栏展开、折叠
    private static final int TAG_VIBRATE = 25; // 推送消息不展示详情
    private static final int TAG_ACCOUNT_SECRET = 26; // 账号与隐私
    private static final int TAG_SEPERATOR_TEXT = 27; // 账号与隐私

    private Toolbar mToolbar;
    private TextView toolbarTitle;
    ListView listView;
    SettingsAdapter adapter;
    private List<SettingTemplate> items = new ArrayList<>();
    private String noDisturbTime;
    private SettingTemplate notificationItem;

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsNoticeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.settings_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("新消息通知");
        mToolbar.setNavigationOnClickListener(v -> finish());

        initData();
        initUI();

        registerObservers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(SettingsServiceObserver.class).observeMultiportPushConfigNotify(pushConfigObserver, register);
    }

    Observer<Boolean> pushConfigObserver = new Observer<Boolean>() {
        @Override
        public void onEvent(Boolean aBoolean) {
            YchatToastUtils.showShort("收到multiport push config：" + aBoolean);
        }
    };

    private void initData() {
        if (UserPreferences.getStatusConfig() == null || !UserPreferences.getStatusConfig().downTimeToggle) {
            noDisturbTime = getString(R.string.setting_close);
        } else {
            noDisturbTime = String.format("%s到%s", UserPreferences.getStatusConfig().downTimeBegin, UserPreferences.getStatusConfig().downTimeEnd);
        }
        //getSDKDirCacheSize();
    }

    private void initUI() {
        initItems();
        listView = (ListView) findViewById(R.id.settings_listview);
        initAdapter();
    }

    private void initAdapter() {
        adapter = new SettingsAdapter(this, this, items);
        listView.setAdapter(adapter);
    }

    private void initItems() {
        items.clear();

        items.add(SettingTemplate.makeSeperator());
        notificationItem = new SettingTemplate(TAG_NOTICE, getString(R.string.msg_notice), SettingType.TYPE_TOGGLE,
                UserPreferences.getNotificationToggle());
        items.add(notificationItem);//新消息通知
        notificationItem = new SettingTemplate(TAG_SEPERATOR_TEXT, "关闭后，手机将不再接收新消息通知", SettingType.TYPE_SEPERATOR_TEXT);
        items.add(notificationItem);
        //pushShowNoDetailItem = new SettingTemplate(TAG_PUSH_SHOW_NO_DETAIL, getString(R.string.push_no_detail), SettingType.TYPE_TOGGLE, getIsShowPushNoDetail());
        //items.add(pushShowNoDetailItem);
        items.add(new SettingTemplate(TAG_RING, getString(R.string.ring), SettingType.TYPE_TOGGLE, UserPreferences.getRingToggle()));
        items.add(SettingTemplate.addLine());
        items.add(new SettingTemplate(TAG_VIBRATE, getString(R.string.vibrate), SettingType.TYPE_TOGGLE, UserPreferences.getVibrateToggle()));
        notificationItem = new SettingTemplate(TAG_SEPERATOR_TEXT, "当空了吹在运行时，你可以设置是否需要声音或振动提示", SettingType.TYPE_SEPERATOR_TEXT);
        items.add(notificationItem);
    }


    @Override
    public void onSwitchChange(SettingTemplate item, boolean checkState) {
        switch (item.getId()) {
            case TAG_NOTICE:
                setMessageNotify(checkState);
                break;
            case TAG_SPEAKER:
                NimUIKit.setEarPhoneModeEnable(checkState);
                break;
            case TAG_RING: {
                UserPreferences.setRingToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.ring = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
                break;
            }
            case TAG_VIBRATE: {
                UserPreferences.setVibrateToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.vibrate = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
            }
            break;
            case TAG_NOTIFICATION_STYLE: {
                UserPreferences.setNotificationFoldedToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.notificationFolded = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
                break;
            }
        }
        item.setChecked(checkState);
    }

    private void setMessageNotify(final boolean checkState) {
        // 如果接入第三方推送（小米），则同样应该设置开、关推送提醒
        // 如果关闭消息提醒，则第三方推送消息提醒也应该关闭。
        // 如果打开消息提醒，则同时打开第三方推送消息提醒。
        NIMClient.getService(MixPushService.class).enable(checkState).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                notificationItem.setChecked(checkState);
                setToggleNotification(checkState);
            }

            @Override
            public void onFailed(int code) {
                notificationItem.setChecked(!checkState);
                // 这种情况是客户端不支持第三方推送
                if (code == ResponseCode.RES_UNSUPPORT) {
                    YchatToastUtils.showShort("ResponseCode.RES_UNSUPPORT=" + code);
                    notificationItem.setChecked(checkState);
                    setToggleNotification(checkState);
                } else if (code == ResponseCode.RES_EFREQUENTLY) {
                    YchatToastUtils.showShort(R.string.operation_too_frequent);
                } else {
                    YchatToastUtils.showShort(R.string.user_info_update_failed);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    private void setToggleNotification(boolean checkState) {
        try {
            setNotificationToggle(checkState);
            NIMClient.toggleNotification(checkState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNotificationToggle(boolean on) {
        UserPreferences.setNotificationToggle(on);
    }


}
