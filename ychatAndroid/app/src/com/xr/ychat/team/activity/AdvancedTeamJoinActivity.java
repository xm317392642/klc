package com.xr.ychat.team.activity;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.session.SessionHelper;

import java.util.List;

/**
 * 申请加入群组界面
 * Created by hzxuwen on 2015/3/20.
 */
public class AdvancedTeamJoinActivity extends SwipeBackUI implements View.OnClickListener {
    private static final String EXTRA_ID = "EXTRA_ID";
    private String teamId;
    private Team team;
    private TextView teamNameText;
    private TextView memberCountText;
    private TextView teamTypeText;
    private Button applyJoinButton;
    private HeadImageView groupHead;
    private TeamDataChangedObserver teamDataChangedObserver = new TeamDataChangedObserver() {

        @Override
        public void onUpdateTeams(List<Team> teams) {
            for (Team team : teams) {
                if (TextUtils.equals(teamId, team.getId()) && TeamHelper.isTeamMember(teamId, NimUIKit.getAccount())) {
                    YchatToastUtils.showShort(getString(R.string.team_join_success, team.getName()));
                    SessionHelper.startTeamSession(AdvancedTeamJoinActivity.this, teamId);
                    finish();
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {

        }
    };

    private void registerTeamUpdateObserver(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataChangedObserver, register);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityView(R.layout.nim_advanced_team_join_activity);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());

        findViews();
        parseIntentData();
        registerTeamUpdateObserver(true);
        requestTeamInfo();
    }

    private void findViews() {
        teamNameText = (TextView) findViewById(R.id.team_name);
        groupHead = (HeadImageView) findViewById(R.id.team_head_image);
        memberCountText = (TextView) findViewById(R.id.member_count);
        applyJoinButton = (Button) findViewById(R.id.apply_join);
        teamTypeText = (TextView) findViewById(R.id.team_type);
        applyJoinButton.setOnClickListener(this);
    }

    private void parseIntentData() {
        Uri data = getIntent().getData();
        teamId = data.getQueryParameter(EXTRA_ID);
    }

    private void requestTeamInfo() {
        NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
            @Override
            public void onResult(boolean success, Team result, int code) {
                if (success && result != null) {
                    updateTeamInfo(result);
                }
            }
        });
    }

    /**
     * 更新群信息
     *
     * @param t 群
     */
    private void updateTeamInfo(final Team t) {
        if (t == null) {
            YchatToastUtils.showShort(R.string.team_not_exist);
            finish();
        } else {
            VerifyTypeEnum verifyTypeEnum = t.getVerifyType();
            applyJoinButton.setEnabled(verifyTypeEnum != VerifyTypeEnum.Private);
            applyJoinButton.setText(verifyTypeEnum != VerifyTypeEnum.Private ? getString(R.string.team_apply_to_join) : "该群无法申请加入");
            team = t;
            teamNameText.setText(team.getName());
            teamTypeText.setText("群号:");
            memberCountText.setText(team.getId());
            groupHead.loadTeamIconByTeam(team);
        }
    }

    @Override
    public void onClick(View v) {
        if (team != null) {
            final EasyEditDialog requestDialog = new EasyEditDialog(this);
            requestDialog.setTitle("申请加入群组");
            requestDialog.setEditText("我是" + UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()));
            requestDialog.addNegativeButtonListener(R.string.cancel, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestDialog.dismiss();
                }
            });
            requestDialog.addPositiveButtonListener(R.string.send, com.netease.nim.uikit.R.color.color_activity_blue_bg, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestDialog.dismiss();
                    String msg = requestDialog.getEditMessage();
                    if (TextUtils.isEmpty(msg)) {
                        msg = String.format("我是%1$s", UserInfoHelper.getUserName(DemoCache.getAccount()));
                    }
                    NIMClient.getService(TeamService.class).applyJoinTeam(team.getId(), msg).setCallback(new RequestCallback<Team>() {
                        @Override
                        public void onSuccess(Team team) {
                            applyJoinButton.setEnabled(false);
                            YchatToastUtils.showShort(getString(R.string.team_join_success, team.getName()));
                            UpdateMemberChangeService.start(AdvancedTeamJoinActivity.this, NimUIKit.getAccount(), teamId, 1, "qr");
                            NimUIKit.startTeamInfo(AdvancedTeamJoinActivity.this, teamId);
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {
                            //仅仅是申请成功
                            if (code == ResponseCode.RES_TEAM_APPLY_SUCCESS) {
                                applyJoinButton.setEnabled(false);
                                YchatToastUtils.showShort(R.string.team_apply_to_join_send_success);
                            } else if (code == ResponseCode.RES_TEAM_ALREADY_IN) {
                                applyJoinButton.setEnabled(false);
                                YchatToastUtils.showShort(R.string.has_exist_in_team);
                            } else if (code == ResponseCode.RES_TEAM_LIMIT) {
                                applyJoinButton.setEnabled(false);
                                YchatToastUtils.showShort(R.string.team_num_limit);
                            } else if (code == ResponseCode.RES_TEAM_ENACCESS) {
                                applyJoinButton.setEnabled(false);
                                YchatToastUtils.showShort("该群无法申请加入");
                            } else {
                                YchatToastUtils.showShort("failed, error code =" + code);
                            }
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                }
            });
            requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
            requestDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerTeamUpdateObserver(false);
    }

}
