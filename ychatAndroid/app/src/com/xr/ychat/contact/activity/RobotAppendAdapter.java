package com.xr.ychat.contact.activity;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.netease.nim.uikit.common.RobotInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.widget.RoundRectTextView;
import com.xr.ychat.R;

import java.util.List;

public class RobotAppendAdapter extends BaseQuickAdapter<RobotInfo, BaseViewHolder> {
    private HeadImageView headImageView;
    private TextView accountText;
    private AppendRobotInteface inteface;
    private RoundRectTextView addRobot;

    public RobotAppendAdapter(RecyclerView recyclerView, List<RobotInfo> data, AppendRobotInteface inteface) {
        super(recyclerView, R.layout.robot_append_item, data);
        this.inteface = inteface;
    }

    @Override
    protected void convert(BaseViewHolder helper, RobotInfo item, int position, boolean isScrolling) {
        headImageView = helper.getView(R.id.robot_avatar);
        headImageView.loadAvatar(item.getIcon());
        accountText = helper.getView(R.id.robot_name);
        accountText.setText(item.getName());
        addRobot = helper.getView(R.id.robot_append);
        addRobot.setOnClickListener(v -> {
            if (inteface != null) {
                inteface.appendRobot(item.getAccid());
            }
        });
    }

    public interface AppendRobotInteface {

        void appendRobot(String accid);
    }

}
