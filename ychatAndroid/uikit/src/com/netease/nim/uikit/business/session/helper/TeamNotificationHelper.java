package com.netease.nim.uikit.business.session.helper;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.business.session.extension.CustomAttachment;
import com.netease.nim.uikit.business.session.extension.CustomAttachmentType;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamAllMuteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.MuteMemberAttachment;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;

import java.util.List;
import java.util.Map;

/**
 * 系统消息描述文本构造器。主要是将各个系统消息转换为显示的文本内容。<br>
 * Created by huangjun on 2015/3/11.
 */
public class TeamNotificationHelper {
    private static ThreadLocal<String> teamId = new ThreadLocal<>();

    public static String getMsgShowText(final IMMessage message) {
        String content = "";
        String messageTip = message.getMsgType().getSendMessageTip();
        if (message.getMsgType() == MsgTypeEnum.custom) {
            CustomAttachment msgAttachment = (CustomAttachment) message.getAttachment();
            switch (msgAttachment.getType()) {
                case CustomAttachmentType.TEAM_AUTHENTICATION:
                    messageTip = "群认证消息";
                    break;
                case CustomAttachmentType.TeamInvite:
                    messageTip = "邀请你入群";
                    break;
                case CustomAttachmentType.Mahjong:
                    messageTip = "机器人消息";
                    break;
                case CustomAttachmentType.GameShare:
                    messageTip = "游戏分享";
                    break;
                case CustomAttachmentType.BusinessCard:
                    messageTip = "个人名片";
                    break;
                case CustomAttachmentType.SnapChat:
                    messageTip = "阅后即焚";
                    break;
                case CustomAttachmentType.CustomMessageTypeCustomTip:
                    messageTip = "截屏消息";
                    break;
                case CustomAttachmentType.OpenedRedPacket:
                case CustomAttachmentType.RedPacket:
                    messageTip = "红包消息";
                    break;
                default:
                    return "自定义消息";
            }
        }

        if (messageTip.length() > 0) {
            content += "[" + messageTip + "]";
        } else {
            if (message.getSessionType() == SessionTypeEnum.Team && message.getAttachment() != null) {
                content += getTeamNotificationText(message, message.getSessionId());
            } else {
                content += message.getContent();
            }
        }

        return content;
    }

    public static String getTeamNotificationText(IMMessage message, String tid) {
        return getTeamNotificationText(message.getSessionId(), message.getFromAccount(), (NotificationAttachment) message.getAttachment());
    }

    public static String getTeamNotificationText(String tid, String fromAccount, NotificationAttachment attachment) {
        teamId.set(tid);
        String text = buildNotification(tid, fromAccount, attachment);
        teamId.set(null);
        return text;
    }

    private static String buildNotification(String tid, String fromAccount, NotificationAttachment attachment) {
        String text;
        switch (attachment.getType()) {
            case InviteMember://邀请群成员发起的一个tip
                text = buildInviteMemberNotification(((MemberChangeAttachment) attachment), fromAccount);
                break;
            case KickMember:
                text = buildKickMemberNotification(((MemberChangeAttachment) attachment), fromAccount);
                break;
            case LeaveTeam:
                text = buildLeaveTeamNotification(fromAccount);
                break;
            case DismissTeam:
                text = buildDismissTeamNotification(fromAccount);
                break;
            case UpdateTeam:
                text = buildUpdateTeamNotification(tid, fromAccount, (UpdateTeamAttachment) attachment);
                break;
            case PassTeamApply:
                text = buildManagerPassTeamApplyNotification(fromAccount, (MemberChangeAttachment) attachment);
                break;
            case TransferOwner:
                text = buildTransferOwnerNotification(fromAccount, (MemberChangeAttachment) attachment);
                break;
            case AddTeamManager:
                text = buildAddTeamManagerNotification((MemberChangeAttachment) attachment);
                break;
            case RemoveTeamManager:
                text = buildRemoveTeamManagerNotification((MemberChangeAttachment) attachment);
                break;
            case AcceptInvite:
                text = buildAcceptInviteNotification(fromAccount, (MemberChangeAttachment) attachment);
                break;
            case MuteTeamMember:
                text = buildMuteTeamNotification((MuteMemberAttachment) attachment);
                break;
            default:
                text = getTeamMemberDisplayName(fromAccount) + ": unknown message";
                break;
        }

        return text;
    }

    private static String getTeamMemberDisplayName(String account) {
        return TeamHelper.getTeamMemberDisplayNameYou(teamId.get(), account);
    }

    private static String buildMemberListString(List<String> members, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        for (String account : members) {
            if (!TextUtils.isEmpty(fromAccount) && fromAccount.equals(account)) {
                continue;
            }
            sb.append(getTeamMemberDisplayName(account));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private static String buildInviteMemberNotification(MemberChangeAttachment a, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        String selfName = getTeamMemberDisplayName(fromAccount);

        sb.append(selfName);
        sb.append("邀请 ");
        sb.append(buildMemberListString(a.getTargets(), fromAccount));
        sb.append(" 加入群");

        return sb.toString();
    }

    private static String buildKickMemberNotification(MemberChangeAttachment a, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTeamMemberDisplayName(fromAccount));
        sb.append("将 ");
        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append("移出了群");
        return sb.toString();
    }

    private static String buildLeaveTeamNotification(String fromAccount) {
        String tip = " 离开了群";
        return getTeamMemberDisplayName(fromAccount) + tip;
    }

    private static String buildDismissTeamNotification(String fromAccount) {
        return getTeamMemberDisplayName(fromAccount) + " 解散了群";
    }

    private static String buildUpdateTeamNotification(String tid, String account, UpdateTeamAttachment a) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TeamFieldEnum, Object> field : a.getUpdatedFields().entrySet()) {
            if (field.getKey() == TeamFieldEnum.Name) {
                sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "更新了群名称");
            } else if (field.getKey() == TeamFieldEnum.Introduce) {
                sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "更新了群介绍");
            } else if (field.getKey() == TeamFieldEnum.Announcement) {
                sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "更新了群公告");
            } else if (field.getKey() == TeamFieldEnum.VerifyType) {
                sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "更新了二维码进群验证方式");
            } else if (field.getKey() == TeamFieldEnum.Extension) {
                try {
                    Gson gson = new Gson();
                    TeamExtension extension = gson.fromJson(field.getValue().toString(), new TypeToken<TeamExtension>() {
                    }.getType());
                    int extensionType = extension.getExtensionType();
                    if (extensionType == 1) {
                        if (TeamExtras.OPEN.equals(extension.getMemberProtect())) {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "开启了群成员保护");
                        } else {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "关闭了群成员保护");
                        }
                    } else if (extensionType == 2) {
                        if (TeamExtras.OPEN.equals(extension.getLeaveTeamVerify())) {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "开启了退群验证");
                        } else {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "关闭了退群验证");
                        }
                    } else if (extensionType == 3) {
                        if (TeamExtras.OPEN.equals(extension.getScreenshotNotify())) {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "开启了截屏通知");
                        } else {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "关闭了截屏通知");
                        }
                    } else if (extensionType == 4) {
                        if (TextUtils.isEmpty(extension.getRobotId())) {
                            sb.append("删除机器人成功");
                        } else {
                            sb.append("添加机器人成功");
                        }
                    } else if (extensionType == 5) {
                        if (TeamExtras.OPEN.equals(extension.getInviteVerity())) {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "开启了群认证");
                        } else {
                            sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "关闭了群认证");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sb.append("群扩展字段被更新为 Exception" + e.getMessage());
                }
            } else if (field.getKey() == TeamFieldEnum.Ext_Server) {
                sb.append("群扩展字段(服务器)被更新为 " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.ICON) {
                sb.append("群头像已更新");
            } else if (field.getKey() == TeamFieldEnum.InviteMode) {
                TeamInviteModeEnum value = (TeamInviteModeEnum) field.getValue();
                sb.append("群邀请他人权限被更新为 ");
                sb.append(value == TeamInviteModeEnum.All ? "所有人" : "管理员");
            } else if (field.getKey() == TeamFieldEnum.TeamUpdateMode) {
                sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + "更新了群资料修改权限");
//                TeamUpdateModeEnum value = (TeamUpdateModeEnum) field.getValue();
//                sb.append("群资料修改权限被更新为 ");
//                sb.append(value == TeamUpdateModeEnum.All ? "所有人可修改" : "管理员修改");
            }
//            else if (field.getKey() == TeamFieldEnum.BeInviteMode) {
//                TeamBeInviteModeEnum value = (TeamBeInviteModeEnum) field.getValue();
//                sb.append("群被邀请人身份验证权限被更新为 ");
//                sb.append(value == TeamBeInviteModeEnum.NeedAuth ? "需要验证" : "不需要验证");
//            }
            else if (field.getKey() == TeamFieldEnum.TeamExtensionUpdateMode) {
                sb.append("群扩展字段修改权限被更新为 " + field.getValue());
            } else if (field.getKey() == TeamFieldEnum.AllMute) {
                TeamAllMuteModeEnum teamAllMuteModeEnum = (TeamAllMuteModeEnum) field.getValue();
                if (teamAllMuteModeEnum == TeamAllMuteModeEnum.Cancel) {
                    sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + " 取消了全员禁言");
                } else {
                    sb.append(TeamHelper.getTeamMemberDisplayNameYou(tid, account) + " 设置了全员禁言");
                }
            } else {
                sb.append("群" + field.getKey() + "被更新为 " + field.getValue());
            }
            sb.append("\r\n");
        }
        if (sb.length() < 2) {
            return "未知通知";
        }
        return sb.delete(sb.length() - 2, sb.length()).toString();
    }

    private static String buildManagerPassTeamApplyNotification(String from, MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTeamMemberDisplayName(from));
        sb.append("通过了");
        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append("的申请");
        return sb.toString();
    }

    private static String buildTransferOwnerNotification(String from, MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTeamMemberDisplayName(from));
        sb.append("转移了群主身份给");
        sb.append(buildMemberListString(a.getTargets(), null));
        return sb.toString();
    }

    private static String buildAddTeamManagerNotification(MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append(" 被任命为管理员");

        return sb.toString();
    }

    private static String buildRemoveTeamManagerNotification(MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append(" 被撤销管理员身份");

        return sb.toString();
    }

    private static String buildAcceptInviteNotification(String from, MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(getTeamMemberDisplayName(from));
        sb.append("接受").append(buildMemberListString(a.getTargets(), null)).append("的邀请进群");

        return sb.toString();
    }

    private static String buildMuteTeamNotification(MuteMemberAttachment a) {
        StringBuilder sb = new StringBuilder();

        sb.append(buildMemberListString(a.getTargets(), null));
        sb.append("被管理员");
        sb.append(a.isMute() ? "禁言" : "解除禁言");

        return sb.toString();
    }
}
