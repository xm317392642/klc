package com.netease.nim.uikit.business.session.fragment;

import android.content.Intent;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.UpdateMemberChatTimeService;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public class TeamMessageFragment extends MessageFragment {

    private Team team;

    @Override
    public boolean isAllowSendMessage(IMMessage message) {
        if (team == null) {
            team = NimUIKit.getTeamProvider().getTeamById(sessionId);
        }

        if (team == null || !team.isMyTeam()) {
            YchatToastUtils.showShort(R.string.team_send_message_not_allow);
            return false;
        }

        return super.isAllowSendMessage(message);
    }

    public void setTeam(Team team) {
        this.team = team;
        NimUIKit.getTeamProvider().fetchTeamMember(team.getId(), NimUIKit.getAccount(), new SimpleCallback<TeamMember>() {
            @Override
            public void onResult(boolean success, TeamMember result, int code) {
                if (success && result != null) {
                    if (result.getType() == TeamMemberType.Manager || result.getType() == TeamMemberType.Owner) {
                        inputPanel.switchMuteBackground(false);
                    } else {
                        inputPanel.switchMuteBackground(team.isAllMute());
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //String teamID = team.getId();
        int dataSize = messageListPanel.adapter.getDataSize();
        IMMessage message = messageListPanel.getUserLastSendMessage();
        if (message != null) {
            Intent service = new Intent(getContext(), UpdateMemberChatTimeService.class);
            service.putExtra(UpdateMemberChatTimeService.EXTRA_ACCOUNT, Preferences.getWeiranUid(getContext()));
            service.putExtra(UpdateMemberChatTimeService.EXTRA_TOKEN, Preferences.getWeiranToken(getContext()));
            service.putExtra(UpdateMemberChatTimeService.EXTRA_TIME, message.getTime() / 1000L);
            service.putExtra(UpdateMemberChatTimeService.EXTRA_TEAM, sessionId);
            getContext().startService(service);
        }
        if (dataSize > 0) {
            NimUIKit.getTeamProvider().fetchTeamMember(sessionId, NimUIKit.getAccount(), (success, teamMember, code) -> {
                if (success && teamMember != null) {
                    Map<String, Object> map = teamMember.getExtension();
                    MsgService msgService = NIMClient.getService(MsgService.class);
                    //如果阅后即焚开启的话
                    if (TeamExtras.OPEN.equals(map.get(TeamExtras.FIRE_MSG))) {
                        msgService.clearChattingHistory(sessionId, SessionTypeEnum.Team);
                    }
                    //如果48小时自动清理开启的话
                    if (TeamExtras.OPEN.equals(map.get(TeamExtras.AUTO_CLEAR))) {
                        //TODO
                        long hour_48 = 48 * 60 * 60 * 1000;
                        //long hour_48 = 20 * 60 * 1000;
                        // 查询anchor往前48小时的数据，200条(查询范围由 toTime 和 limit 共同决定，以先到为准。如果到 toTime 之间消息大于 limit 条，返回 limit 条记录，如果小于 limit 条，返回实际条数。)
                        //IMMessage anchor = messageListPanel.adapter.getItem(messageListPanel.adapter.getDataSize() - 1);//消息列表最后一条消息
                        IMMessage anchorStart = MessageBuilder.createEmptyMessage(sessionId, SessionTypeEnum.Team, System.currentTimeMillis() - hour_48);//48小时之前的消息锚点，起始时间
                        msgService.queryMessageListEx(anchorStart, QueryDirectionEnum.QUERY_OLD, 200, true).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
                            @Override
                            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                                if (result != null) {
                                    for (IMMessage message : result) {
                                        msgService.deleteChattingHistory(message);// 删除单条消息
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }

    }
}