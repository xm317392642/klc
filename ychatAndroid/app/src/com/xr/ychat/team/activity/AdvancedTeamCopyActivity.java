package com.xr.ychat.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.xr.ychat.R;
import com.xr.ychat.team.TeamCreateHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择成员复制群
 * Created by hzxuwen on 2015/3/20.
 */
public class AdvancedTeamCopyActivity extends SwipeBackUI implements View.OnClickListener {
    private static final String EXTRA_ID = "EXTRA_ID";
    private static final int REQUEST_CODE_ADVANCED = 2;//一键复制新群
    private String teamId;
    private TextView teamNameText;
    private TextView memberCountText;
    private Button applyJoinButton;
    private HeadImageView groupHead;
    private String robotId;

    public static void start(Activity activity, String teamId) {
        Intent intent = new Intent();
        intent.setClass(activity, AdvancedTeamCopyActivity.class);
        intent.putExtra(EXTRA_ID, teamId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityView(R.layout.nim_advanced_team_copy_activity);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());

        findViews();
        parseIntentData();
        requestTeamInfo();
    }

    private void findViews() {
        teamNameText = (TextView) findViewById(R.id.team_name);
        groupHead = (HeadImageView) findViewById(R.id.team_head_image);
        memberCountText = (TextView) findViewById(R.id.member_count);
        applyJoinButton = (Button) findViewById(R.id.apply_join);
        applyJoinButton.setOnClickListener(this);
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void requestTeamInfo() {
        //NimUIKit.getTeamProvider().getTeamById()
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
     * @param team 群
     */
    private void updateTeamInfo(final Team team) {
        if (team == null) {
            YchatToastUtils.showShort(R.string.team_not_exist);
            finish();
        } else {
            teamNameText.setText(team.getName());
            groupHead.loadTeamIconByTeam(team);
            robotId = getRobotId(team);
            int size = team.getMemberCount() - (TextUtils.isEmpty(robotId) ? 0 : 1);
            memberCountText.setText(size + "人");
        }
    }

    private String getRobotId(Team team) {
        TeamExtension extension;
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                Gson gson = new Gson();
                extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
            } catch (Exception exception) {
                extension = new TeamExtension();
            }
        } else {
            extension = new TeamExtension();
        }
        return extension.getRobotId();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_ADVANCED) {
            ArrayList<String> memberAccounts = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            TeamCreateHelper.createAdvancedTeam(this, memberAccounts);
        }
    }

    @Override
    public void onClick(View v) {
        List<TeamMember> teamMembers = NimUIKit.getTeamProvider().getTeamMemberList(teamId);
        ArrayList<String> memberAccounts = new ArrayList<>(teamMembers.size());
        for (TeamMember teamMember : teamMembers) {
            if (NimUIKit.getAccount().equals(teamMember.getAccount())) {
                continue;//是自己的话，就执行下一次
            }
            if (TextUtils.isEmpty(robotId) || !TextUtils.equals(robotId, teamMember.getAccount())) {
                memberAccounts.add(teamMember.getAccount());
            }
        }
        ContactSelectActivity.Option advancedOption = TeamHelper.getCopySelectOption(teamId, memberAccounts, robotId);
        NimUIKit.startContactSelector(this, advancedOption, REQUEST_CODE_ADVANCED);
    }
}
