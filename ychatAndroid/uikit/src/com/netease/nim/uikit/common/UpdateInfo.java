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
    private String update;
    private String isForce;
    private String downUrl;
    private String version;
    private String release_log;
    private String emergencyUrl;
    private String emergency;
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

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public String getIsForce() {
        return isForce;
    }

    public void setIsForce(String isForce) {
        this.isForce = isForce;
    }

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "cmd=" + cmd +
                ", code=" + code +
                ", version=" + version +
                ", update=" + update +
                ", release_log=" + release_log +
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

    public String getRelease_log() {
        return release_log;
    }

    public void setRelease_log(String release_log) {
        this.release_log = release_log;
    }

    public String getEmergencyUrl() {
        return emergencyUrl;
    }

    public void setEmergencyUrl(String emergencyUrl) {
        this.emergencyUrl = emergencyUrl;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }
}




