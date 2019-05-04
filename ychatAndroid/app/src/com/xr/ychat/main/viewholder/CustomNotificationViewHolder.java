package com.xr.ychat.main.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ApplyLeaveTeam;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.xr.ychat.R;

/**
 * Created by huangjun on 2015/3/18.
 */
public class CustomNotificationViewHolder extends TViewHolder {

    private CustomNotification message;
    private View layout;
    private HeadImageView headImageView;
    private TextView fromAccountText;
    private TextView timeText;
    private TextView contentText;
    private View operatorLayout;
    private Button agreeButton;
    private Button rejectButton;
    private TextView operatorResultText;
    private CustomNotificationListener listener;
    private Gson gson;

    public interface CustomNotificationListener {
        void onAgree(CustomNotification message);

        void onReject(CustomNotification message);

        void onItemClick(CustomNotification message);
    }

    @Override
    protected int getResId() {
        return R.layout.message_system_notification_view_item;
    }

    @Override
    protected void inflate() {
        layout = view.findViewById(R.id.message_system_layout);
        headImageView = (HeadImageView) view.findViewById(R.id.from_account_head_image);
        fromAccountText = (TextView) view.findViewById(R.id.from_account_text);
        contentText = (TextView) view.findViewById(R.id.content_text);
        timeText = (TextView) view.findViewById(R.id.notification_time);
        operatorLayout = view.findViewById(R.id.operator_layout);
        agreeButton = (Button) view.findViewById(R.id.agree);
        rejectButton = (Button) view.findViewById(R.id.reject);
        operatorResultText = (TextView) view.findViewById(R.id.operator_result);
        view.setBackgroundResource(R.drawable.nim_list_item_bg_selecter);
    }

    @Override
    protected void refresh(Object item) {
        message = (CustomNotification) item;
        String fromAccount = message.getSessionId();
        headImageView.loadBuddyAvatar(fromAccount);
        fromAccountText.setText(UserInfoHelper.getUserDisplayNameEx(fromAccount, "我"));
        timeText.setText(TimeUtil.getNewChatTime(message.getTime()));
        if (gson == null) {
            gson = new Gson();
        }
        String content = message.getContent();
        ApplyLeaveTeam applyLeaveTeam = gson.fromJson(content, new TypeToken<ApplyLeaveTeam>() {
        }.getType());
        int teamLeaveType = applyLeaveTeam.getTeamLeaveType();
        if (teamLeaveType == 0) {
            // 未处理
            operatorLayout.setVisibility(View.VISIBLE);
            operatorResultText.setVisibility(View.GONE);
            agreeButton.setVisibility(View.VISIBLE);
            rejectButton.setVisibility(View.VISIBLE);
            contentText.setText("申请退出 " + TeamHelper.getTeamName(applyLeaveTeam.getLeaveTeamID()));
        } else if (teamLeaveType == 1) {
            // 处理结果
            operatorLayout.setVisibility(View.VISIBLE);
            agreeButton.setVisibility(View.GONE);
            rejectButton.setVisibility(View.GONE);
            operatorResultText.setVisibility(View.VISIBLE);
            operatorResultText.setText("已同意");
            operatorLayout.setVisibility(View.VISIBLE);
            contentText.setText("申请退出 " + TeamHelper.getTeamName(applyLeaveTeam.getLeaveTeamID()));
        } else if (teamLeaveType == 2) {
            // 处理结果
            agreeButton.setVisibility(View.GONE);
            rejectButton.setVisibility(View.GONE);
            operatorResultText.setVisibility(View.VISIBLE);
            operatorResultText.setText("已拒绝");
            contentText.setText("申请退出 " + TeamHelper.getTeamName(applyLeaveTeam.getLeaveTeamID()));
        } else if (teamLeaveType == 3) {
            operatorLayout.setVisibility(View.GONE);
            contentText.setText(UserInfoHelper.getUserDisplayName(fromAccount) + " 已同意你的退群申请");
        } else if (teamLeaveType == 4) {
            operatorLayout.setVisibility(View.GONE);
            contentText.setText(UserInfoHelper.getUserDisplayName(fromAccount) + " 已拒绝你的退群申请");
        } else if (teamLeaveType == 5) {
            agreeButton.setVisibility(View.GONE);
            rejectButton.setVisibility(View.GONE);
            operatorResultText.setVisibility(View.VISIBLE);
            operatorResultText.setText("已过期");
            contentText.setText("申请退出 " + TeamHelper.getTeamName(applyLeaveTeam.getLeaveTeamID()));
        }
    }

    public void refreshDirectly(final CustomNotification message) {
        if (message != null) {
            refresh(message);
        }
    }

    public void setListener(final CustomNotificationListener l) {
        if (l == null) {
            return;
        }
        this.listener = l;
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAgree(message);
            }
        });
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReject(message);
            }
        });
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(message);
            }
        });
    }

}

