package com.netease.nim.uikit.business.team.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class TeamMemberChatTimePageAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragments;

    public TeamMemberChatTimePageAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "一周不活跃";
        } else if (position == 1) {
            return "两周不活跃";
        } else {
            return "一个月不活跃";
        }
    }

}