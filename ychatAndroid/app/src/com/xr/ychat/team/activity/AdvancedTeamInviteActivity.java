package com.xr.ychat.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.business.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.business.session.extension.TeamAuthAttachment;
import com.netease.nim.uikit.business.session.extension.TeamInviteAttachment;
import com.netease.nim.uikit.business.session.fragment.MessageFragment;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.CustomClickListener;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.xr.ychat.R;

import java.util.ArrayList;
import java.util.List;

public class AdvancedTeamInviteActivity extends SwipeBackUI {
    private static final String TEAM_MESSAGE = "TEAM_MESSAGE";
    private Toolbar mToolbar;
    private HeadImageView teamInviteAvatar;
    private TextView teamInviteName;
    private TextView teamInviteNumber;
    private TextView teamInviteTitle;
    private Button teamInviteAccept;
    private SystemMessage systemMessage;
    private IMMessage message;
    private String teamId;
    private String invitorId;
    private String name;
    private String teamName;
    private String creator;
    private TeamInviteAttachment inviteAttachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.nim_advanced_team_invite_activity);
        message = (IMMessage) getIntent().getSerializableExtra(TEAM_MESSAGE);
        inviteAttachment = (TeamInviteAttachment) message.getAttachment();
        teamId = inviteAttachment.getTeam_id();
        teamName = inviteAttachment.getTeam_name();
        invitorId = message.getSessionId();
        name = message.getFromAccount();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        findViews();
        teamInviteName.setText(teamName);
        teamInviteTitle.setText(String.format("\"%1$s\"邀请你加入群聊", UserInfoHelper.getUserName(invitorId)));
        teamInviteAccept.setText("加入群聊");
        if (message.getStatus() == MsgStatusEnum.read) {
            teamInviteAccept.setEnabled(false);
        } else {
            teamInviteAccept.setEnabled(true);
        }
        NIMClient.getService(TeamService.class).searchTeam(teamId).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team result) {
                if (result != null) {
                    creator = result.getCreator();
                    teamInviteAvatar.loadTeamIconByTeam(result);
                    String robotId = null;
                    if (!TextUtils.isEmpty(result.getExtension())) {
                        try {
                            Gson gson = new Gson();
                            TeamExtension extension = gson.fromJson(result.getExtension(), new TypeToken<TeamExtension>() {
                            }.getType());
                            robotId = extension.getRobotId();
                        } catch (Exception e) {

                        }
                    }
                    int memberNumber = result.getMemberCount() - (TextUtils.isEmpty(robotId) ? 0 : 1);
                    teamInviteNumber.setText(memberNumber + "人");
                }
            }

            @Override
            public void onFailed(int code) {
                YchatToastUtils.showShort("群组不存在");
                inviteMessageMiss();
            }

            @Override
            public void onException(Throwable exception) {
                YchatToastUtils.showShort("网络不可用");
            }
        });
        List<SystemMessageType> systemMessageTypes = new ArrayList<>();
        systemMessageTypes.add(SystemMessageType.TeamInvite);
        NIMClient.getService(SystemMessageService.class).querySystemMessageByType(systemMessageTypes, 0, 100).setCallback(new RequestCallback<List<SystemMessage>>() {
            @Override
            public void onSuccess(List<SystemMessage> param) {
                if (param != null && param.size() > 0) {
                    for (SystemMessage message : param) {
                        if (TextUtils.equals(message.getTargetId(), teamId) && message.getStatus() == SystemMessageStatus.init) {
                            systemMessage = message;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(Throwable exception) {
            }
        });
    }

    private void findViews() {
        teamInviteAvatar = (HeadImageView) findView(R.id.team_invite_avatar);
        teamInviteName = (TextView) findView(R.id.team_invite_name);
        teamInviteNumber = (TextView) findView(R.id.team_invite_number);
        teamInviteTitle = (TextView) findView(R.id.team_invite_title);
        teamInviteAccept = (Button) findView(R.id.team_invite_accept);
        teamInviteAccept.setOnClickListener(new CustomClickListener() {
            @Override
            protected void onSingleClick(View v) {
                if (systemMessage != null) {
                    NIMClient.getService(TeamService.class).acceptInvite(systemMessage.getTargetId(), systemMessage.getFromAccount()).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            updateInviteMessage();
                            if (TextUtils.equals(inviteAttachment.getInvitor_id(), creator)) {
                                sendTip3(name, NimUIKit.getAccount());
                            }
                            NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(systemMessage.getMessageId(), SystemMessageStatus.passed);
                            NimUIKit.startTeamSession(AdvancedTeamInviteActivity.this, teamId);
                            UpdateMemberChangeService.start(AdvancedTeamInviteActivity.this, NimUIKit.getAccount(), teamId, 1, message.getFromAccount());
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {
                            inviteMessageMiss();
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                } else {
                    inviteMessageMiss();
                }
            }
        });
    }

    private void sendTip3(String tipFromId, String tipToId) {
        TeamAuthAttachment authAttachment = new TeamAuthAttachment();
        authAttachment.setInviteTipType(TeamAuthAttachment.ACCEPT3);
        authAttachment.setInviteTipId(System.currentTimeMillis() / 1000L + NimUIKit.getAccount());//msgid:时间戳+accid(时间戳单位：秒)
        authAttachment.setInviteTipFromId(tipFromId);
        authAttachment.setInviteTipToId(tipToId);
        String fromName = TeamHelper.getTeamMemberDisplayNameYou(message.getSessionId(), tipFromId);
        String toName = "";
        String[] toIdArray = tipToId.split(",");
        if (toIdArray != null && toIdArray.length > 0) {
            for (int i = 0, len = toIdArray.length; i < len; i++) {
                String name = TeamHelper.getTeamMemberDisplayNameYou(message.getSessionId(), toIdArray[i]);
                if (i == len - 1) {
                    toName = toName + name;
                } else {
                    toName = toName + name + ",";
                }
            }
        }
        authAttachment.setInviteTipContent(" " + toName + "接受" + fromName + "的邀请进群 ");
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = false;
        config.enablePush = false;
        config.enableUnreadCount = false;
        IMMessage imMessage = MessageBuilder.createCustomMessage(teamId, SessionTypeEnum.Team, null, authAttachment, config);
        NIMClient.getService(MsgService.class).sendMessage(imMessage, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                List<Activity> activityList = ActivityUtils.getActivityList();
                for (Activity activity : activityList) {
                    if (activity instanceof TeamMessageActivity) {
                        TeamMessageActivity baseMessageActivity = (TeamMessageActivity) activity;
                        MessageFragment messageFragment = (MessageFragment) baseMessageActivity.getSupportFragmentManager().getFragments().get(0);
                        messageFragment.messageListPanel.onMsgSend(imMessage);
                        break;
                    }
                }
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(Throwable exception) {
            }
        });
    }

    private void updateInviteMessage() {
        List<Activity> activityList = ActivityUtils.getActivityList();
        for (Activity activity : activityList) {
            if (activity instanceof P2PMessageActivity) {
                P2PMessageActivity teamMessageActivity = (P2PMessageActivity) activity;
                MessageFragment messageFragment = (MessageFragment) teamMessageActivity.getSupportFragmentManager().getFragments().get(0);
                messageFragment.messageListPanel.updateInviteMessage(message);
                break;
            }
        }
    }

    private void inviteMessageMiss() {
        updateInviteMessage();
        teamInviteAccept.setText("加入群聊");
        teamInviteAccept.setEnabled(false);
    }

    public static void start(Context context, IMMessage message) {
        Intent intent = new Intent(context, AdvancedTeamInviteActivity.class);
        intent.putExtra(TEAM_MESSAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
