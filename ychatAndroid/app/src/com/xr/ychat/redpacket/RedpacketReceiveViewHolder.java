package com.xr.ychat.redpacket;

import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.R;

public class RedpacketReceiveViewHolder extends RecyclerViewHolder<BaseQuickAdapter, BaseViewHolder, RedpacketInfo> {

    private HeadImageView imageView;
    private TextView name;
    private TextView time;
    private TextView money;

    public RedpacketReceiveViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    public void convert(BaseViewHolder holder, RedpacketInfo data, int position, boolean isScrolling) {
        inflate(holder);
        refresh(data);
    }

    public void inflate(BaseViewHolder holder) {
        imageView = holder.getView(R.id.redpacket_avatar);
        name = holder.getView(R.id.redpacket_name);
        time = holder.getView(R.id.redpacket_time);
        money = holder.getView(R.id.redpacket_money);
    }

    public void refresh(RedpacketInfo info) {
        time.setText(TimeUtils.millis2String(info.getnPayTime() * 1000));
        money.setText(String.format("%1$.2f元", info.getAmount()));
        NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(info.getFromUID());
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(info.getFromUID(), new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        imageView.loadAvatar(result.getAvatar());
                        name.setText(result.getName());
                    }
                }
            });
        } else {
            imageView.loadAvatar(userInfo.getAvatar());
            name.setText(userInfo.getName());
        }
    }
}