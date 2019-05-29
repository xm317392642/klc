package com.netease.nim.uikit.business.session.extension;

import com.alibaba.fastjson.JSONObject;

public class TeamInviteAttachment extends CustomAttachment {
    private String invitor_id;
    private String invitor_name;
    private String team_id;
    private String team_name;
    private static final String KEY_INVITE_ID = "invitor_id";
    private static final String KEY_INVITE_NAME = "invitor_name";
    private static final String KEY_TEAM_ID = "team_id";
    private static final String KEY_TEAM_NAME = "team_name";

    public TeamInviteAttachment() {
        super(CustomAttachmentType.TeamInvite);
    }

    @Override
    protected void parseData(JSONObject data) {
        invitor_id = data.getString(KEY_INVITE_ID);
        invitor_name = data.getString(KEY_INVITE_NAME);
        team_id = data.getString(KEY_TEAM_ID);
        team_name = data.getString(KEY_TEAM_NAME);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_INVITE_ID, invitor_id);
        data.put(KEY_INVITE_NAME, invitor_name);
        data.put(KEY_TEAM_ID, team_id);
        data.put(KEY_TEAM_NAME, team_name);
        return data;
    }

    public String getInvitor_id() {
        return invitor_id;
    }

    public void setInvitor_id(String invitor_id) {
        this.invitor_id = invitor_id;
    }

    public String getInvitor_name() {
        return invitor_name;
    }

    public void setInvitor_name(String invitor_name) {
        this.invitor_name = invitor_name;
    }

    public String getTeam_id() {
        return team_id;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }
}
