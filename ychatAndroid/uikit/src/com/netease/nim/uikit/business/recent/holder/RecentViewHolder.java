package com.netease.nim.uikit.business.recent.holder;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.recent.RecentContactsCallback;
import com.netease.nim.uikit.business.recent.RecentContactsFragment;
import com.netease.nim.uikit.business.recent.adapter.RecentContactAdapter;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ui.drop.DropFake;
import com.netease.nim.uikit.common.ui.drop.DropManager;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;
import java.util.Map;

import static com.netease.nim.uikit.business.recent.RecentContactsFragment.RECENT_TAG_SELECTOR;

public abstract class RecentViewHolder extends RecyclerViewHolder<BaseQuickAdapter, BaseViewHolder, RecentContact> {

    public RecentViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    private int lastUnreadCount = 0;

    protected HeadImageView imgHead;

    protected TextView tvNickname;

    protected TextView tvMessage;

    protected TextView tvDatetime;

    // 消息发送错误状态标记，目前没有逻辑处理
    protected ImageView imgMsgStatus;

    // 未读红点（一个占坑，一个全屏动画）
    protected DropFake tvUnread;

    private ImageView imgUnreadExplosion;

    private ImageView imgUnreadIndicator;

    protected TextView tvOnlineState;

    // 子类覆写
    protected abstract String getContent(RecentContact recent);

    @Override
    public void convert(BaseViewHolder holder, RecentContact data, int position, boolean isScrolling) {
        inflate(holder, data);
        refresh(holder, data, position);
    }

    public void inflate(BaseViewHolder holder, final RecentContact recent) {
        this.imgHead = holder.getView(R.id.img_head);
        this.tvNickname = holder.getView(R.id.tv_nickname);
        this.tvMessage = holder.getView(R.id.tv_message);
        this.tvUnread = holder.getView(R.id.unread_number_tip);
        this.imgUnreadExplosion = holder.getView(R.id.unread_number_explosion);
        this.imgUnreadIndicator = holder.getView(R.id.unread_number_indicator);
        this.tvDatetime = holder.getView(R.id.tv_date_time);
        this.imgMsgStatus = holder.getView(R.id.img_msg_status);
        this.tvOnlineState = holder.getView(R.id.tv_online_state);
        holder.addOnClickListener(R.id.unread_number_tip);
        this.tvUnread.setTouchListener(new DropFake.ITouchListener() {
            @Override
            public void onDown() {
                DropManager.getInstance().setCurrentId(recent);
                DropManager.getInstance().down(tvUnread, tvUnread.getText());
            }

            @Override
            public void onMove(float curX, float curY) {
                DropManager.getInstance().move(curX, curY);
            }

            @Override
            public void onUp() {
                DropManager.getInstance().up();
            }
        });
    }

    public void refresh(BaseViewHolder holder, RecentContact recent, final int position) {
        // unread count animation
        boolean shouldBoom = lastUnreadCount > 0 && recent.getUnreadCount() == 0; // 未读数从N->0执行爆裂动画;
        lastUnreadCount = recent.getUnreadCount();

        updateBackground(holder, recent, position);

        loadPortrait(holder, recent);
        updateNickLabel(recent, holder.getContext());

        updateOnlineState(recent);

        updateMsgLabel(holder, recent);

        updateNewIndicator(recent);

        if (shouldBoom) {
            Object o = DropManager.getInstance().getCurrentId();
            if (o instanceof String && o.equals("0")) {
                imgUnreadExplosion.setImageResource(R.drawable.nim_explosion);
                imgUnreadExplosion.setVisibility(View.VISIBLE);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ((AnimationDrawable) imgUnreadExplosion.getDrawable()).start();
                        // 解决部分手机动画无法播放的问题（例如华为荣耀）
                        getAdapter().notifyItemChanged(getAdapter().getViewHolderPosition(position));
                    }
                });
            }
        } else {
            imgUnreadExplosion.setVisibility(View.GONE);
        }
    }

    private void updateBackground(BaseViewHolder holder, RecentContact recent, int position) {
        if ((recent.getTag() & RecentContactsFragment.RECENT_TAG_STICKY) == 0) {
            Map<String, Object> map = recent.getExtension();
            if (map != null) {
                Integer integer = (Integer) map.get(RECENT_TAG_SELECTOR);
                if (integer != null && integer == 1) {
                    holder.getConvertView().setBackgroundResource(R.drawable.nim_recent_contact_sticky_selecter);
                } else {
                    holder.getConvertView().setBackgroundResource(R.drawable.nim_recent_contact_background);
                }
            } else {
                holder.getConvertView().setBackgroundResource(R.drawable.nim_recent_contact_background);
            }
        } else {
            holder.getConvertView().setBackgroundResource(R.drawable.nim_recent_contact_sticky_selecter);
        }
    }

    protected void loadPortrait(BaseViewHolder holder, RecentContact recent) {
        // 设置头像
        if (recent.getSessionType() == SessionTypeEnum.P2P) {
            if (TextUtils.equals(recent.getContactId(), CommonUtil.ASSISTANT_ACCOUNT)) {
                imgHead.setImageResource(R.drawable.nim_avatar_assistant);
            } else {
                imgHead.loadBuddyAvatar(recent.getContactId());
            }
        } else if (recent.getSessionType() == SessionTypeEnum.Team) {
            String teamId = recent.getContactId();
            NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
                @Override
                public void onResult(boolean success, List<TeamMember> result, int code) {
                    imgHead.loadTeamIconByTeam(result, teamId);
                }
            });
        }
    }

    private boolean needRefresh(List<TeamMember> teamMembers) {
        if (teamMembers == null) {
            return true;
        }
        if (teamMembers.size() == 0) {
            return true;
        }
        if (teamMembers.size() == 1) {
            TeamMember teamMember = teamMembers.get(0);
            if (teamMember.getType() == TeamMemberType.Owner) {
                return true;
            }
        }
        boolean hasOwner = true;
        for (TeamMember teamMember : teamMembers) {
            if (teamMember.getType() == TeamMemberType.Owner) {
                hasOwner = false;
                break;
            }
        }
        return hasOwner;
    }

    private void updateNewIndicator(RecentContact recent) {
        int unreadNum = recent.getUnreadCount();
        if (isNeedMessageNotify(recent)) {
            tvUnread.setVisibility(unreadNum > 0 ? View.VISIBLE : View.GONE);
            tvUnread.setText(unreadCountShowRule(unreadNum));
            imgUnreadIndicator.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.GONE);
            imgUnreadIndicator.setVisibility(unreadNum > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void updateMsgLabel(BaseViewHolder holder, RecentContact recent) {
        imgMsgStatus.setVisibility(View.GONE);
        if (recent.getSessionType() == SessionTypeEnum.Team) {
            TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(recent.getContactId(), NimUIKit.getAccount());
            if (teamMember != null) {
                Map<String, Object> map = teamMember.getExtension();
                if (map != null && map.containsKey(TeamExtras.FIRE_MSG) && TeamExtras.OPEN.equals(map.get(TeamExtras.FIRE_MSG))) {
                    tvMessage.setText("[ 阅后即焚 ]");
                } else {
                    MoonUtil.identifyRecentVHFaceExpressionAndTags(holder.getContext(), tvMessage, getContent(recent), -1, 0.45f);
                }
            } else {
                MoonUtil.identifyRecentVHFaceExpressionAndTags(holder.getContext(), tvMessage, getContent(recent), -1, 0.45f);
            }
        } else {
            MoonUtil.identifyRecentVHFaceExpressionAndTags(holder.getContext(), tvMessage, getContent(recent), -1, 0.45f);
        }
        // 显示消息具体内容
        //tvMessage.setText(getContent());
        MsgStatusEnum status = recent.getMsgStatus();
        switch (status) {
            case fail:
                imgMsgStatus.setImageResource(R.drawable.nim_g_ic_failed_small);
                imgMsgStatus.setVisibility(View.VISIBLE);
                break;
            case sending:
                imgMsgStatus.setImageResource(R.drawable.nim_recent_contact_ic_sending);
                imgMsgStatus.setVisibility(View.VISIBLE);
                break;
            default: {
                if (isNeedMessageNotify(recent)) {
                    imgMsgStatus.setVisibility(View.GONE);
                } else {
                    imgMsgStatus.setImageResource(R.drawable.nim_g_ic_no_disturbing);
                    imgMsgStatus.setVisibility(View.VISIBLE);
                }
            }
            break;
        }
        //String timeString = TimeUtil.getFriendlyTimeSpanByNow(recent.getTime());
        //String timeString = TimeUtil.getNewChatTime(recent.getTime());
        String timeString = TimeUtil.getNewChatTimeInSesstionList(recent.getTime());

        tvDatetime.setText(timeString);
    }

    private boolean isNeedMessageNotify(RecentContact recent) {
        if (recent.getSessionType() == SessionTypeEnum.P2P) {
            return NIMClient.getService(FriendService.class).isNeedMessageNotify(recent.getContactId());
        } else {
            Team t = NimUIKit.getTeamProvider().getTeamById(recent.getContactId());
            return t != null && t.getMessageNotifyType() == TeamMessageNotifyTypeEnum.All;
        }
    }

    protected String getOnlineStateContent(RecentContact recent) {
        return "";
    }

    protected void updateOnlineState(RecentContact recent) {
        if (recent.getSessionType() == SessionTypeEnum.Team) {
            tvOnlineState.setVisibility(View.GONE);
        } else {
            String onlineStateContent = getOnlineStateContent(recent);
            if (TextUtils.isEmpty(onlineStateContent)) {
                tvOnlineState.setVisibility(View.GONE);
            } else {
                tvOnlineState.setVisibility(View.VISIBLE);
                tvOnlineState.setText(getOnlineStateContent(recent));
            }
        }
    }

    protected void updateNickLabel(RecentContact recent, Context context) {
        int labelWidth = ScreenUtil.screenWidth;
        labelWidth -= ScreenUtil.dip2px(50 + 70); // 减去固定的头像和时间宽度
        if (labelWidth > 0) {
            tvNickname.setMaxWidth(labelWidth);
        }
        if (!TextUtils.equals(recent.getContactId(), CommonUtil.ASSISTANT_ACCOUNT)) {
            tvNickname.setText(UserInfoHelper.getUserTitleName(recent.getContactId(), recent.getSessionType()));
        } else {
            tvNickname.setText("空了吹小助手");
        }
        if (recent.getSessionType() == SessionTypeEnum.Team) {
            TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(recent.getContactId(), NimUIKit.getAccount());
            if (teamMember != null) {
                Map<String, Object> map = teamMember.getExtension();
                if (map == null) {
                    tvNickname.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                } else if (TeamExtras.OPEN.equals(map.get(TeamExtras.FIRE_MSG))) {
                    tvNickname.setCompoundDrawablePadding(ScreenUtil.dip2px(3f));
                    tvNickname.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.nim_burn), null);
                } else {
                    tvNickname.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            } else {
                tvNickname.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
        } else {
            tvNickname.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    protected String unreadCountShowRule(int unread) {
        return unread > 99 ? "99+" : String.valueOf(unread);
    }

    protected RecentContactsCallback getCallback() {
        return ((RecentContactAdapter) getAdapter()).getCallback();
    }
}
