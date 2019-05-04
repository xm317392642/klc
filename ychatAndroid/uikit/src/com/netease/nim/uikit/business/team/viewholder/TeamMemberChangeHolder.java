package com.netease.nim.uikit.business.team.viewholder;

import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

public class TeamMemberChangeHolder extends RecyclerViewHolder<BaseQuickAdapter, BaseViewHolder, RedpacketInfo> {
    private HeadImageView imageView;
    private TextView name;
    private TextView type;
    private TextView time;
    private NimUserInfo userInfo;

    public TeamMemberChangeHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    public void convert(BaseViewHolder holder, RedpacketInfo data, int position, boolean isScrolling) {
        inflate(holder);
        refresh(data);
    }

    public void inflate(BaseViewHolder holder) {
        imageView = holder.getView(R.id.team_member_change_avatar);
        name = holder.getView(R.id.team_member_change_name);
        type = holder.getView(R.id.team_member_change_type);
        time = holder.getView(R.id.team_member_change_time);
    }

    public void refresh(RedpacketInfo info) {
        imageView.loadBuddyAvatar(info.getAccid());
        type.setText(info.getAction() == 1 ? "进群" : "退群");
        time.setText(TimeUtil.getNewChatTimeInSesstionList(info.getTime() * 1000L));
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(info.getAccid());
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(info.getAccid(), new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        userInfo = result;
                        updateUI();
                    }
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        name.setText(String.format("%1$s", userInfo.getName()));
    }
}
