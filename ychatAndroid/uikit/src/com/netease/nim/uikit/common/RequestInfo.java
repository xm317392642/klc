package com.netease.nim.uikit.common;

import java.util.ArrayList;

public class RequestInfo {
    private int isfirstlogin;//是否第一次登录(1是第一次登录)
    private String account;
    private String accid;
    private int cmd;
    private int code;
    private String password;
    private int platflag;
    private int platid;
    private String token;
    private String mytoken;
    private String para;
    private String uid;
    private String aliuid;
    private String alinickname;
    private String aliavatar;
    private String alilogonid;
    private String orderno;
    private AlipayInfo aliinf;
    private float amount;
    private int status; // 0=初始化状态 1=玩家支付取消 2=支付被支付宝关闭 3=支付完成 4=完成领取 5=领取过期
    private String fromUID;
    private String fromAliLogonId;
    private String toUID;
    private String toAliLogonId;
    private long nPayTime;
    private long nDisTime;
    private String date;
    private int hisType;
    private int page;
    private ArrayList<RedpacketInfo> data;
    private float send;
    private float receive;
    private int flag;
    private String ychatNo;
    private String DoAccID;
    private int action;
    private int onoff;
    private String qunID;
    private ArrayList<RobotInfo> list;

    public int getIsfirstlogin() {
        return isfirstlogin;
    }

    public void setIsfirstlogin(int isfirstlogin) {
        this.isfirstlogin = isfirstlogin;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPlatflag() {
        return platflag;
    }

    public void setPlatflag(int platflag) {
        this.platflag = platflag;
    }

    public int getPlatid() {
        return platid;
    }

    public void setPlatid(int platid) {
        this.platid = platid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPara() {
        return para;
    }

    public void setPara(String para) {
        this.para = para;
    }

    public String getMytoken() {
        return mytoken;
    }

    public void setMytoken(String mytoken) {
        this.mytoken = mytoken;
    }

    public String getAliuid() {
        return aliuid;
    }

    public void setAliuid(String aliuid) {
        this.aliuid = aliuid;
    }

    public AlipayInfo getAliinf() {
        return aliinf;
    }

    public void setAliinf(AlipayInfo aliinf) {
        this.aliinf = aliinf;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFromUID() {
        return fromUID;
    }

    public void setFromUID(String fromUID) {
        this.fromUID = fromUID;
    }

    public String getToUID() {
        return toUID;
    }

    public void setToUID(String toUID) {
        this.toUID = toUID;
    }

    public long getnPayTime() {
        return nPayTime;
    }

    public void setnPayTime(long nPayTime) {
        this.nPayTime = nPayTime;
    }

    public long getnDisTime() {
        return nDisTime;
    }

    public void setnDisTime(long nDisTime) {
        this.nDisTime = nDisTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHisType() {
        return hisType;
    }

    public void setHisType(int hisType) {
        this.hisType = hisType;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ArrayList<RedpacketInfo> getData() {
        return data;
    }

    public void setData(ArrayList<RedpacketInfo> data) {
        this.data = data;
    }

    public String getFromAliLogonId() {
        return fromAliLogonId;
    }

    public void setFromAliLogonId(String fromAliLogonId) {
        this.fromAliLogonId = fromAliLogonId;
    }

    public String getToAliLogonId() {
        return toAliLogonId;
    }

    public void setToAliLogonId(String toAliLogonId) {
        this.toAliLogonId = toAliLogonId;
    }

    public String getAlinickname() {
        return alinickname;
    }

    public void setAlinickname(String alinickname) {
        this.alinickname = alinickname;
    }

    public String getAliavatar() {
        return aliavatar;
    }

    public void setAliavatar(String aliavatar) {
        this.aliavatar = aliavatar;
    }

    public String getAlilogonid() {
        return alilogonid;
    }

    public void setAlilogonid(String alilogonid) {
        this.alilogonid = alilogonid;
    }

    public float getSend() {
        return send;
    }

    public void setSend(float send) {
        this.send = send;
    }

    public float getReceive() {
        return receive;
    }

    public void setReceive(float receive) {
        this.receive = receive;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getYchatNo() {
        return ychatNo;
    }

    public void setYchatNo(String ychatNo) {
        this.ychatNo = ychatNo;
    }

    public String getDoAccID() {
        return DoAccID;
    }

    public void setDoAccID(String doAccID) {
        DoAccID = doAccID;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getQunID() {
        return qunID;
    }

    public void setQunID(String qunID) {
        this.qunID = qunID;
    }

    public int getHb_onoff() {
        return onoff;
    }

    public void setHb_onoff(int hb_onoff) {
        this.onoff = hb_onoff;
    }

    public ArrayList<RobotInfo> getList() {
        return list;
    }

    public void setList(ArrayList<RobotInfo> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "account='" + account + '\'' +
                ", accid='" + accid + '\'' +
                ", cmd=" + cmd +
                ", code=" + code +
                ", password='" + password + '\'' +
                ", platflag=" + platflag +
                ", platid=" + platid +
                ", token='" + token + '\'' +
                ", mytoken='" + mytoken + '\'' +
                ", para='" + para + '\'' +
                ", uid='" + uid + '\'' +
                ", aliuid='" + aliuid + '\'' +
                ", alinickname='" + alinickname + '\'' +
                ", aliavatar='" + aliavatar + '\'' +
                ", alilogonid='" + alilogonid + '\'' +
                ", orderno='" + orderno + '\'' +
                ", aliinf=" + aliinf +
                ", amount=" + amount +
                ", status=" + status +
                ", fromUID='" + fromUID + '\'' +
                ", fromAliLogonId='" + fromAliLogonId + '\'' +
                ", toUID='" + toUID + '\'' +
                ", toAliLogonId='" + toAliLogonId + '\'' +
                ", nPayTime=" + nPayTime +
                ", nDisTime=" + nDisTime +
                ", date='" + date + '\'' +
                ", hisType=" + hisType +
                ", page=" + page +
                ", data=" + data +
                ", send=" + send +
                ", receive=" + receive +
                ", flag=" + flag +
                ", ychatNo='" + ychatNo + '\'' +
                ", DoAccID='" + DoAccID + '\'' +
                ", action=" + action +
                ", hb_onoff=" + onoff +
                ", qunID='" + qunID + '\'' +
                '}';
    }
}


