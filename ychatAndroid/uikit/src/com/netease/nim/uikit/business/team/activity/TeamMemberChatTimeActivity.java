package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.adapter.TeamMemberChatTimeAdapter;
import com.netease.nim.uikit.business.team.adapter.TeamMemberChatTimePageAdapter;
import com.netease.nim.uikit.business.team.ui.TeamMemberChatTimeFragment;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.util.YchatToastUtils;

import java.util.ArrayList;
import java.util.List;

public class TeamMemberChatTimeActivity extends SwipeBackUI implements TeamMemberChatTimeAdapter.TeamMemberChatTimeMultiple {
    public static final String EXTRA_TEAM = "team";
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private LinearLayout bottomLayout;
    private TextView multipleChoice;
    private TextView deleteChoice;
    private TextView action;
    private TeamMemberChatTimeFragment oneWeekendFragment;
    private TeamMemberChatTimeFragment twoWeekendsFragment;
    private TeamMemberChatTimeFragment oneMonthFragment;
    private List<Fragment> mFragments;
    private String teamId;
    private boolean multipleChoiceMode = false;
    private boolean allChoice = false;
    private LocalBroadcastManager localBroadcastManager;
    private ArrayList<String> accounts;
    private int oneWeekendNumber;
    private int twoWeekendsNumber;
    private int oneMonthNumber;

    public static void start(Activity context, String team, int requestCode) {
        Intent intent = new Intent(context, TeamMemberChatTimeActivity.class);
        intent.putExtra(EXTRA_TEAM, team);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_team_member_chat_time_activity);
        initToolbar();
        initViewPager();
        initBottomLayout();
        accounts = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(TeamMemberChatTimeActivity.this);
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        action = (TextView) findViewById(R.id.action_bar_right_clickable_textview);
        action.setOnClickListener(v -> {
            allChoice = false;
            multipleChoice.setText("全选");
            if (multipleChoiceMode) {
                bottomLayout.setVisibility(View.GONE);
                multipleChoiceMode = false;
                action.setText("多选");
                accounts.clear();
                sendCommand(mTabLayout.getSelectedTabPosition(), 0);
            } else {
                bottomLayout.setVisibility(View.VISIBLE);
                multipleChoiceMode = true;
                action.setText("取消");
                sendCommand(mTabLayout.getSelectedTabPosition(), 1);
            }
        });
        teamId = getIntent().getStringExtra(EXTRA_TEAM);
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.team_member_chat_time_content);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mFragments = new ArrayList<>();
        oneWeekendFragment = TeamMemberChatTimeFragment.newInstance(1, teamId);
        mFragments.add(oneWeekendFragment);
        twoWeekendsFragment = TeamMemberChatTimeFragment.newInstance(2, teamId);
        mFragments.add(twoWeekendsFragment);
        oneMonthFragment = TeamMemberChatTimeFragment.newInstance(3, teamId);
        mFragments.add(oneMonthFragment);
        mViewPager.setAdapter(new TeamMemberChatTimePageAdapter(getSupportFragmentManager(), mFragments));
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                bottomLayout.setVisibility(View.GONE);
                multipleChoiceMode = false;
                allChoice = false;
                action.setText("多选");
                accounts.clear();
                multipleChoice.setText("全选");
                sendCommand(tab.getPosition(), 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initBottomLayout() {
        bottomLayout = (LinearLayout) findViewById(R.id.team_member_chat_time_layout);
        bottomLayout.setVisibility(View.GONE);
        multipleChoice = (TextView) findViewById(R.id.team_member_chat_time_multiple_choice);
        multipleChoice.setOnClickListener(v -> {
            if (allChoice) {
                allChoice = false;
                multipleChoice.setText("全选");
                sendCommand(mTabLayout.getSelectedTabPosition(), 1);
            } else {
                allChoice = true;
                multipleChoice.setText("取消全选");
                sendCommand(mTabLayout.getSelectedTabPosition(), 2);
            }
        });
        deleteChoice = (TextView) findViewById(R.id.team_member_chat_time_delete);
        deleteChoice.setOnClickListener(v -> {
            if (accounts.size() > 0) {
                YchatToastUtils.showShort(accounts.toString());
                Intent intent = new Intent();
                intent.putExtra(ContactSelectActivity.RESULT_DATA, accounts);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                YchatToastUtils.showShort("没有选中群成员");
            }
        });
    }

    @Override
    public void onCheckedChanged(String accid, boolean isChecked) {
        if (isChecked) {
            if (!accounts.contains(accid)) {
                accounts.add(accid);
                int number;
                int position = mTabLayout.getSelectedTabPosition();
                if (position == 0) {
                    number = oneWeekendNumber;
                } else if (position == 1) {
                    number = twoWeekendsNumber;
                } else {
                    number = oneMonthNumber;
                }
                if (accounts.size() == number && !allChoice) {
                    allChoice = true;
                    multipleChoice.setText("取消全选");
                }
            }
        } else {
            if (accounts.contains(accid)) {
                accounts.remove(accid);
                if (allChoice) {
                    allChoice = false;
                    multipleChoice.setText("全选");
                }
            }
        }
    }

    @Override
    public void updateMemberNumber(int type, int number) {
        if (type == 1) {
            oneWeekendNumber = number;
        } else if (type == 2) {
            twoWeekendsNumber = number;
        } else {
            oneMonthNumber = number;
        }
    }

    //multipleChoiceMode 0取消多选 1多选下一个都不选 2多选下全选
    private void sendCommand(int position, int multipleChoiceMode) {
        Intent intent = new Intent("com.xr.ychat.TeamMemberChatTimeBroadcastReceiver");
        intent.putExtra("Action", position + 1);
        intent.putExtra("MultipleChoice", multipleChoiceMode);
        localBroadcastManager.sendBroadcast(intent);
    }

}

