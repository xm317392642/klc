package com.xr.ychat.session.viewholder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.uikit.business.chatroom.adapter.ChatRoomMsgAdapter;
import com.netease.nim.uikit.business.session.extension.RedPacketAttachment;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.business.session.module.list.MsgAdapter;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.xr.ychat.R;
import com.xr.ychat.redpacket.NIMOpenRpCallback;
import com.xr.ychat.redpacket.NIMRedPacketClient;

public class MsgViewHolderRedPacket extends MsgViewHolderBase {
    private long mLastClickTime = 0;
    public static final long TIME_INTERVAL = 1000L;
    private RelativeLayout sendView, revView;
    private TextView sendContentText, revContentText;    // 红包描述
    private TextView sendStatusText, revStatusText;    // 红包状态

    public MsgViewHolderRedPacket(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.red_packet_item;
    }

    @Override
    protected void inflateContentView() {
        sendContentText = findViewById(R.id.tv_bri_mess_send);
        sendView = findViewById(R.id.bri_send);
        revContentText = findViewById(R.id.tv_bri_mess_rev);
        revView = findViewById(R.id.bri_rev);
        sendStatusText = findViewById(R.id.tv_bri_status_send);
        revStatusText = findViewById(R.id.tv_bri_status_rev);
    }

    @Override
    protected void bindContentView() {
        RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();

        if (!isReceivedMessage()) {// 消息方向，自己发送的
            sendView.setVisibility(View.VISIBLE);
            int type = attachment.getRpType();
            progressBar.setVisibility(View.GONE);
            sendView.setEnabled(type == 0 || type == 5);
            String reType;
            if (type == 0 || type == 5) {
                reType = "查看红包";
            } else if (type == 2) {
                reType = "已过期";
            } else if (type == 3) {
                reType = "不可领取";
            } else {
                reType = "已领取";
            }
            sendStatusText.setText(reType);
            revView.setVisibility(View.GONE);
            sendContentText.setText(attachment.getRpContent());
        } else {
            sendView.setVisibility(View.GONE);
            revView.setVisibility(View.VISIBLE);
            int type = attachment.getRpType();
            progressBar.setVisibility(View.GONE);
            revView.setEnabled(type == 0);
            String reType;
            if (type == 0) {
                reType = "领取红包";
            } else if (type == 2) {
                reType = "已过期";
            } else if (type == 3) {
                reType = "不可领取";
            } else {
                reType = "已领取";
            }
            revStatusText.setText(reType);
            revContentText.setText(attachment.getRpContent());
        }
    }

    @Override
    protected int leftBackground() {
        return R.color.transparent;
    }

    @Override
    protected int rightBackground() {
        return R.color.transparent;
    }

    @Override
    protected void onItemClick() {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > TIME_INTERVAL) {
            mLastClickTime = nowTime;
            // 拆红包
            RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
            BaseMultiItemFetchLoadAdapter adapter = getAdapter();
            ModuleProxy proxy = null;
            if (adapter instanceof MsgAdapter) {
                proxy = ((MsgAdapter) adapter).getContainer().proxy;
            } else if (adapter instanceof ChatRoomMsgAdapter) {
                proxy = ((ChatRoomMsgAdapter) adapter).getContainer().proxy;
            }
            FragmentActivity activity = (FragmentActivity) context;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("OpenRedpacketFragment");
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit();
            }
            NIMOpenRpCallback cb = new NIMOpenRpCallback(message.getFromAccount(), message.getSessionId(), message.getSessionType(), proxy);
            NIMRedPacketClient.startOpenRpDialog(activity, message.getSessionId(), message.getFromAccount(), attachment.getRpContent(), message.getSessionType(), attachment.getRpId(), attachment.getRpType(), cb, message);
        } else {
            YchatToastUtils.showShort("不要快速点击");
        }
    }

}
