package com.xr.ychat.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.business.session.extension.CustomAttachment;
import com.netease.nim.uikit.business.session.extension.CustomAttachmentType;

public class RedPacketAttachment extends CustomAttachment {

    private String content;//  消息文本内容
    private String redPacketId;//  红包id
    private String title;// 红包名称
    private float amount;
    private int type;

    private static final String KEY_CONTENT = "content";
    private static final String KEY_ID = "redPacketId";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_TYPE = "type";
    //0未领取  1 已领取  2 过期  3 不可领取  4 被别人领取

    public RedPacketAttachment() {
        super(CustomAttachmentType.RedPacket);
    }

    @Override
    protected void parseData(JSONObject data) {
        content = data.getString(KEY_CONTENT);
        redPacketId = data.getString(KEY_ID);
        title = data.getString(KEY_TITLE);
        amount = data.getFloat(KEY_AMOUNT);
        type = data.getInteger(KEY_TYPE);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_CONTENT, content);
        data.put(KEY_ID, redPacketId);
        data.put(KEY_TITLE, title);
        data.put(KEY_AMOUNT, amount);
        data.put(KEY_TYPE, type);
        return data;
    }

    public String getRpContent() {
        return content;
    }

    public String getRpId() {
        return redPacketId;
    }

    public String getRpTitle() {
        return title;
    }

    public float getRpAmount() {
        return amount;
    }

    public int getRpType() {
        return type;
    }

    public void setRpContent(String content) {
        this.content = content;
    }

    public void setRpId(String briberyID) {
        this.redPacketId = briberyID;
    }

    public void setRpTitle(String briberyName) {
        this.title = briberyName;
    }

    public void setRpAmount(float amount) {
        this.amount = amount;
    }

    public void setRpType(int type) {
        this.type = type;
    }
}
