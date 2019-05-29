package com.netease.nim.uikit.common;

import java.io.Serializable;

public class TeamInvite implements Serializable {
    private String invitor_id;
    private String invitor_name;
    private String team_id;
    private String team_name;

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
