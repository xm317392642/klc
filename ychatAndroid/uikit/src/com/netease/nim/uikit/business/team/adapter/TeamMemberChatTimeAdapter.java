package com.netease.nim.uikit.business.team.adapter;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;

import java.util.List;

public class TeamMemberChatTimeAdapter extends BaseMultiItemQuickAdapter<RedpacketInfo, BaseViewHolder> {
    private int type = 1;
    private TeamMemberChatTimeMultiple memberChatTimeMultiple;

    interface ViewType {
        int TYPE_NORMAL = 1;
        int TYPE_MULTIPLE_CHOICE = 2;
    }

    public TeamMemberChatTimeAdapter(Fragment fragment, RecyclerView recyclerView, List<RedpacketInfo> data) {
        super(recyclerView, data);
        memberChatTimeMultiple = (TeamMemberChatTimeMultiple) fragment;
        addItemType(ViewType.TYPE_NORMAL, R.layout.nim_team_member_chat_time_item, TeamMemberChatTimeHolder.class);
        addItemType(ViewType.TYPE_MULTIPLE_CHOICE, R.layout.nim_team_member_chat_time_choice_item, TeamMemberChatTimeMultipleHolder.class);
    }

    @Override
    protected int getViewType(RedpacketInfo item) {
        return type == 1 ? ViewType.TYPE_NORMAL : ViewType.TYPE_MULTIPLE_CHOICE;
    }

    @Override
    protected String getItemKey(RedpacketInfo item) {
        return item.getUid() + item.getAccid() + item.getTime();
    }

    public void setType(int type) {
        this.type = type;
    }

    public void onCheckedChanged(String accid, boolean isChecked) {
        memberChatTimeMultiple.onCheckedChanged(accid, isChecked);
    }

    public interface TeamMemberChatTimeMultiple {
        void onCheckedChanged(String accid, boolean isChecked);

        void updateMemberNumber(int type, int number);
    }
}