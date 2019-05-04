package com.netease.nim.uikit.common;

public class UpdateInfo {

    /**
     * cmd : 15
     * code : 0
     * update : 1
     * downUrl : http://xxxxxxxx.com/erer
     */

    private int cmd;
    private int code;
    private int update;
    private int isForce;
    private String downUrl;
    private String version;
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

    public int getUpdate() {
        return update;
    }

    public void setUpdate(int update) {
        this.update = update;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public int getIsForce() {
        return isForce;
    }

    public void setIsForce(int isForce) {
        this.isForce = isForce;
    }

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "cmd=" + cmd +
                ", code=" + code +
                ", version=" + version +
                ", update=" + update +
                ", isForce=" + isForce +
                ", downUrl='" + downUrl + '\'' +
                '}';
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}




