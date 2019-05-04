package com.xr.ychat.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.common.ApplyLeaveTeam;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.xr.ychat.R;
import com.xr.ychat.main.adapter.SystemNotificationPageAdapter;
import com.xr.ychat.main.fragment.CustomMessageFragment;
import com.xr.ychat.main.fragment.SystemMessageFragment;
import com.xr.ychat.main.helper.CustomNotificationCache;

import java.util.ArrayList;
import java.util.List;

public class SystemNotificationActivity extends SwipeBackUI {
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ImageView unreadIndicator;
    private SystemMessageFragment systemMessageFragment;
    private CustomMessageFragment customMessageFragment;
    private List<Fragment> mFragments;
    private LocalBroadcastManager localBroadcastManager;
    private Gson gson;

    public static void start(Context context) {
        Intent intent = new Intent(context, SystemNotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_notification);
        initToolbar();
        initViewPager();
        unreadIndicator = (ImageView) findViewById(R.id.unread_number_indicator);
        unreadIndicator.setVisibility(hasCustomMessage() ? View.VISIBLE : View.GONE);
        localBroadcastManager = LocalBroadcastManager.getInstance(SystemNotificationActivity.this);
        gson = new Gson();
    }

    private boolean hasCustomMessage() {
        boolean hasCustomMessage = false;
        Gson gson = new Gson();
        List<CustomNotification> datas = CustomNotificationCache.getDataList();
        if (datas != null && datas.size() > 0) {
            for (CustomNotification customNotification : datas) {
                if (!TextUtils.isEmpty(customNotification.getContent())) {
                    ApplyLeaveTeam leaveTeam = gson.fromJson(customNotification.getContent(), new TypeToken<ApplyLeaveTeam>() {
                    }.getType());
                    if (!leaveTeam.isHasRead()) {
                        hasCustomMessage = true;
                        break;
                    }
                }
            }
        }
        return hasCustomMessage;
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(com.netease.nim.uikit.R.id.toolbar);
        mToolbar.setNavigationIcon(com.netease.nim.uikit.R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        TextView action = (TextView) findViewById(com.netease.nim.uikit.R.id.action_bar_right_clickable_textview);
        action.setOnClickListener(v -> {
            Intent intent = new Intent("com.xr.ychat.ClearNotificationReceiver");
            localBroadcastManager.sendBroadcast(intent);
        });
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(com.netease.nim.uikit.R.id.team_member_chat_time_content);
        mTabLayout = (TabLayout) findViewById(com.netease.nim.uikit.R.id.tab_layout);
        mFragments = new ArrayList<>();
        systemMessageFragment = SystemMessageFragment.newInstance();
        mFragments.add(systemMessageFragment);
        customMessageFragment = CustomMessageFragment.newInstance();
        mFragments.add(customMessageFragment);
        mViewPager.setAdapter(new SystemNotificationPageAdapter(this, getSupportFragmentManager(), mFragments));
        mViewPager.setCurrentItem(0);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                unreadIndicator.setVisibility(View.GONE);
                List<CustomNotification> cache = CustomNotificationCache.getDataList();
                if (!cache.isEmpty()) {
                    for (CustomNotification customNotification : cache) {
                        ApplyLeaveTeam applyLeaveTeam = gson.fromJson(customNotification.getContent(), new TypeToken<ApplyLeaveTeam>() {
                        }.getType());
                        if (!applyLeaveTeam.isHasRead()) {
                            applyLeaveTeam.setHasRead(true);
                            customNotification.setContent(gson.toJson(applyLeaveTeam));
                        }
                    }
                    CustomNotificationCache.setDataList(cache);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent("com.xr.ychat.ClearSystemCountBroadcastReceiver");
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent("com.xr.ychat.ClearSystemCountBroadcastReceiver");
        localBroadcastManager.sendBroadcast(intent);
    }

}