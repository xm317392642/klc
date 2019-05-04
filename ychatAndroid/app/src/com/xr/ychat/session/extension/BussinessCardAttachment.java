package com.xr.ychat.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.business.session.extension.CustomAttachment;
import com.netease.nim.uikit.business.session.extension.CustomAttachmentType;

public class BussinessCardAttachment extends CustomAttachment {

    private String personCardUserImage;//  头像
    private String personCardUserName;//  昵称
    private String personCardUserid;// 账号

    private static final String KEY_AVATAR = "personCardUserImage";
    private static final String KEY_NICKNAME = "personCardUserName";
    private static final String KEY_ACCOUNT = "personCardUserid";

    public BussinessCardAttachment() {
        super(CustomAttachmentType.BusinessCard);
    }

    @Override
    protected void parseData(JSONObject data) {
        personCardUserImage = data.getString(KEY_AVATAR);
        personCardUserName = data.getString(KEY_NICKNAME);
        personCardUserid = data.getString(KEY_ACCOUNT);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_AVATAR, personCardUserImage);
        data.put(KEY_NICKNAME, personCardUserName);
        data.put(KEY_ACCOUNT, personCardUserid);
        return data;
    }

    public String getPersonCardUserImage() {
        return personCardUserImage;
    }

    public void setPersonCardUserImage(String personCardUserImage) {
        this.personCardUserImage = personCardUserImage;
    }

    public String getPersonCardUserName() {
        return personCardUserName;
    }

    public void setPersonCardUserName(String personCardUserName) {
        this.personCardUserName = personCardUserName;
    }

    public String getPersonCardUserid() {
        return personCardUserid;
    }

    public void setPersonCardUserid(String personCardUserid) {
        this.personCardUserid = personCardUserid;
    }
}
