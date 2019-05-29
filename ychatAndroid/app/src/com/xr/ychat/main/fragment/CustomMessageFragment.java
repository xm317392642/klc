package com.xr.ychat.main.fragment;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ApplyLeaveTeam;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.listview.AutoRefreshListView;
import com.netease.nim.uikit.common.ui.listview.MessageListView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.xr.ychat.R;
import com.xr.ychat.contact.activity.SystemMessageDetailActivity;
import com.xr.ychat.contact.activity.UserProfileActivity;
import com.xr.ychat.main.adapter.CustomMessageAdapter;
import com.xr.ychat.main.helper.CustomNotificationCache;
import com.xr.ychat.main.helper.SystemMessageUnreadManager;
import com.xr.ychat.main.reminder.ReminderManager;
import com.xr.ychat.main.viewholder.CustomNotificationViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义通知
 * <p/>
 * Created by huangjun on 2015/5/28
 */
public class CustomMessageFragment extends Fragment implements TAdapterDelegate, CustomNotificationViewHolder.CustomNotificationListener {

    // view
    private MessageListView listView;

    // adapter
    private CustomMessageAdapter adapter;
    private List<CustomNotification> items = new ArrayList<>();
    private Gson gson;

    private LocalBroadcastManager localBroadcastManager;
    private ClearNotificationReceiver receiver;
    private DisposeCustomNotificationReceiver disposeCustomNotificationReceiver;
    private LastNotificationReceiver lastNotificationReceiver;

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return CustomNotificationViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    public static CustomMessageFragment newInstance() {
        CustomMessageFragment fragment = new CustomMessageFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.system_notification_message_activity, container, false);
        listView = view.findViewById(R.id.messageListView);
        return listView;
    }

    //主页传来清空消息
    private class ClearNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            deleteAllMessages();
        }
    }

    //消息详情传来处理方式同意或者拒绝
    private class DisposeCustomNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean status = intent.getBooleanExtra(SystemMessageDetailActivity.EXTRA_STATUS, false);
            CustomNotification data = (CustomNotification) intent.getSerializableExtra(SystemMessageDetailActivity.EXTRA_DATA);
            if (status) {
                onAgree(data);
            } else {
                onReject(data);
            }
        }
    }

    private class LastNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<CustomNotification> datas = CustomNotificationCache.getDataList();
            for (CustomNotification customNotification : datas) {
                if (!TextUtils.isEmpty(customNotification.getContent())) {
                    ApplyLeaveTeam leaveTeam = gson.fromJson(customNotification.getContent(), new TypeToken<ApplyLeaveTeam>() {
                    }.getType());
                    if (!leaveTeam.isHasRead()) {
                        leaveTeam.setHasRead(true);
                        customNotification.setContent(gson.toJson(leaveTeam));
                    }
                }
            }
            CustomNotificationCache.setDataList(datas);
            SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(0);
            ReminderManager.getInstance().updateContactUnreadNum(0);
            items.clear();
            items.addAll(datas);
            refresh();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gson = new Gson();
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        receiver = new ClearNotificationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xr.ychat.ClearNotificationReceiver");
        localBroadcastManager.registerReceiver(receiver, intentFilter);

        disposeCustomNotificationReceiver = new DisposeCustomNotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xr.ychat.DisposeCustomNotificationReceiver");
        localBroadcastManager.registerReceiver(disposeCustomNotificationReceiver, filter);

        lastNotificationReceiver = new LastNotificationReceiver();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("com.xr.ychat.LastNotificationReceiver");
        localBroadcastManager.registerReceiver(lastNotificationReceiver, intentFilter1);

        initAdapter();
        initListView();
        loadData(); // load old data
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(receiver);
        localBroadcastManager.unregisterReceiver(disposeCustomNotificationReceiver);
        localBroadcastManager.unregisterReceiver(lastNotificationReceiver);
    }

    private void initAdapter() {
        adapter = new CustomMessageAdapter(getContext(), items, this, this);
    }

    private void initListView() {
        listView.setMode(AutoRefreshListView.Mode.END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        // adapter
        listView.setAdapter(adapter);
    }


    private void loadData() {
        List<CustomNotification> cache = CustomNotificationCache.getDataList();
        if (!cache.isEmpty()) {
            items.addAll(cache);
        }
        refresh();
    }

    private void refresh() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAgree(CustomNotification message) {
        String content = message.getContent();
        ApplyLeaveTeam applyLeaveTeam = gson.fromJson(content, new TypeToken<ApplyLeaveTeam>() {
        }.getType());
        TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(applyLeaveTeam.getLeaveTeamID(), NimUIKit.getAccount());
        if (teamMember.getType() == TeamMemberType.Owner) {
            CustomNotification command = new CustomNotification();
            command.setFromAccount(NimUIKit.getAccount());
            command.setSessionId(message.getSessionId());
            command.setSessionType(SessionTypeEnum.P2P);
            applyLeaveTeam.setTeamLeaveType(1);
            applyLeaveTeam.setHasRead(true);
            applyLeaveTeam.setContent(UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()) + "[已同意退群申请]");
            String newContent = gson.toJson(applyLeaveTeam);
            int index = items.indexOf(message);
            message.setContent(newContent);
            items.set(index, message);
            processItems(message, applyLeaveTeam.getLeaveTeamID(), index);
            CustomNotificationCache.setDataList(items);
            adapter.notifyDataSetChanged();
            applyLeaveTeam.setTeamLeaveType(3);
            command.setContent(gson.toJson(applyLeaveTeam));
            command.setApnsText(UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()) + "[已同意退群申请]");
            command.setSendToOnlineUserOnly(false);
            NIMClient.getService(MsgService.class).sendCustomNotification(command);
            ArrayList<String> accounts = new ArrayList<>();
            accounts.add(message.getSessionId());
            NIMClient.getService(TeamService.class).removeMembers(applyLeaveTeam.getLeaveTeamID(), accounts).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    UpdateMemberChangeService.start(getContext(), createMembersString(accounts), applyLeaveTeam.getLeaveTeamID(), 2);
                }

                @Override
                public void onFailed(int code) {
                    if (code == ResponseCode.RES_TEAM_ENACCESS) {
                        YchatToastUtils.showShort( "没有权限删除群成员");
                    }
                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        } else {
            int index = items.indexOf(message);
            applyLeaveTeam.setTeamLeaveType(5);
            applyLeaveTeam.setHasRead(true);
            message.setContent(gson.toJson(applyLeaveTeam));
            items.set(index, message);
            processItems(message, applyLeaveTeam.getLeaveTeamID(), index);
            CustomNotificationCache.setDataList(items);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onReject(CustomNotification message) {
        String content = message.getContent();
        ApplyLeaveTeam applyLeaveTeam = gson.fromJson(content, new TypeToken<ApplyLeaveTeam>() {
        }.getType());
        TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(applyLeaveTeam.getLeaveTeamID(), NimUIKit.getAccount());
        if (teamMember.getType() == TeamMemberType.Owner) {
            CustomNotification command = new CustomNotification();
            command.setFromAccount(NimUIKit.getAccount());
            command.setSessionId(message.getSessionId());
            command.setSessionType(SessionTypeEnum.P2P);
            applyLeaveTeam.setTeamLeaveType(2);
            applyLeaveTeam.setHasRead(true);
            applyLeaveTeam.setContent(UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()) + "[已拒绝退群申请]");
            String newContent = gson.toJson(applyLeaveTeam);
            int index = items.indexOf(message);
            message.setContent(newContent);
            items.set(index, message);
            processItems(message, applyLeaveTeam.getLeaveTeamID(), index);
            CustomNotificationCache.setDataList(items);
            adapter.notifyDataSetChanged();
            applyLeaveTeam.setTeamLeaveType(4);
            command.setContent(gson.toJson(applyLeaveTeam));
            command.setApnsText(UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()) + "[已拒绝退群申请]");
            command.setSendToOnlineUserOnly(false);
            NIMClient.getService(MsgService.class).sendCustomNotification(command);
        } else {
            int index = items.indexOf(message);
            applyLeaveTeam.setTeamLeaveType(5);
            applyLeaveTeam.setHasRead(true);
            message.setContent(gson.toJson(applyLeaveTeam));
            items.set(index, message);
            processItems(message, applyLeaveTeam.getLeaveTeamID(), index);
            CustomNotificationCache.setDataList(items);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(CustomNotification message) {
        ApplyLeaveTeam applyLeaveTeam = gson.fromJson(message.getContent(), new TypeToken<ApplyLeaveTeam>() {
        }.getType());
        if (applyLeaveTeam.getTeamLeaveType() == 0) {
            SystemMessageDetailActivity.start(getContext(), message);
        } else {
            UserProfileActivity.start(getContext(), message.getFromAccount());
        }
    }

    private void processItems(CustomNotification message, String leaveTeamID, int index) {
        for (int i = 0; i < items.size(); i++) {
            if (i != index) {
                CustomNotification customNotification = items.get(i);
                if (TextUtils.equals(customNotification.getSessionId(), message.getSessionId())) {
                    ApplyLeaveTeam leaveTeam = gson.fromJson(customNotification.getContent(), new TypeToken<ApplyLeaveTeam>() {
                    }.getType());
                    if (TextUtils.equals(leaveTeamID, leaveTeam.getLeaveTeamID()) && leaveTeam.getTeamLeaveType() == 0) {
                        leaveTeam.setTeamLeaveType(5);
                        leaveTeam.setHasRead(true);
                        customNotification.setContent(gson.toJson(leaveTeam));
                    }
                }
            }
        }
    }

    private String createMembersString(ArrayList<String> accounts) {
        if (accounts == null || accounts.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int selectedNumber = accounts.size();
        for (int i = 0; i < selectedNumber; i++) {
            builder.append(accounts.get(i));
            if (i != selectedNumber - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private void deleteAllMessages() {
        CustomNotificationCache.clearCustomNotification();
        items.clear();
        refresh();
        YchatToastUtils.showShort(R.string.clear_all_success);
    }

}