package com.xr.ychat.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.business.session.extension.CustomAttachment;
import com.netease.nim.uikit.business.session.extension.CustomAttachmentType;

public class GameShareAttachment extends CustomAttachment {

    private String shareLinkUrl;
    private String shareLinkTitle;
    private String shareLinkDes;
    private String shareLinkImage;
    private String shareLinkSourceName;
    private String shareLinkSourceImage;

    private static final String KEY_ShareLinkUrl = "shareLinkUrl";
    private static final String KEY_ShareLinkTitle = "shareLinkTitle";
    private static final String KEY_ShareLinkDes = "shareLinkDes";
    private static final String KEY_ShareLinkImage = "shareLinkImage";
    private static final String KEY_ShareLinkSourceName = "shareLinkSourceName";
    private static final String KEY_ShareLinkSourceImage = "shareLinkSourceImage";

    public GameShareAttachment() {
        super(CustomAttachmentType.GameShare);
    }

    @Override
    protected void parseData(JSONObject data) {
        shareLinkUrl = data.getString(KEY_ShareLinkUrl);
        shareLinkTitle = data.getString(KEY_ShareLinkTitle);
        shareLinkDes = data.getString(KEY_ShareLinkDes);
        shareLinkImage = data.getString(KEY_ShareLinkImage);
        shareLinkSourceName = data.getString(KEY_ShareLinkSourceName);
        shareLinkSourceImage = data.getString(KEY_ShareLinkSourceImage);
    }

    @Override
    protected JSONObject packData() {
        JSONObject data = new JSONObject();
        data.put(KEY_ShareLinkUrl, shareLinkUrl);
        data.put(KEY_ShareLinkTitle, shareLinkTitle);
        data.put(KEY_ShareLinkDes, shareLinkDes);
        data.put(KEY_ShareLinkImage, shareLinkImage);
        data.put(KEY_ShareLinkSourceName, shareLinkSourceName);
        data.put(KEY_ShareLinkSourceImage, shareLinkSourceImage);
        return data;
    }

    public String getShareLinkUrl() {
        return shareLinkUrl;
    }

    public void setShareLinkUrl(String shareLinkUrl) {
        this.shareLinkUrl = shareLinkUrl;
    }

    public String getShareLinkTitle() {
        return shareLinkTitle;
    }

    public void setShareLinkTitle(String shareLinkTitle) {
        this.shareLinkTitle = shareLinkTitle;
    }

    public String getShareLinkDes() {
        return shareLinkDes;
    }

    public void setShareLinkDes(String shareLinkDes) {
        this.shareLinkDes = shareLinkDes;
    }

    public String getShareLinkImage() {
        return shareLinkImage;
    }

    public void setShareLinkImage(String shareLinkImage) {
        this.shareLinkImage = shareLinkImage;
    }

    public String getShareLinkSourceName() {
        return shareLinkSourceName;
    }

    public void setShareLinkSourceName(String shareLinkSourceName) {
        this.shareLinkSourceName = shareLinkSourceName;
    }

    public String getShareLinkSourceImage() {
        return shareLinkSourceImage;
    }

    public void setShareLinkSourceImage(String shareLinkSourceImage) {
        this.shareLinkSourceImage = shareLinkSourceImage;
    }


}
