package com.xr.ychat.contact.activity;

import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nim.uikit.common.RobotInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.xr.ychat.R;
import com.xr.ychat.main.activity.HelpActivity;

import java.util.List;

public class RobotDetailAdapter extends BaseQuickAdapter<RobotInfo, BaseViewHolder> {
    private HeadImageView headImageView;
    private TextView accountText;
    private RobotInteface inteface;
    private TextView removeRobot;
    private TextView switchRobot;
    private TextView robotId;
    private Button robotDetail;
    private String teamId;

    public RobotDetailAdapter(RecyclerView recyclerView, List<RobotInfo> data, RobotInteface inteface, String teamId) {
        super(recyclerView, R.layout.robot_detail_item, data);
        this.inteface = inteface;
        this.teamId = teamId;
    }

    @Override
    protected void convert(BaseViewHolder helper, RobotInfo item, int position, boolean isScrolling) {
        headImageView = helper.getView(R.id.robot_avatar);
        headImageView.loadAvatar(item.getIcon());
        accountText = helper.getView(R.id.robot_name);
        accountText.setText(item.getName());
        robotId = helper.getView(R.id.robot_id);
        robotId.setText("机器人ID: " + item.getAccid());
        switchRobot = helper.getView(R.id.robot_switch);
        switchRobot.setOnClickListener(v -> {
            inteface.switchRobot(item.getAccid());
        });
        removeRobot = helper.getView(R.id.robot_remove);
        removeRobot.setOnClickListener(v -> {
            inteface.removeRobot(item.getAccid());
        });
        robotDetail = helper.getView(R.id.robot_detail);
        robotDetail.setOnClickListener(v -> {
            //String url = "http://39.105.35.71:8021/dist/#/home?qunid=" + teamId;
            String url = item.getLink() + "/dist/#/home?qunid=" + teamId;
            HelpActivity.start(helper.getContext(), url, "第三方机器人服务");
        });
    }

    public interface RobotInteface {
        void removeRobot(String accid);

        void switchRobot(String accid);
    }

}
