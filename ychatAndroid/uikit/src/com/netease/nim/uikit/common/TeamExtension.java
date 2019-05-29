package com.netease.nim.uikit.common;

public class TeamExtension {
    private String memberProtect = "0";//群成员保护
    private String leaveTeamVerify = "0";//退群验证
    private String screenshotNotify = "0";//截屏通知
    private String inviteVerity="0";//开启群认证，0：未开启，1：已开启
    private String robotId;
    private String deletedRobotId;
    /**
     * 1:群成员保护模式
     * 2:退群验证
     * 3:截屏通知
     * 4:添加、移出机器人
     * 5:开启群认证
     */
    private int extensionType;

    public String getMemberProtect() {
        return memberProtect;
    }

    public String getInviteVerity() {
        return inviteVerity;
    }

    public void setInviteVerity(String inviteVerity) {
        this.inviteVerity = inviteVerity;
    }

    public void setMemberProtect(String memberProtect) {
        this.memberProtect = memberProtect;
    }

    public String getLeaveTeamVerify() {
        return leaveTeamVerify;
    }

    public void setLeaveTeamVerify(String leaveTeamVerify) {
        this.leaveTeamVerify = leaveTeamVerify;
    }

    public int getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(int extensionType) {
        this.extensionType = extensionType;
    }

    @Override
    public String toString() {
        return "TeamExtension{" +
                "memberProtect='" + memberProtect + '\'' +
                ", leaveTeamVerify='" + leaveTeamVerify + '\'' +
                ", screenshotNotify='" + screenshotNotify + '\'' +
                ", extensionType=" + extensionType +
                '}';
    }

    public String getScreenshotNotify() {
        return screenshotNotify;
    }

    public void setScreenshotNotify(String screenshotNotify) {
        this.screenshotNotify = screenshotNotify;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getDeletedRobotId() {
        return deletedRobotId;
    }

    public void setDeletedRobotId(String deletedRobotId) {
        this.deletedRobotId = deletedRobotId;
    }
}




