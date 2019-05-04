package com.netease.nim.uikit.business.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.ait.selector.AitContactDecoration;
import com.netease.nim.uikit.business.ait.selector.model.ItemType;
import com.netease.nim.uikit.business.team.adapter.TeamMemberChangeAdapter;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class TeamMemberChangeActivity extends SwipeBackUI {
    public static final String EXTRA_TEAM = "team";
    private TextView toolbarAction;
    private String uid;
    private String myToken;
    private String teamID;
    private RecyclerView recyclerView;
    private TeamMemberChangeAdapter adapter;
    private List<RedpacketInfo> items;
    private int pageNumber;
    private View emptyView;
    private TextView content;

    public static void start(Context context, String team) {
        Intent intent = new Intent(context, TeamMemberChangeActivity.class);
        intent.putExtra(EXTRA_TEAM, team);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_team_member_change_activity);
        initToolbar();
        initAdapter();
        uid = Preferences.getWeiranUid(this);
        myToken = Preferences.getWeiranToken(this);
        teamID = getIntent().getStringExtra(EXTRA_TEAM);
        pageNumber = 0;
        teamMemberChangeLog(uid, myToken, teamID, pageNumber);
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarAction = (TextView) findViewById(R.id.action_bar_right_clickable_textview);
        toolbarAction.setOnClickListener(v -> {
            clearTeamMemberChangeLog(uid, myToken, teamID);
        });
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void initAdapter() {
        emptyView = LayoutInflater.from(TeamMemberChangeActivity.this).inflate(R.layout.empty_team_member_change, null);
        content = emptyView.findViewById(R.id.content);
        content.setText("没有群成员变动记录");
        items = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeamMemberChangeAdapter(recyclerView, items);
        recyclerView.setAdapter(adapter);
        List<Integer> noDividerViewTypes = new ArrayList<>(1);
        noDividerViewTypes.add(ItemType.SIMPLE_LABEL);
        recyclerView.addItemDecoration(new AitContactDecoration(TeamMemberChangeActivity.this, LinearLayoutManager.VERTICAL, noDividerViewTypes));
        adapter.setOnLoadMoreListener(() -> {
            teamMemberChangeLog(uid, myToken, teamID, pageNumber++);
        });
    }

    /**
     * 群记录（入群 退群日志）
     */
    private void teamMemberChangeLog(String uid, String mytoken, String qunID, int page) {
        if (!NetworkUtil.isNetAvailable(TeamMemberChangeActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.getInstance().teamMemberChangeLog(uid, mytoken, qunID, page, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                if (aVoid.getData() != null && aVoid.getData().size() > 0) {
                    if (page == 0) {
                        adapter.setNewData(aVoid.getData());
                    } else {
                        adapter.addData(aVoid.getData());
                    }
                    boolean hasNextPage = aVoid.getData().size() >= 10;
                    adapter.setEnableLoadMore(hasNextPage);
                    if (hasNextPage) {
                        adapter.loadMoreComplete();
                    } else {
                        adapter.loadMoreEnd();
                    }
                } else {
                    if (page == 0) {
                        adapter.setNewData(null);
                        adapter.setEmptyView(emptyView);
                    } else {
                        adapter.loadMoreEnd();
                    }
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                handlerError(page);
            }
        });
    }

    private void handlerError(int page) {
        if (page == 0) {
            adapter.setNewData(null);
            adapter.setEmptyView(emptyView);
        } else {
            adapter.loadMoreFail();
        }
    }

    /**
     * 清空群记录（入群 退群日志）
     */
    private void clearTeamMemberChangeLog(String uid, String mytoken, String qunID) {
        if (!NetworkUtil.isNetAvailable(TeamMemberChangeActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "正在清空群记录", false);
        ContactHttpClient.getInstance().clearTeamMemberChangeLog(uid, mytoken, qunID, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                adapter.setNewData(null);
                adapter.setEmptyView(emptyView);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

}
