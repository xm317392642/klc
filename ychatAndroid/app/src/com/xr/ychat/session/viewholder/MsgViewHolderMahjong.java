package com.xr.ychat.session.viewholder;

import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.Mahjong;
import com.netease.nim.uikit.common.MahjongBean;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.xr.ychat.R;
import com.xr.ychat.session.extension.MahjongAttachment;

import java.util.ArrayList;

public class MsgViewHolderMahjong extends MsgViewHolderBase {
    private TextView name;
    private TextView tid;
    private TextView time;
    private RelativeLayout one;
    private RelativeLayout two;
    private RelativeLayout three;
    private RelativeLayout four;

    public MsgViewHolderMahjong(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.robot_chat_item;
    }

    @Override
    protected void inflateContentView() {
        name = (TextView) view.findViewById(R.id.room_party);
        tid = (TextView) view.findViewById(R.id.room_tid);
        time = (TextView) view.findViewById(R.id.room_time);
        one = (RelativeLayout) view.findViewById(R.id.user_one_layout);
        two = (RelativeLayout) view.findViewById(R.id.user_two_layout);
        three = (RelativeLayout) view.findViewById(R.id.user_three_layout);
        four = (RelativeLayout) view.findViewById(R.id.user_four_layout);
    }

    @Override
    protected void bindContentView() {
        if (message.getAttachment() == null) {
            return;
        }
        MahjongAttachment mahjongAttachment = (MahjongAttachment) message.getAttachment();
        Mahjong mahjong = mahjongAttachment.getMahjong();
        name.setText(mahjong.getGame_name());
        tid.setText("" + mahjong.getTid());
        String text = TimeUtils.millis2String(mahjong.getTime() * 1000L);
        time.setText(text);
        ArrayList<MahjongBean> beans = mahjong.getPlayer();
        int size = beans.size();
        if (size >= 4) {
            disItem(four, beans.get(3));
        }
        if (size >= 3) {
            disItem(three, beans.get(2));
        }
        if (size >= 2) {
            disItem(two, beans.get(1));
        }
        if (size >= 1) {
            disItem(one, beans.get(0));
        }
    }

    private void disItem(RelativeLayout layout, MahjongBean bean) {
        if (bean != null) {
            layout.setVisibility(View.VISIBLE);
            HeadImageView headImageView = layout.findViewById(R.id.user_avatar);
            TextView userName = layout.findViewById(R.id.user_nickname);
            TextView userScore = layout.findViewById(R.id.user_score);
            TextView UserId = layout.findViewById(R.id.user_id);
            String icon = bean.getIcon();
            if (!TextUtils.isEmpty(icon)) {
                if (!icon.startsWith("http://")) {
                    icon = "http://" + icon;
                }
                headImageView.loadAvatar(icon);
            }
            userName.setText(bean.getName());
            UserId.setText("ID: " + bean.getUid());
            int result = bean.getResult();
            if (result >= 0) {
                userScore.setTextColor(ContextCompat.getColor(context, R.color.color_f25542));
            } else {
                userScore.setTextColor(ContextCompat.getColor(context, R.color.color_1485ef));
            }
            userScore.setText("分数: " + result);
        }
    }

    @Override
    protected void onItemClick() {

    }
}
