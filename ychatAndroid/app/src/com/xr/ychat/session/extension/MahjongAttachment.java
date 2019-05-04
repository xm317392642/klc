package com.xr.ychat.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.business.session.extension.CustomAttachment;
import com.netease.nim.uikit.business.session.extension.CustomAttachmentType;
import com.netease.nim.uikit.common.Mahjong;

public class MahjongAttachment extends CustomAttachment {
    private Mahjong mahjong;
    private Gson gson;

    public MahjongAttachment() {
        super(CustomAttachmentType.Mahjong);
        gson = new Gson();
    }

    @Override
    protected void parseData(JSONObject data) {
        mahjong = gson.fromJson(data.toString(), new TypeToken<Mahjong>() {
        }.getType());
    }

    @Override
    protected JSONObject packData() {
        return JSONObject.parseObject(gson.toJson(mahjong));
    }

    public Mahjong getMahjong() {
        return mahjong;
    }

    public void setMahjong(Mahjong mahjong) {
        this.mahjong = mahjong;
    }
}
