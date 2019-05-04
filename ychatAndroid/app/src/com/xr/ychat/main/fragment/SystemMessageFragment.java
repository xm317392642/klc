package com.xr.ychat.main.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.listview.AutoRefreshListView;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.ui.listview.MessageListView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.R;
import com.xr.ychat.contact.activity.SystemMessageDetailActivity;
import com.xr.ychat.contact.activity.UserProfileActivity;
import com.xr.ychat.main.adapter.SystemMessageAdapter;
import com.xr.ychat.main.helper.MessageHelper;
import com.xr.ychat.main.viewholder.SystemMessageViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemMessageFragment extends Fragment implements TAdapterDelegate,
        AutoRefreshListView.OnRefreshListener, SystemMessageViewHolder.SystemMessageListener {

    private static final boolean MERGE_ADD_FRIEND_VERIFY = false;

    private static final int LOAD_MESSAGE_COUNT = 10;

    // view
    private MessageListView listView;

    // adapter
    private SystemMessageAdapter adapter;
    private List<SystemMessage> items = new ArrayList<>();
    private Set<Long> itemIds = new HashSet<>();

    // db
    private boolean firstLoad = true;
    private Activity activity;
    private LocalBroadcastManager localBroadcastManager;
    private ClearNotificationReceiver receiver;
    private DisposeSystemMessageReceiver disposeSystemMessageReceiver;

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return SystemMessageViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }


    @Override
    public void onRefreshFromStart() {
    }

    @Override
    public void onRefreshFromEnd() {
        loadMessages(); // load old data
    }

    public static SystemMessageFragment newInstance() {
        SystemMessageFragment fragment = new SystemMessageFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.system_notification_message_activity, container, false);
        listView = view.findViewById(R.id.messageListView);
        return listView;
    }

    private class ClearNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            deleteAllMessages();
        }
    }

    private class DisposeSystemMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean status = intent.getBooleanExtra(SystemMessageDetailActivity.EXTRA_STATUS, false);
            SystemMessage data = (SystemMessage) intent.getSerializableExtra(SystemMessageDetailActivity.EXTRA_DATA);
            if (status) {
                onAgree(data);
            } else {
                onReject(data);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        receiver = new ClearNotificationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xr.ychat.ClearNotificationReceiver");
        localBroadcastManager.registerReceiver(receiver, intentFilter);
        disposeSystemMessageReceiver = new DisposeSystemMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xr.ychat.DisposeSystemMessageReceiver");
        localBroadcastManager.registerReceiver(disposeSystemMessageReceiver, filter);
        initAdapter();
        initListView();
        loadMessages(); // load old data
        registerSystemObserver(true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(receiver);
        localBroadcastManager.unregisterReceiver(disposeSystemMessageReceiver);
        registerSystemObserver(false);
    }

    private void initAdapter() {
        adapter = new SystemMessageAdapter(getContext(), items, this, this);
    }

    private void initListView() {
        listView.setMode(AutoRefreshListView.Mode.END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        // adapter
        listView.setAdapter(adapter);

        // listener
        listView.setOnRefreshListener(this);
    }


    private int loadOffset = 0;
    private Set<String> addFriendVerifyRequestAccounts = new HashSet<>(); // 发送过好友申请的账号（好友申请合并用）

    /**
     * 加载历史消息
     */
    public void loadMessages() {
        listView.onRefreshStart(AutoRefreshListView.Mode.END);
        boolean loadCompleted; // 是否已经加载完成，后续没有数据了or已经满足本次请求数量
        int validMessageCount = 0; // 实际加载的数量（排除被过滤被合并的条目）
        List<String> messageFromAccounts = new ArrayList<>(LOAD_MESSAGE_COUNT);

        while (true) {
            List<SystemMessage> temps = NIMClient.getService(SystemMessageService.class).querySystemMessagesBlock(loadOffset, LOAD_MESSAGE_COUNT);
            loadOffset += temps.size();
            loadCompleted = temps.size() < LOAD_MESSAGE_COUNT;
            int tempValidCount = 0;
            for (SystemMessage m : temps) {
                // 去重
                if (duplicateFilter(m)) {
                    continue;
                }
                // 同一个账号的好友申请仅保留最近一条
                if (addFriendVerifyFilter(m)) {
                    continue;
                }
                // 保存有效消息
                items.add(m);
                tempValidCount++;
                if (!messageFromAccounts.contains(m.getFromAccount())) {
                    messageFromAccounts.add(m.getFromAccount());
                }
                // 判断是否达到请求数
                if (++validMessageCount >= LOAD_MESSAGE_COUNT) {
                    loadCompleted = true;
                    // 已经满足要求，此时需要修正loadOffset
                    loadOffset -= temps.size();
                    loadOffset += tempValidCount;
                    break;
                }
            }
            if (loadCompleted) {
                break;
            }
        }
        // 更新数据源，刷新界面
        refresh();
        boolean first = firstLoad;
        firstLoad = false;
        if (first) {
            ListViewUtil.scrollToPosition(listView, 0, 0); // 第一次加载后显示顶部
        }
        listView.onRefreshComplete(validMessageCount, LOAD_MESSAGE_COUNT, true);
        // 收集未知用户资料的账号集合并从远程获取
        collectAndRequestUnknownUserInfo(messageFromAccounts);
    }

    /**
     * 新消息到来
     */
    private void onIncomingMessage(final SystemMessage message) {
        // 同一个账号的好友申请仅保留最近一条
        if (addFriendVerifyFilter(message)) {
            SystemMessage del = null;
            for (SystemMessage m : items) {
                if (m.getFromAccount().equals(message.getFromAccount()) && m.getType() == SystemMessageType.AddFriend) {
                    AddFriendNotify attachData = (AddFriendNotify) m.getAttachObject();
                    if (attachData != null && attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                        del = m;
                        break;
                    }
                }
            }
            if (del != null) {
                items.remove(del);
            }
        }
        loadOffset++;
        items.add(0, message);
        refresh();
        // 收集未知用户资料的账号集合并从远程获取
        List<String> messageFromAccounts = new ArrayList<>(1);
        messageFromAccounts.add(message.getFromAccount());
        collectAndRequestUnknownUserInfo(messageFromAccounts);
    }


    // 去重
    private boolean duplicateFilter(final SystemMessage msg) {
        if (itemIds.contains(msg.getMessageId())) {
            return true;
        }
        itemIds.add(msg.getMessageId());
        return false;
    }

    // 同一个账号的好友申请仅保留最近一条
    private boolean addFriendVerifyFilter(final SystemMessage msg) {
        if (!MERGE_ADD_FRIEND_VERIFY) {
            return false; // 不需要MERGE，不过滤
        }
        if (msg.getType() != SystemMessageType.AddFriend) {
            return false; // 不过滤
        }
        AddFriendNotify attachData = (AddFriendNotify) msg.getAttachObject();
        if (attachData == null) {
            return true; // 过滤
        }
        if (attachData.getEvent() != AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
            return false; // 不过滤
        }
        if (addFriendVerifyRequestAccounts.contains(msg.getFromAccount())) {
            return true; // 过滤
        }
        addFriendVerifyRequestAccounts.add(msg.getFromAccount());
        return false; // 不过滤
    }

    // 请求未知的用户资料
    private void collectAndRequestUnknownUserInfo(List<String> messageFromAccounts) {
        List<String> unknownAccounts = new ArrayList<>();
        for (String account : messageFromAccounts) {
            if (NimUIKit.getUserInfoProvider().getUserInfo(account) == null) {
                unknownAccounts.add(account);
            }
        }
        if (!unknownAccounts.isEmpty()) {
            requestUnknownUser(unknownAccounts);
        }
    }

    @Override
    public void onAgree(SystemMessage message) {
        onSystemNotificationDeal(message, true);
    }

    @Override
    public void onReject(SystemMessage message) {
        onSystemNotificationDeal(message, false);
    }

    @Override
    public void onLongPressed(SystemMessage message) {
        showLongClickMenu(message);
    }

    @Override
    public void onItemClick(SystemMessage message) {
        if (message.getStatus() == SystemMessageStatus.init && MessageHelper.isVerifyMessageNeedDeal(message)) {
            SystemMessageDetailActivity.start(getContext(), message);
        } else {
            UserProfileActivity.start(getContext(), message.getFromAccount());
        }
    }

    private void onSystemNotificationDeal(final SystemMessage message, final boolean pass) {
        RequestCallback callback = new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                onProcessSuccess(pass, message);
            }

            @Override
            public void onFailed(int code) {
                onProcessFailed(code, message);
            }

            @Override
            public void onException(Throwable exception) {

            }
        };
        if (message.getType() == SystemMessageType.TeamInvite) {
            if (pass) {
                NIMClient.getService(TeamService.class).acceptInvite(message.getTargetId(), message.getFromAccount()).setCallback(callback);
            } else {
                NIMClient.getService(TeamService.class).declineInvite(message.getTargetId(), message.getFromAccount(), "").setCallback(callback);
            }

        } else if (message.getType() == SystemMessageType.ApplyJoinTeam) {
            if (pass) {
                NIMClient.getService(TeamService.class).passApply(message.getTargetId(), message.getFromAccount()).setCallback(callback);
            } else {
                NIMClient.getService(TeamService.class).rejectApply(message.getTargetId(), message.getFromAccount(), "").setCallback(callback);
            }
        } else if (message.getType() == SystemMessageType.AddFriend) {
            NIMClient.getService(FriendService.class).ackAddFriendRequest(message.getFromAccount(), pass).setCallback(callback);
        }
    }

    private void onProcessSuccess(final boolean pass, SystemMessage message) {
        if (pass && message.getType() == SystemMessageType.AddFriend) {
            IMMessage msg = MessageBuilder.createTipMessage(message.getFromAccount(), SessionTypeEnum.P2P);
            msg.setContent("我们已经是好友，现在可以开始聊天了");
            CustomMessageConfig config = new CustomMessageConfig();
            config.enablePush = false; // 不推送
            config.enableUnreadCount = false;
            msg.setConfig(config);
            msg.setStatus(MsgStatusEnum.success);
            NIMClient.getService(MsgService.class).sendMessage(msg, false);
        }
        if (pass && message.getType() == SystemMessageType.ApplyJoinTeam) {
            UpdateMemberChangeService.start(getContext(), message.getFromAccount(), message.getTargetId(), 1, "qr");
        }
        if (pass && message.getType() == SystemMessageType.TeamInvite) {
            UpdateMemberChangeService.start(getContext(), NimUIKit.getAccount(), message.getTargetId(), 1, message.getFromAccount());
        }
        for (SystemMessage systemMessage : items) {
            if (systemMessage.getMessageId() != message.getMessageId()) {
                if (systemMessage.getStatus() == SystemMessageStatus.init &&
                        systemMessage.getType() == message.getType() &&
                        TextUtils.equals(systemMessage.getFromAccount(), message.getFromAccount())
                        && TextUtils.equals(systemMessage.getTargetId(), message.getTargetId())) {
                    SystemMessageStatus systemMessageStatus = SystemMessageStatus.expired;
                    NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(systemMessage.getMessageId(), systemMessageStatus);
                    systemMessage.setStatus(systemMessageStatus);
                    refreshViewHolder(systemMessage);
                }
            } else {
                SystemMessageStatus status = pass ? SystemMessageStatus.passed : SystemMessageStatus.declined;
                NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(), status);
                message.setStatus(status);
                refreshViewHolder(message);
            }
        }
    }

    private void onProcessFailed(final int code, SystemMessage message) {
        if (code == 509) {
            YchatToastUtils.showShort( "请求已经失效");
        } else {
            YchatToastUtils.showShort( "请求失败");
        }
        if (code == 408) {
            return;
        }

        SystemMessageStatus status = SystemMessageStatus.expired;
        NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(), status);
        message.setStatus(status);
        refreshViewHolder(message);
    }

    private void refresh() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshViewHolder(final SystemMessage message) {
        final long messageId = message.getMessageId();

        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            SystemMessage item = items.get(i);
            if (messageId == item.getMessageId()) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            return;
        }

        final int m = index;
        listView.post(new Runnable() {
            @Override
            public void run() {
                if (m < 0) {
                    return;
                }

                Object tag = ListViewUtil.getViewHolderByIndex(listView, m);
                if (tag instanceof SystemMessageViewHolder) {
                    ((SystemMessageViewHolder) tag).refreshDirectly(message);
                }
            }
        });
    }

    private void registerSystemObserver(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(systemMessageObserver, register);
    }

    Observer<SystemMessage> systemMessageObserver = new Observer<SystemMessage>() {
        @Override
        public void onEvent(SystemMessage systemMessage) {
            onIncomingMessage(systemMessage);
        }
    };

    private void requestUnknownUser(List<String> accounts) {
        NimUIKit.getUserInfoProvider().getUserInfoAsync(accounts, new SimpleCallback<List<NimUserInfo>>() {
            @Override
            public void onResult(boolean success, List<NimUserInfo> result, int code) {
                if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && !result.isEmpty()) {
                        refresh();
                    }
                }
            }
        });
    }

    private void deleteAllMessages() {
        NIMClient.getService(SystemMessageService.class).clearSystemMessages();
        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
        items.clear();
        refresh();
        YchatToastUtils.showShort(R.string.clear_all_success);
    }

    private void showLongClickMenu(final SystemMessage message) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(getContext());
        alertDialog.setTitle(R.string.delete_tip);
        String title = getString(R.string.delete_system_message);
        alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                deleteSystemMessage(message);
            }
        });
        alertDialog.show();
    }

    private void deleteSystemMessage(final SystemMessage message) {
        NIMClient.getService(SystemMessageService.class).deleteSystemMessage(message.getMessageId());
        items.remove(message);
        refresh();
        YchatToastUtils.showShort( R.string.delete_success);
    }
}
