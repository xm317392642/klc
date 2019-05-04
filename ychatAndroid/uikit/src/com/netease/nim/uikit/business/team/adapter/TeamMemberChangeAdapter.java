package com.netease.nim.uikit.business.team.adapter;

import android.support.v7.widget.RecyclerView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.team.viewholder.TeamMemberChangeHolder;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;

import java.util.List;

public class TeamMemberChangeAdapter extends BaseMultiItemQuickAdapter<RedpacketInfo, BaseViewHolder> {

    interface ViewType {
        int TYPE_ADD = 1;
        int TYPE_REMOVE = 2;
    }

    public TeamMemberChangeAdapter(RecyclerView recyclerView, List<RedpacketInfo> data) {
        super(recyclerView, data);
        addItemType(ViewType.TYPE_ADD, R.layout.nim_team_member_change_item, TeamMemberChangeHolder.class);
    }

    @Override
    protected int getViewType(RedpacketInfo item) {
        return ViewType.TYPE_ADD;
    }

    @Override
    protected String getItemKey(RedpacketInfo item) {
        return item.getUid() + item.getAction() + item.getAccid() + item.getTime();
    }

}
