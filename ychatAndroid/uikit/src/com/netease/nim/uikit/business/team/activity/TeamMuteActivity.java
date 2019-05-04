package com.netease.nim.uikit.business.team.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;

public class TeamMuteActivity extends SwipeBackUI {
    private static final String ALL_MUTE = "isAllMute";
    private static final String TEAM_ID = "teamId";
    private SwitchButton switchButton;
    private boolean isAllMute;
    private String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_mute_activity);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        isAllMute = getIntent().getBooleanExtra(ALL_MUTE, false);
        teamId = getIntent().getStringExtra(TEAM_ID);
        ((TextView) findViewById(R.id.item_title)).setText("全员禁言");
        switchButton = (SwitchButton) findViewById(R.id.setting_item_toggle);
        switchButton.setCheck(isAllMute);
        switchButton.setOnChangedListener(new SwitchButton.OnChangedListener() {
            @Override
            public void OnChanged(View v, boolean checkState) {
                if (!NetworkUtil.isNetAvailable(TeamMuteActivity.this)) {
                    failMuteTeam();
                    return;
                }
                NIMClient.getService(TeamService.class).muteAllTeamMember(teamId, checkState).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        isAllMute = checkState;
                        switchButton.setCheck(isAllMute);
                        if (isAllMute) {
                            YchatToastUtils.showShort("已开启群内禁言");
                        } else {
                            YchatToastUtils.showShort("已关闭群内禁言");
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        failMuteTeam();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        failMuteTeam();
                    }
                });
            }
        });
    }

    private void failMuteTeam() {
        switchButton.setCheck(isAllMute);
        if (!isAllMute) {
            YchatToastUtils.showShort( "开启群内禁言失败");
        } else {
            YchatToastUtils.showShort( "关闭群内禁言失败");
        }
    }

    public static void start(Context context, String teamId, boolean isAllMute) {
        Intent intent = new Intent(context, TeamMuteActivity.class);
        intent.putExtra(ALL_MUTE, isAllMute);
        intent.putExtra(TEAM_ID, teamId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
