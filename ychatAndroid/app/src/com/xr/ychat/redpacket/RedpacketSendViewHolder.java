package com.xr.ychat.redpacket;

import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;
import com.xr.ychat.R;

public class RedpacketSendViewHolder extends RecyclerViewHolder<BaseQuickAdapter, BaseViewHolder, RedpacketInfo> {

    private TextView type;
    private TextView time;
    private TextView money;
    private TextView status;

    public RedpacketSendViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    public void convert(BaseViewHolder holder, RedpacketInfo data, int position, boolean isScrolling) {
        inflate(holder);
        refresh(data);
    }

    public void inflate(BaseViewHolder holder) {
        type = holder.getView(R.id.redpacket_type);
        time = holder.getView(R.id.redpacket_time);
        money = holder.getView(R.id.redpacket_money);
        status = holder.getView(R.id.redpacket_status);
    }

    public void refresh(RedpacketInfo info) {
        time.setText(TimeUtils.millis2String(info.getnPayTime() * 1000));
        money.setText(String.format("%1$.2f元", info.getAmount()));
        String redpacketType;
        if (info.getnType() == 3) {
            redpacketType = "群红包";
        } else {
            redpacketType = "个人红包";
        }
        type.setText(redpacketType);
        String redpacketStatus;
        if (info.getStatus() == 3) {
            redpacketStatus = "未领取 0/1";
        } else if (info.getStatus() == 4) {
            redpacketStatus = "已领完 1/1";
        } else {
            redpacketStatus = "已过期 1/1";
        }
        status.setText(redpacketStatus);
    }
}