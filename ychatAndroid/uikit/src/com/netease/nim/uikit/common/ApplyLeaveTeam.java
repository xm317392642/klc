package com.netease.nim.uikit.common;

public class ApplyLeaveTeam {
    private String leaveTeamID;
    private String content;
    private int teamLeaveType;
    private int id;
    private boolean hasRead;

    public String getLeaveTeamID() {
        return leaveTeamID;
    }

    public void setLeaveTeamID(String leaveTeamID) {
        this.leaveTeamID = leaveTeamID;
    }

    public int getTeamLeaveType() {
        return teamLeaveType;
    }

    public void setTeamLeaveType(int teamLeaveType) {
        this.teamLeaveType = teamLeaveType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isHasRead() {
        return hasRead;
    }

    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }
}




