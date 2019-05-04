package com.netease.nim.uikit.business.recent.holder;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.CacheMemoryUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.recent.TeamMemberAitHelper;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.model.RecentContact;

public class TeamRecentViewHolder extends CommonRecentViewHolder {

    public TeamRecentViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    protected String getContent(RecentContact recent) {
        String content = descOfMsg(recent);

        String fromId = recent.getFromAccount();
        if (!TextUtils.isEmpty(fromId)
                && !fromId.equals(NimUIKit.getAccount())
                && !(recent.getAttachment() instanceof NotificationAttachment)) {
            String tid = recent.getContactId();
            String teamNick = getTeamUserDisplayName(tid, fromId);
            content = teamNick + ": " + content;

            if (TeamMemberAitHelper.hasAitExtension(recent)) {
                String cachedData=recent.getFromAccount()+descOfMsg(recent);
                if(cachedData.contains("@")){
                    //teamId,缓存@我的消息块，然后从最近会话列表点进去的时候，传到TeamMessageActiviy,然后再里面再取出消息块，有的话，则滑出“有人@你了”
                    CacheMemoryUtils cacheMemoryUtils=CacheMemoryUtils.getInstance();
                    if(TextUtils.isEmpty(cacheMemoryUtils.get(recent.getContactId()))){
                        cacheMemoryUtils.put(recent.getContactId(),cachedData);//key:teamId  value:发送者账号+消息内容
                        Log.e("xx","缓存的at数据："+cachedData);
                    }else{
                        Log.e("xx","原来存在@的数据了，不需要缓存此条了："+cachedData);
                    }

                }

                if (recent.getUnreadCount() == 0) {
                    TeamMemberAitHelper.clearRecentContactAited(recent);
                } else {
                    content = TeamMemberAitHelper.getAitAlertString(content);
                }
            }
        }

        return content;
    }

    private String getTeamUserDisplayName(String tid, String account) {
        return TeamHelper.getTeamMemberDisplayName(tid, account);
    }
}
