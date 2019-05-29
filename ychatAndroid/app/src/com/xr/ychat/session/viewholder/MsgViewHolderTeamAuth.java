package com.xr.ychat.session.viewholder;

import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.extension.TeamAuthAttachment;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.xr.ychat.main.activity.InviteDetailActivity;


public class MsgViewHolderTeamAuth extends MsgViewHolderBase {

    private TextView notificationTextView;
    private ImageView invite_friend_img;
    private TeamAuthAttachment attachment;
    private LinearLayout linearLayout;


    public MsgViewHolderTeamAuth(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.team_auth_notification_item;
    }

    @Override
    protected void inflateContentView() {
        notificationTextView = findViewById(R.id.notification_message);
        invite_friend_img = findViewById(R.id.invite_friend);
        linearLayout = findViewById(R.id.packet_ll);

    }

    @Override
    protected void bindContentView() {
        attachment = (TeamAuthAttachment) message.getAttachment();
        TeamMember member = NimUIKit.getTeamProvider().getTeamMember(message.getSessionId(), NimUIKit.getAccount());
        //邀请通知只有群主和管理员看到，其他成员不可见
        String fromId = attachment.getInviteTipFromId();
        String[] toIdArray = attachment.getInviteTipToId().split(",");

        switch (attachment.getInviteTipType()) {
            case "-1"://本地临时改变状态为-1表示已确认
                String notifica = " " + CommonUtil.getInviteTipContent(message.getSessionId(), attachment);
                invite_friend_img.setVisibility(View.GONE);
                SpannableString spannableStri = new SpannableString(notifica);
                spannableStri.setSpan(new ForegroundColorSpan(Color.parseColor("#BE6913")), spannableStri.length() - 5, spannableStri.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationTextView.setText(spannableStri);
                notificationTextView.setTextColor(Color.parseColor("#888888"));
                notificationTextView.setBackgroundResource(R.color.transparent);
                break;
            case TeamAuthAttachment.APPLY1:
                if (NimUIKit.getAccount().equals(fromId)) {
                    notificationTextView.setText(" 群聊邀请申请已发送，等待管理员确认 ");
                    invite_friend_img.setImageResource(R.drawable.invite_friend);
                    notificationTextView.setTextColor(Color.parseColor("#888888"));
                    notificationTextView.setBackgroundResource(R.color.transparent);
                    invite_friend_img.setVisibility(View.VISIBLE);
                } else if (member != null && (member.getType() == TeamMemberType.Owner || member.getType() == TeamMemberType.Manager)) {
                    String notification = " " + "\"" + message.getFromNick() + "\"" + "想邀请" + toIdArray.length + "位朋友加入群聊  去确认";
                    invite_friend_img.setImageResource(R.drawable.invite_friend);
                    invite_friend_img.setVisibility(View.VISIBLE);

                    SpannableString spannableString = new SpannableString(notification);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#BE6913")), spannableString.length() - 5, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    notificationTextView.setText(spannableString);
                    notificationTextView.setTextColor(Color.parseColor("#888888"));
                    notificationTextView.setBackgroundResource(R.color.transparent);
                } else {
                    setLayoutParams(0, 0, linearLayout);//其他普通成员看不到这条tip
                }
                break;
            case TeamAuthAttachment.AGREE2:
                notificationTextView.setText(" " + CommonUtil.getInviteTipContent(message.getSessionId(), attachment));
                notificationTextView.setTextColor(Color.WHITE);
                notificationTextView.setBackgroundResource(R.drawable.nim_bg_message_tip);
                invite_friend_img.setVisibility(View.GONE);
                break;
            case TeamAuthAttachment.ACCEPT3:
                notificationTextView.setText(" " + CommonUtil.getInviteTipContent(message.getSessionId(), attachment));
                notificationTextView.setTextColor(Color.WHITE);
                notificationTextView.setBackgroundResource(R.drawable.nim_bg_message_tip);
                invite_friend_img.setVisibility(View.GONE);
                break;

            case TeamAuthAttachment.CREATE_TEAM4:
                notificationTextView.setText(" " + CommonUtil.getInviteTipContent(message.getSessionId(), attachment));
                notificationTextView.setTextColor(Color.WHITE);
                notificationTextView.setBackgroundResource(R.drawable.nim_bg_message_tip);
                invite_friend_img.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected boolean shouldDisplayReceipt() {
        return false;
    }


    /**
     * ------------------------------显示样式-------------------------
     */

    @Override
    protected boolean isMiddleItem() {
        return true;
    }

    @Override
    protected boolean isShowBubble() {
        return false;
    }

    @Override
    protected boolean isShowHeadImage() {
        return false;
    }

    @Override
    protected boolean onItemLongClick() {
        return true;
    }

    @Override
    protected void onItemClick() {
        String inviteTipType = attachment.getInviteTipType();
        //普通成员申请拉人，并且当前用户为群主或者管理员的时候，这条tip才可点击，点击进去后，再进行 是否同意邀请 这个操作。同意成功后，把 "去确认"改为 "已确认"
        TeamMember member = NimUIKit.getTeamProvider().getTeamMember(message.getSessionId(), NimUIKit.getAccount());
        if ((TeamAuthAttachment.APPLY1.equals(inviteTipType) || "-1".equals(inviteTipType)) && (member.getType() == TeamMemberType.Owner || member.getType() == TeamMemberType.Manager)) {
            Intent intent = new Intent(context, InviteDetailActivity.class);
            intent.putExtra("message", message);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        }
    }

}
