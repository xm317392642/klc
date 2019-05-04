package com.netease.nim.uikit.business.team.adapter;

import android.widget.CheckBox;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

public class TeamMemberChatTimeMultipleHolder extends RecyclerViewHolder<BaseQuickAdapter, BaseViewHolder, RedpacketInfo> {
    private HeadImageView imageView;
    private TextView name;
    private CheckBox checkBox;
    private NimUserInfo userInfo;
    private TeamMemberChatTimeAdapter adapter;

    public TeamMemberChatTimeMultipleHolder(BaseQuickAdapter adapter) {
        super(adapter);
        this.adapter = (TeamMemberChatTimeAdapter) adapter;
    }

    @Override
    public void convert(BaseViewHolder holder, RedpacketInfo data, int position, boolean isScrolling) {
        inflate(holder);
        refresh(data);
    }

    public void inflate(BaseViewHolder holder) {
        imageView = holder.getView(R.id.team_member_change_avatar);
        name = holder.getView(R.id.team_member_change_name);
        checkBox = holder.getView(R.id.team_member_change_checkbox);
    }

    public void refresh(RedpacketInfo info) {
        imageView.loadBuddyAvatar(info.getAccid());
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
        checkBox.setChecked(info.isCheck());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            info.setCheck(isChecked);
            adapter.onCheckedChanged(info.getAccid(), isChecked);
        });
    }

    private void updateUI() {
        name.setText(String.format("%1$s", userInfo.getName()));
    }

}