package com.xr.ychat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.common.ApplyLeaveTeam;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.xr.ychat.main.helper.CustomNotificationCache;
import com.xr.ychat.main.helper.SystemMessageUnreadManager;
import com.xr.ychat.main.reminder.ReminderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义通知消息广播接收器
 */
public class CustomNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Gson gson = new Gson();
        String action = context.getPackageName() + NimIntent.ACTION_RECEIVE_CUSTOM_NOTIFICATION;
        if (action.equals(intent.getAction())) {
            // 从intent中取出自定义通知
            CustomNotification notification = (CustomNotification) intent.getSerializableExtra(NimIntent.EXTRA_BROADCAST_MSG);
            try {
                ApplyLeaveTeam applyLeaveTeam = gson.fromJson(notification.getContent(), new TypeToken<ApplyLeaveTeam>() {
                }.getType());
                if (applyLeaveTeam.getId() == 2) {
                    // 加入缓存中
                    applyLeaveTeam.setHasRead(false);
                    notification.setContent(gson.toJson(applyLeaveTeam));
                    CustomNotificationCache.addCustomNotification(notification);
                    List<SystemMessageType> systemMessageTypes = new ArrayList<>();
                    systemMessageTypes.add(SystemMessageType.TeamInvite);
                    int teamInviteNumber = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountByType(systemMessageTypes);
                    if (teamInviteNumber > 0) {
                        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCountByType(systemMessageTypes);
                    }
                    int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock() - teamInviteNumber + CustomNotificationCache.getUnreadCount();
                    SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
                    ReminderManager.getInstance().updateContactUnreadNum(unread);
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                    Intent intent1 = new Intent("com.xr.ychat.LastNotificationReceiver");
                    localBroadcastManager.sendBroadcast(intent1);
                }
            } catch (Exception e) {
                LogUtil.e("demo", e.getMessage());
            }

        }
    }
}
