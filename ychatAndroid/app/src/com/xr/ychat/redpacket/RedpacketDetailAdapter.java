package com.xr.ychat.redpacket;

import android.support.v7.widget.RecyclerView;

import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.xr.ychat.R;

import java.util.List;

public class RedpacketDetailAdapter extends BaseMultiItemQuickAdapter<RedpacketInfo, BaseViewHolder> {
    private int type;

    interface ViewType {
        int TYPE_SEND = 1;
        int VIEW_RECEIVE = 2;
    }

    public RedpacketDetailAdapter(RecyclerView recyclerView, List<RedpacketInfo> data, int type) {
        super(recyclerView, data);
        this.type = type;
        addItemType(ViewType.TYPE_SEND, R.layout.item_redpacket_record_send, RedpacketSendViewHolder.class);
        addItemType(ViewType.VIEW_RECEIVE, R.layout.item_redpacket_record_receive, RedpacketReceiveViewHolder.class);
    }

    @Override
    protected int getViewType(RedpacketInfo item) {
        return (type == 2) ? ViewType.VIEW_RECEIVE : ViewType.TYPE_SEND;
    }

    @Override
    protected String getItemKey(RedpacketInfo item) {
        return item.getOrderno();
    }

}
