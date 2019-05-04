package com.xr.ychat.redpacket;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class RedpacketRecordAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragments;
    private Context context;

    public RedpacketRecordAdapter(Context context, FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.context = context;
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
            return "收到的红包";
        } else {
            return "发出的红包";
        }
    }

}