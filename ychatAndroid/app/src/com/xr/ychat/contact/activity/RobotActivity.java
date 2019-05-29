package com.xr.ychat.contact.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.RobotInfo;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.xr.ychat.R;

import java.util.ArrayList;
import java.util.List;

public class RobotActivity extends SwipeBackUI implements RobotDetailAdapter.RobotInteface {
    private static final String TEAM_ID = "teamId";
    private static final String TEAM_CREATOR = "teamCreator";
    private static final String TEAM_EXTENTION = "teamExtention";
    public static final String EXTRA_ACCID = "extra_accid";
    public static final String EXTRA_NEW_ACCID = "extra_new_accid";
    public static final int APPEND_ROBOT = 0x01;
    private String teamId;
    private String teamCreator;
    private String teamExtention;
    private String uid;
    private String mytoken;
    private RecyclerView recyclerView;
    private RobotDetailAdapter adapter;
    private List<RobotInfo> robotInfos;
    private LayoutInflater layoutInflater;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_append_robot);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("机器人");
        layoutInflater = LayoutInflater.from(RobotActivity.this);
        gson = new Gson();
        Uri uri = getIntent().getData();
        teamId = uri.getQueryParameter(TEAM_ID);
        teamCreator = uri.getQueryParameter(TEAM_CREATOR);
        teamExtention = uri.getQueryParameter(TEAM_EXTENTION);
        uid = Preferences.getWeiranUid(this);
        mytoken = Preferences.getWeiranToken(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        initAdapter(recyclerView);
        queryTeamRobot();
    }

    private void initAdapter(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(RobotActivity.this));
        robotInfos = new ArrayList<>();
        adapter = new RobotDetailAdapter(recyclerView, robotInfos, this, teamId);
        recyclerView.setAdapter(adapter);
    }

    private void queryTeamRobot() {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().queryTeamRobot(uid, mytoken, teamId, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (aVoid.getList() != null && aVoid.getList().size() > 0) {
                    robotInfos.clear();
                    robotInfos.addAll(aVoid.getList());
                    adapter.setNewData(robotInfos);
                    adapter.setEnableLoadMore(false);
                    adapter.loadMoreEnd();
                } else {
                    handlerError();
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                handlerError();
            }
        });
    }

    private void handlerError() {
        adapter.setNewData(null);
        View emptyView = layoutInflater.inflate(R.layout.robot_empty, null);
        Button append = emptyView.findViewById(R.id.append);
        append.setOnClickListener(v -> {
            RobotAppendActivity.start(RobotActivity.this, null, APPEND_ROBOT);
        });
        adapter.setEmptyView(emptyView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case APPEND_ROBOT:
                    String newaccid = data.getStringExtra(EXTRA_NEW_ACCID);
                    String accid = data.getStringExtra(EXTRA_ACCID);
                    if (TextUtils.isEmpty(accid)) {
                        addTeamRobot(newaccid);
                    } else {
                        removeTeamRobot(accid, newaccid);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void switchRobot(String accid) {
        RobotAppendActivity.start(RobotActivity.this, accid, APPEND_ROBOT);
    }

    @Override
    public void removeRobot(String accid) {
        removeTeamRobot(accid, "");
    }

    private void addTeamRobot(String accid) {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "正在添加机器人", false);
        ContactHttpClient.getInstance().addTeamRobot(uid, mytoken, teamId, accid, teamCreator, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                synExtensionToServer(true, accid);
                queryTeamRobot();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("添加机器人失败");
            }
        });
    }

    private void removeTeamRobot(String accid, String newaccid) {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "正在删除机器人", false);
        ContactHttpClient.getInstance().removeTeamRobot(uid, mytoken, teamId, accid, teamCreator, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                synExtensionToServer(false, accid);
                robotInfos.clear();
                adapter.setNewData(robotInfos);
                adapter.notifyDataSetChanged();
                handlerError();
                if (!TextUtils.isEmpty(newaccid)) {
                    addTeamRobot(newaccid);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("删除机器人失败");
            }
        });
    }

    private void synExtensionToServer(boolean isAdd, String value) {
        TeamExtension extension;
        if (!TextUtils.isEmpty(teamExtention)) {
            try {
                extension = gson.fromJson(teamExtention, new TypeToken<TeamExtension>() {
                }.getType());
            } catch (Exception exception) {
                extension = new TeamExtension();
            }
        } else {
            extension = new TeamExtension();
        }
        extension.setExtensionType(4);
        if (isAdd) {
            extension.setRobotId(value);
        } else {
            extension.setRobotId("");
        }
        String extensionString = gson.toJson(extension);
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.Extension, extensionString).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                teamExtention = extensionString;
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(Throwable exception) {
            }
        });
    }
}
