package com.xr.ychat.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.lucene.LuceneService;
import com.netease.nimlib.sdk.mixpush.MixPushService;
import com.netease.nimlib.sdk.settings.SettingsService;
import com.netease.nimlib.sdk.settings.SettingsServiceObserver;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.config.preference.UserPreferences;
import com.xr.ychat.main.adapter.SettingsAdapter;
import com.xr.ychat.main.model.SettingTemplate;
import com.xr.ychat.main.model.SettingType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2015/6/26.
 */
public class SettingsActivity extends SwipeBackUI implements SettingsAdapter.SwitchChangeListener {
    private static final int TAG_NOTICE = 2;
    private static final int TAG_NO_DISTURBE = 3;
    private static final int TAG_ABOUT = 6;
    private static final int TAG_GENERAL = 8;
    private static final int TAG_NOTIFICATION_STYLE = 21; // 通知栏展开、折叠
    private static final int TAG_ACCOUNT_SECRET = 26; // 账号与隐私

    private Toolbar mToolbar;
    private TextView toolbarTitle;
    ListView listView;
    SettingsAdapter adapter;
    private List<SettingTemplate> items = new ArrayList<SettingTemplate>();
    private String noDisturbTime;
    private SettingTemplate disturbItem;
    private SettingTemplate notificationItem;

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText(R.string.settings);
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
            YchatToastUtils.showShort( "收到multiport push config：" + aBoolean);
        }
    };

    private void initData() {
        if (UserPreferences.getStatusConfig() == null || !UserPreferences.getStatusConfig().downTimeToggle) {
            noDisturbTime = getString(R.string.setting_close);
        } else {
            noDisturbTime = String.format("%s到%s", UserPreferences.getStatusConfig().downTimeBegin,
                    UserPreferences.getStatusConfig().downTimeEnd);
        }
        //getSDKDirCacheSize();
    }

    private EasyAlertDialog alertDialog;

    private void initUI() {
        initItems();
        listView = (ListView) findViewById(R.id.settings_listview);
        View footer = LayoutInflater.from(this).inflate(R.layout.settings_logout_footer, null);
        listView.addFooterView(footer);

        initAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingTemplate item = items.get(position);
                onListItemClick(item);
            }
        });
        View logoutBtn = footer.findViewById(R.id.settings_button_logout);
        logoutBtn.setOnClickListener(view -> {
            if (alertDialog == null) {
                alertDialog = new EasyAlertDialog(SettingsActivity.this);
                alertDialog.setMessage("确定退出登录?");
                alertDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                        v -> {
                            alertDialog.dismiss();
                        }
                );
                alertDialog.addPositiveButton("确定", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                        v -> {
                            alertDialog.dismiss();
                            logout();
                        });
            }
            alertDialog.show();
        });
    }

    private void initAdapter() {
        adapter = new SettingsAdapter(this, this, items);
        listView.setAdapter(adapter);
    }

    private void initItems() {
        items.clear();
        notificationItem = new SettingTemplate(TAG_NOTICE, getString(R.string.msg_notice), SettingType.TYPE_ARROW);
        items.add(notificationItem);//新消息通知
        items.add(SettingTemplate.addLine());
        notificationItem = new SettingTemplate(TAG_ACCOUNT_SECRET, getString(R.string.account_secret), SettingType.TYPE_ARROW
        );
        items.add(notificationItem);//账号与隐私
        items.add(SettingTemplate.addLine());
//        items.add(new SettingTemplate(TAG_SPEAKER, getString(R.string.msg_speaker), SettingType.TYPE_TOGGLE,
//                NimUIKit.isEarPhoneModeEnable()));
//        items.add(SettingTemplate.addLine());

        items.add(new SettingTemplate(TAG_GENERAL, getString(R.string.general_title), SettingType.TYPE_ARROW));//通用
        items.add(SettingTemplate.addLine());

        disturbItem = new SettingTemplate(TAG_NO_DISTURBE, getString(R.string.no_disturb), noDisturbTime);
        disturbItem.setType(SettingType.TYPE_TEXT_ARROW);
        items.add(disturbItem);

        items.add(SettingTemplate.makeSeperator());
        SettingTemplate aboutItem = new SettingTemplate(TAG_ABOUT, getString(R.string.setting_about), "版本" + AppUtils.getAppVersionName());
        aboutItem.setType(SettingType.TYPE_TEXT_ARROW);
        items.add(aboutItem);
        items.add(SettingTemplate.makeSeperator());
    }

    private void onListItemClick(SettingTemplate item) {
        if (item == null) return;

        switch (item.getId()) {
            case TAG_NOTICE:
                SettingsNoticeActivity.start(SettingsActivity.this);
                break;
            case TAG_ACCOUNT_SECRET:
                AccountSecretActivity.start(SettingsActivity.this);
                break;
            case TAG_NO_DISTURBE://免打扰
                startNoDisturb();
                break;
            case TAG_ABOUT:
                AboutActivity.start(SettingsActivity.this);
                break;
            case TAG_GENERAL:
                GeneralActivity.start(SettingsActivity.this);
                break;
            default:
                break;
        }
    }


    /**
     * 注销
     */
    private void logout() {
        NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount());
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(DemoCache.getAccount(), (success, result, code) -> {
                exit(userInfo);
            });
        } else {
          exit(userInfo);
        }
    }

    private void exit(NimUserInfo userInfo){

        MainActivity.logout(SettingsActivity.this, true, userInfo.getAvatar());
        NIMClient.getService(AuthService.class).logout();
        finish();
    }
    @Override
    public void onSwitchChange(SettingTemplate item, boolean checkState) {
        switch (item.getId()) {
            case TAG_NOTICE:
                setMessageNotify(checkState);
                break;
            case TAG_NOTIFICATION_STYLE: {
                UserPreferences.setNotificationFoldedToggle(checkState);
                StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
                config.notificationFolded = checkState;
                UserPreferences.setStatusConfig(config);
                NIMClient.updateStatusBarNotificationConfig(config);
                break;
            }
            default:
                break;
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
                    notificationItem.setChecked(checkState);
                    setToggleNotification(checkState);
                } else if (code == ResponseCode.RES_EFREQUENTLY) {
                    YchatToastUtils.showShort( R.string.operation_too_frequent);
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

    private void startNoDisturb() {
        NoDisturbActivity.startActivityForResult(this, UserPreferences.getStatusConfig(), noDisturbTime, NoDisturbActivity.NO_DISTURB_REQ);
    }

    private String getIndexCacheSize() {
        long size = NIMClient.getService(LuceneService.class).getCacheSize();
        return String.format("%.2f", size / (1024.0f * 1024.0f));
    }

//    private void clearIndex() {
//        NIMClient.getService(LuceneService.class).clearCache();
//        clearIndexItem.setDetail("0.00 M");
//        adapter.notifyDataSetChanged();
//    }

    private void updateMultiportPushConfig(final boolean checkState) {
        NIMClient.getService(SettingsService.class).updateMultiportPushConfig(checkState).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                YchatToastUtils.showShort("设置成功");
            }

            @Override
            public void onFailed(int code) {
                YchatToastUtils.showShort("设置失败,code:" + code);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case NoDisturbActivity.NO_DISTURB_REQ:
                    setNoDisturbTime(data);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置免打扰时间
     *
     * @param data
     */
    private void setNoDisturbTime(Intent data) {
        boolean isChecked = data.getBooleanExtra(NoDisturbActivity.EXTRA_ISCHECKED, false);
        noDisturbTime = getString(R.string.setting_close);
        StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
        if (isChecked) {
            config.downTimeBegin = data.getStringExtra(NoDisturbActivity.EXTRA_START_TIME);
            config.downTimeEnd = data.getStringExtra(NoDisturbActivity.EXTRA_END_TIME);
            noDisturbTime = String.format("%s到%s", config.downTimeBegin, config.downTimeEnd);
        } else {
            config.downTimeBegin = null;
            config.downTimeEnd = null;
        }
        disturbItem.setDetail(noDisturbTime);
        adapter.notifyDataSetChanged();
        UserPreferences.setDownTimeToggle(isChecked);
        config.downTimeToggle = isChecked;
        UserPreferences.setStatusConfig(config);
        NIMClient.updateStatusBarNotificationConfig(config);
    }
}
