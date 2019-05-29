package com.xr.ychat.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.recent.RecentContactsFragment;
import com.netease.nim.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.xr.ychat.R;
import com.xr.ychat.contact.activity.UserProfileActivity;
import com.xr.ychat.team.TeamCreateHelper;

import java.util.ArrayList;

/**
 * Created by hzxuwen on 2015/10/13.
 */
public class MessageInfoActivity extends SwipeBackUI {
    private final static String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
    private static final int REQUEST_CODE_ADVANCED = 2;
    private final String KEY_MSG_NOTICE = "msg_notice";
    private final String KEY_RECENT_STICKY = "recent_contacts_sticky";
    // data
    private String account;
    // view
    private SwitchButton switchButton;
    private SwitchButton SettingTopButton;
    private Toolbar mToolbar;
    private TextView toolbarTitle;

    public static void startActivity(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, MessageInfoActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, account);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.message_info_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText(R.string.message_info);
        mToolbar.setNavigationOnClickListener(v -> {
            showKeyboard(false);
            finish();
        });
        account = getIntent().getStringExtra(EXTRA_ACCOUNT);
        findViews();
        getYChatNum();
    }

    private String otherYchatNo;

    private void getYChatNum() {
        DialogMaker.showProgressDialog(this, "");
        ContactHttpClient.getInstance().getYchatAccount(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                otherYchatNo = aVoid.getYchatNo();
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                otherYchatNo = "";
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSwitchBtn();
    }

    private void findViews() {
        findView(R.id.complaint).setOnClickListener(v -> {
            String urlString = "web://h5/enter?&web_view_title=投诉&person_id=" + otherYchatNo;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            startActivity(intent);
        });
        HeadImageView userHead = (HeadImageView) findViewById(R.id.user_layout).findViewById(R.id.imageViewHeader);
        TextView userName = (TextView) findViewById(R.id.user_layout).findViewById(R.id.textViewName);
        userHead.loadBuddyAvatar(account);
        userName.setText(UserInfoHelper.getUserDisplayName(account));
        userHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserProfile();
            }
        });

        //((TextView) findViewById(R.id.create_team_layout).findViewById(R.id.textViewName)).setText(R.string.create_normal_team);
        HeadImageView addImage = (HeadImageView) findViewById(R.id.create_team_layout).findViewById(R.id.imageViewHeader);
        addImage.setBackgroundResource(com.netease.nim.uikit.R.drawable.nim_team_member_add_selector);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTeamMsg();
            }
        });

        switchButton = findViewById(R.id.toggle_layout).findViewById(R.id.user_notification_toggle);
        switchButton.setTag(KEY_MSG_NOTICE);
        switchButton.setOnChangedListener(onChangedListener);
        View settingLayout = findViewById(R.id.setting_layout);
        SettingTopButton = settingLayout.findViewById(R.id.setting_top_toggle);
        SettingTopButton.setTag(KEY_RECENT_STICKY);
        SettingTopButton.setOnChangedListener(onChangedListener);
        if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
            settingLayout.setVisibility(View.VISIBLE);
            RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(account, SessionTypeEnum.P2P);
            boolean isSticky = recentContact != null && CommonUtil.isTagSet(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
            SettingTopButton.setCheck(isSticky);
        } else {
            settingLayout.setVisibility(View.GONE);
        }

        TextView clearHistory = (TextView) findViewById(R.id.remove_chat);
        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyAlertDialogHelper.createOkCancelDiolag(MessageInfoActivity.this, null, "确定要清空吗？", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        NIMClient.getService(MsgService.class).clearChattingHistory(account, SessionTypeEnum.P2P);
                        MessageListPanelHelper.getInstance().notifyClearMessages(account);//用户A给好友发送多条消息,好友点...清空聊天记录，程序崩溃（数组下标越界异常）
                    }
                }).show();
            }
        });
        TextView findHistory = (TextView) findViewById(R.id.find_chat);
        findHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = String.format("scheme://ychat/sessionsearch?EXTRA_SESSION_ID=%1$s&EXTRA_SESSION_TYPE=%2$s", account, String.valueOf(SessionTypeEnum.P2P.getValue()));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

    }

    private void updateSwitchBtn() {
        boolean notice = NIMClient.getService(FriendService.class).isNeedMessageNotify(account);
        switchButton.setCheck(!notice);
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            if (!NetworkUtil.isNetAvailable(MessageInfoActivity.this)) {
                YchatToastUtils.showShort(R.string.network_is_not_available);
                switchButton.setCheck(!checkState);
                return;
            }
            String tag = (String) v.getTag();
            if (TextUtils.equals(tag, KEY_MSG_NOTICE)) {
                if (!NetworkUtil.isNetAvailable(MessageInfoActivity.this)) {
                    YchatToastUtils.showShort(R.string.network_is_not_available);
                    switchButton.setCheck(!checkState);
                    return;
                }
                NIMClient.getService(FriendService.class).setMessageNotify(account, !checkState).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        if (checkState) {
                            YchatToastUtils.showShort("已开启消息免打扰");
                        } else {
                            YchatToastUtils.showShort("已关闭消息免打扰");
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 408) {
                            YchatToastUtils.showShort(R.string.network_is_not_available);
                        } else {
                            YchatToastUtils.showShort("on failed:" + code);
                        }
                        switchButton.setCheck(!checkState);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
            } else {
                //查询之前是不是存在会话记录
                RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(account, SessionTypeEnum.P2P);
                //置顶
                if (checkState) {
                    //如果之前不存在，创建一条空的会话记录
                    if (recentContact == null) {
                        // RecentContactsFragment 的 MsgServiceObserve#observeRecentContact 观察者会收到通知
                        NIMClient.getService(MsgService.class).createEmptyRecentContact(account,
                                SessionTypeEnum.P2P,
                                RecentContactsFragment.RECENT_TAG_STICKY,
                                System.currentTimeMillis(),
                                true);
                    }
                    // 之前存在，更新置顶flag
                    else {
                        CommonUtil.addTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                }
                //取消置顶
                else {
                    if (recentContact != null) {
                        CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                        NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                    }
                }
            }
        }
    };

    private void openUserProfile() {
        UserProfileActivity.start(this, account);
    }

    /**
     * 创建群聊
     */
    private void createTeamMsg() {
        int capacity = 50;
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = NimUIKit.getContext().getString(com.netease.nim.uikit.R.string.invite_member);
        option.maxSelectNum = capacity;
        option.maxSelectedTip = NimUIKit.getContext().getString(com.netease.nim.uikit.R.string.reach_team_member_capacity, capacity);
        option.allowSelectEmpty = false;
        ArrayList<String> includeAccounts = new ArrayList<>();
        includeAccounts.add(account);
        includeAccounts.add(SPUtils.getInstance().getString(CommonUtil.ASSISTANT));
        option.itemFilter = new ContactIdFilter(includeAccounts, true);
        NimUIKit.startContactSelector(this, option, REQUEST_CODE_ADVANCED); // 创建群
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_ADVANCED) {
                final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
                    if (!selected.contains(account)) {
                        selected.add(0, account);
                    }
                    TeamCreateHelper.createNormalTeam(MessageInfoActivity.this, selected, true, new RequestCallback<CreateTeamResult>() {
                        @Override
                        public void onSuccess(CreateTeamResult param) {
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                } else {
                    YchatToastUtils.showShort("请选择至少一个联系人！");
                }
            }
        }
    }
}
