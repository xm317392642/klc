package com.netease.nim.uikit.business.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

/**
 * 群认证的一条自定义消息
 *
 * tipType:1为普通成员拉人申请， 2为群主、管理员同意邀请,   3为接受（群超过40人，用户接受发送3，黄朝那边发送,）4为首次创建群
 *
 * 用户首次创建群的时候，发tipType=4的消息
 * 普通成员拉人申请,发tipType=1的消息
 * 群主拉人申请,发tipType=2的消息
 * 普通成员申请拉人后，群主、管理员点击同意邀请成功inteam=1后，发tipType=2的消息
 */
public class TeamAuthAttachment extends CustomAttachment {
    public static final String APPLY1 = "1";
    public static final String AGREE2 = "2";
    public static final String ACCEPT3 = "3";
    public static final String CREATE_TEAM4 = "4";

    private String inviteTipId; //消息id，当前时间戳+account
    private String inviteTipFromId;//邀请人的id
    private String inviteTipToId;//被邀请人的id用逗号分隔开来
    private String inviteTipContent;//邀请原因
    private String inviteTipType;//1为普通成员拉人申请， 2为群主同意,3为群主接受邀请   （屏蔽同意和接受这两个）


    private static final String KEY_inviteTipId = "inviteTipId";
    private static final String KEY_inviteTipFromId = "inviteTipFromId";
    private static final String KEY_inviteTipToId = "inviteTipToId";
    private static final String KEY_inviteTipContent = "inviteTipContent";
    private static final String KEY_inviteTipType = "inviteTipType";

    public TeamAuthAttachment() {
        super(CustomAttachmentType.TEAM_AUTHENTICATION);
    }



    @Override
    protected void parseData(JSONObject data) {
        inviteTipId = data.getString(KEY_inviteTipId);
        inviteTipFromId = data.getString(KEY_inviteTipFromId);
        inviteTipToId = data.getString(KEY_inviteTipToId);
        inviteTipContent = data.getString(KEY_inviteTipContent);
        inviteTipType = data.getString(KEY_inviteTipType);


    }

    @Override
    protected JSONObject packData() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(KEY_inviteTipId, inviteTipId);
        jsonObj.put(KEY_inviteTipFromId, inviteTipFromId);
        jsonObj.put(KEY_inviteTipToId, inviteTipToId);
        jsonObj.put(KEY_inviteTipContent, inviteTipContent);
        jsonObj.put(KEY_inviteTipType, inviteTipType);
        return jsonObj;
    }

    public String getInviteTipId() {
        return inviteTipId;
    }

    public void setInviteTipId(String inviteTipId) {
        this.inviteTipId = inviteTipId;
    }

    public String getInviteTipFromId() {
        return inviteTipFromId;
    }

    public void setInviteTipFromId(String inviteTipFromId) {
        this.inviteTipFromId = inviteTipFromId;
    }

    public String getInviteTipToId() {
        return inviteTipToId;
    }

    public void setInviteTipToId(String inviteTipToId) {
        this.inviteTipToId = inviteTipToId;
    }

    public String getInviteTipContent() {
        return inviteTipContent;
    }

    public void setInviteTipContent(String inviteTipContent) {
        this.inviteTipContent = inviteTipContent;
    }



    public String getInviteTipType() {
        return inviteTipType;
    }

    public void setInviteTipType(String inviteTipType) {
        this.inviteTipType = inviteTipType;
    }

}
