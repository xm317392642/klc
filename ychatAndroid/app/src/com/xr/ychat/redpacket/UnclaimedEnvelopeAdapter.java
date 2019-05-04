package com.xr.ychat.redpacket;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.common.UnclaimedEnvelope;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.R;

import java.util.List;

public class UnclaimedEnvelopeAdapter extends BaseQuickAdapter<UnclaimedEnvelope, BaseViewHolder> {
    private HeadImageView imageView;
    private TextView name;
    private TextView time;
    private TextView content;
    private TextView status;
    private ConstraintLayout layout;
    private NimUserInfo userInfo;
    private Container container;
    private String teamId;

    public UnclaimedEnvelopeAdapter(RecyclerView recyclerView, List<UnclaimedEnvelope> data, Container container, String teamId) {
        super(recyclerView, R.layout.nim_team_envelope_unclaimed_item, data);
        this.container = container;
        this.teamId = teamId;
    }

    @Override
    protected void convert(BaseViewHolder holder, UnclaimedEnvelope unclaimedEnvelope, int position, boolean isScrolling) {
        imageView = holder.getView(R.id.user_avatar);
        name = holder.getView(R.id.user_name);
        time = holder.getView(R.id.envelope_time);
        content = holder.getView(R.id.envelope_content);
        status = holder.getView(R.id.envelope_status);
        layout = holder.getView(R.id.envelope_layout);
        content.setText(unclaimedEnvelope.getContent());
        int type = unclaimedEnvelope.getType();
        String reType;
        if (type == 0) {
            layout.setBackground(ContextCompat.getDrawable(holder.getContext(), R.drawable.red_packet_rev_normal));
        } else {
            layout.setBackground(ContextCompat.getDrawable(holder.getContext(), R.drawable.red_packet_rev_press));
        }
        if (type == 0) {
            reType = "查看红包";
        } else if (type == 1) {
            reType = "已领取";
        } else if (type == 2) {
            reType = "已过期";
        } else if (type == 3) {
            reType = "不可领取";
        } else {
            reType = "已领取";
        }
        status.setText(reType);
        String text = TimeUtil.getNewChatTime(unclaimedEnvelope.getTime() * 1000L);
        time.setText(text);
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(unclaimedEnvelope.getFromid());
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(unclaimedEnvelope.getFromid(), new SimpleCallback<NimUserInfo>() {

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
        layout.setOnClickListener(v -> {
            OpenRedpacketFragment fragment = new OpenRedpacketFragment();
            Bundle bundle = new Bundle();
            bundle.putString("SessionId", teamId);
            bundle.putString("FromAccount", unclaimedEnvelope.getFromid());
            bundle.putString("FromContent", unclaimedEnvelope.getContent());
            bundle.putString("BriberyId", unclaimedEnvelope.getOrderno());
            bundle.putInt("Type", 2);
            bundle.putInt("Status", unclaimedEnvelope.getType());
            fragment.setArguments(bundle);
            SwipeBackUI swipeBackUI = (SwipeBackUI) holder.getContext();
            NIMOpenRpCallback cb = new NIMOpenRpCallback(unclaimedEnvelope.getFromid(), teamId, SessionTypeEnum.Team, container.proxy);
            fragment.show(swipeBackUI.getSupportFragmentManager(), cb, null);
        });
    }

    private void updateUI() {
        imageView.loadAvatar(userInfo.getAvatar());
        name.setText(String.format("%1$s", userInfo.getName()));
    }

}