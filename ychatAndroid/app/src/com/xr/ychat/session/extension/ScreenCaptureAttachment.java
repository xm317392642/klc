package com.xr.ychat.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.business.session.extension.CustomAttachment;
import com.netease.nim.uikit.business.session.extension.CustomAttachmentType;

public class ScreenCaptureAttachment extends CustomAttachment {

    private String customTipId;
    private String customTipContent;

    private static final String KEY_CustomTipId = "customTipId";
    private static final String KEY_CustomTipContent= "customTipContent";

    public ScreenCaptureAttachment() {
        super(CustomAttachmentType.CustomMessageTypeCustomTip);
    }

    @Override
    protected void parseData(JSONObject data) {
        customTipId = data.getString(KEY_CustomTipId);
        customTipContent = data.getString(KEY_CustomTipContent);
    }
    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_CustomTipId, customTipId);
        data.put(KEY_CustomTipContent, customTipContent);
        return data;
    }

    public String getCustomTipId() {
        return customTipId;
    }

    public void setCustomTipId(String customTipId) {
        this.customTipId = customTipId;
    }

    public String getCustomTipContent() {
        return customTipContent;
    }

    public void setCustomTipContent(String customTipContent) {
        this.customTipContent = customTipContent;
    }

    public static String getKEY_CustomTipId() {
        return KEY_CustomTipId;
    }

    public static String getKEY_CustomTipContent() {
        return KEY_CustomTipContent;
    }
}
