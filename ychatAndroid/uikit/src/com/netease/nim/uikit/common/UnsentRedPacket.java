package com.netease.nim.uikit.common;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

public class UnsentRedPacket {
    private String redPacketAccount;
    private String redPacketID;
    private String redPacketMessage;
    private SessionTypeEnum sessionTypeEnum;
    private int time;

    public UnsentRedPacket(String redPacketAccount, String redPacketID, String redPacketMessage, SessionTypeEnum sessionTypeEnum) {
        this.redPacketAccount = redPacketAccount;
        this.redPacketID = redPacketID;
        this.redPacketMessage = redPacketMessage;
        this.sessionTypeEnum = sessionTypeEnum;
        this.time = 0;
    }

    public String getRedPacketAccount() {
        return redPacketAccount;
    }

    public String getRedPacketID() {
        return redPacketID;
    }

    public String getRedPacketMessage() {
        return redPacketMessage;
    }

    public SessionTypeEnum getSessionTypeEnum() {
        return sessionTypeEnum;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
