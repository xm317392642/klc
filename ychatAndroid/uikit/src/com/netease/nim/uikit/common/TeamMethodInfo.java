package com.netease.nim.uikit.common;

import java.util.ArrayList;

public class TeamMethodInfo {
    private int cmd;
    private int code;
    private String note;
    private ArrayList<UnclaimedEnvelope> list;

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ArrayList<UnclaimedEnvelope> getList() {
        return list;
    }

    public void setList(ArrayList<UnclaimedEnvelope> list) {
        this.list = list;
    }
}
