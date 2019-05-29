package com.xr.ychat.session.viewholder;

import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.extension.TeamInviteAttachment;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.xr.ychat.R;
import com.xr.ychat.team.activity.AdvancedTeamInviteActivity;

public class MsgViewHolderTeamInvite extends MsgViewHolderBase {
    private HeadImageView inviteGroup;
    private TextView inviteTitle;
    private TextView inviteMessage;
    private LinearLayout constraintLayout;

    public MsgViewHolderTeamInvite(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        if (TextUtils.equals(message.getFromAccount(), NimUIKit.getAccount())) {
            return R.layout.team_invite_chat_self_item;
        } else {
            return R.layout.team_invite_chat_item;
        }
    }

    @Override
    protected void inflateContentView() {
        inviteGroup = (HeadImageView) view.findViewById(R.id.team_invite_avatar);
        inviteMessage = (TextView) view.findViewById(R.id.team_invite_content);
        inviteTitle = (TextView) view.findViewById(R.id.team_invite_title);
        constraintLayout = (LinearLayout) view.findViewById(R.id.team_invite_layout);
    }

    @Override
    protected void bindContentView() {
        if (message.getAttachment() == null) {
            return;
        }
        TeamInviteAttachment inviteAttachment = (TeamInviteAttachment) message.getAttachment();
        String teamId = inviteAttachment.getTeam_id();
        String teamName = inviteAttachment.getTeam_name();
        String userName = UserInfoHelper.getUserName(message.getSessionId());
        if (TextUtils.equals(message.getFromAccount(), NimUIKit.getAccount())) {
            constraintLayout.setBackgroundResource(R.drawable.nim_message_item_right_bc);
            inviteTitle.setText(String.format("你邀请\"%1$s\"加入群聊", userName));
            String content = String.format("你邀请\"%1$s\"加入群聊\"%2$s\"", userName, teamName);
            inviteMessage.setText(content);
        } else {
            constraintLayout.setBackgroundResource(R.drawable.nim_message_item_left_bc);
            inviteTitle.setText("邀请你加入群聊");
            String content = String.format("\"%1$s\"邀请你加入群聊\"%2$s\"，进入可查看详情", userName, teamName);
            inviteMessage.setText(content);
        }
        NIMClient.getService(TeamService.class).searchTeam(teamId).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team result) {
                inviteGroup.loadTeamIconByTeam(result);
            }

            @Override
            public void onFailed(int code) {
                inviteGroup.setImageResource(R.drawable.nim_avatar_group);
            }

            @Override
            public void onException(Throwable exception) {
                inviteGroup.setImageResource(R.drawable.nim_avatar_group);
            }
        });
    }

    @Override
    protected void onItemClick() {
        TeamInviteAttachment inviteAttachment = (TeamInviteAttachment) message.getAttachment();
        if (!TextUtils.equals(message.getFromAccount(), NimUIKit.getAccount())) {
            if (TeamHelper.isTeamMember(inviteAttachment.getTeam_id(), NimUIKit.getAccount())) {
                NimUIKit.startTeamSession(context, inviteAttachment.getTeam_id());
            } else {
                AdvancedTeamInviteActivity.start(context, message);
            }
        }
    }

    @Override
    protected int leftBackground() {
        return 0;
    }

    @Override
    protected int rightBackground() {
        return 0;
    }
}
