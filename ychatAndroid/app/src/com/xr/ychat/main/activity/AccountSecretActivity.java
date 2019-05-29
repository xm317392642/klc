package com.xr.ychat.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
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
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.config.preference.UserPreferences;
import com.xr.ychat.contact.activity.BlackListActivity;
import com.xr.ychat.login.BindPhoneNumActivity;
import com.xr.ychat.login.ResetPasswordActivity;
import com.xr.ychat.main.adapter.SettingsAdapter;
import com.xr.ychat.main.model.SettingTemplate;
import com.xr.ychat.main.model.SettingType;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号与隐私
 * Created by hzxuwen on 2015/6/26.
 */
public class AccountSecretActivity extends SwipeBackUI implements SettingsAdapter.SwitchChangeListener {
    private static final int TAG_NOTICE = 2;
    private static final int TAG_SPEAKER = 7;
    private static final int TAG_RING = 11;
    private static final int TAG_NOTIFICATION_STYLE = 21; // 通知栏展开、折叠
    private static final int TAG_VIBRATE = 25; // 推送消息不展示详情

    private static final int TAG_BLACK_LIST = 26; // 黑名单
    private static final int TAG_SET_LOGIN_PWD = 27; // 账号与隐私
    private static final int TAG_SEPERATOR_TEXT = 28; // 账号与隐私
    private static final int TAG_ADD_METHOD = 29; // 账号与隐私
    private static final int TAG_BIND_PHONE = 30; //绑定手机号,没有绑定的话，显示出来；绑定的话则隐藏（后续会增加更改绑定手机号）

    private Toolbar mToolbar;
    private TextView toolbarTitle;
    ListView listView;
    SettingsAdapter adapter;
    private List<SettingTemplate> items = new ArrayList<SettingTemplate>();
    private String noDisturbTime;
    private SettingTemplate disturbItem;
    private SettingTemplate notificationItem;

    public static void start(Context context) {
        Intent intent = new Intent(context, AccountSecretActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    private void onListItemClick(SettingTemplate item) {
        if (item == null) return;

        switch (item.getId()) {
            case TAG_SET_LOGIN_PWD://设置登录密码
                ResetPasswordActivity.start(AccountSecretActivity.this, true);
                break;
            case TAG_BLACK_LIST://黑名单
                BlackListActivity.start(this);
                break;
            case TAG_ADD_METHOD://添加方式
                AddMineMethodActivity.start(this);
                break;
            case TAG_BIND_PHONE://绑定手机号
                Intent extras=new Intent(this,BindPhoneNumActivity.class);
                extras.putExtra("flag","AccountSecretActivity");//是从账号隐私页面跳转过去的，这种情况做特殊处理
                ActivityUtils.startActivityForResult(this,extras,TAG_BIND_PHONE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.settings_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("账号与隐私");
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

    private void initUI() {
        initItems();
        listView = (ListView) findViewById(R.id.settings_listview);
        initAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingTemplate item = items.get(position);
                onListItemClick(item);
            }
        });
    }

    private void initAdapter() {
        adapter = new SettingsAdapter(this, this, items);
        listView.setAdapter(adapter);
    }

    private void initItems() {
        items.clear();
        items.add(new SettingTemplate(TAG_ADD_METHOD, "添加我的方式", SettingType.TYPE_ARROW));

        NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount());
        //用户未绑定手机号，显示绑定手机号入口，已绑定用户显示设置登录密码
        if(TextUtils.isEmpty(userInfo.getMobile())){
            items.add(SettingTemplate.addLine());
            items.add(new SettingTemplate(TAG_BIND_PHONE, "绑定手机号", SettingType.TYPE_ARROW));
        }else{
            items.add(SettingTemplate.addLine());
            items.add(new SettingTemplate(TAG_SET_LOGIN_PWD, "设置登录密码", SettingType.TYPE_ARROW));
        }
        items.add(SettingTemplate.makeSeperator());
        notificationItem = new SettingTemplate(TAG_BLACK_LIST, "黑名单", SettingType.TYPE_ARROW);
        items.add(notificationItem);
        notificationItem = new SettingTemplate(TAG_SEPERATOR_TEXT, "被添加的联系人将无法发消息给你", SettingType.TYPE_SEPERATOR_TEXT);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case NoDisturbActivity.NO_DISTURB_REQ:
                    setNoDisturbTime(data);
                    break;
                case TAG_BIND_PHONE://绑定手机号成功，该页面刷新，item变为设置登录密码
                    initItems();
                    adapter.notifyDataSetChanged();
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
