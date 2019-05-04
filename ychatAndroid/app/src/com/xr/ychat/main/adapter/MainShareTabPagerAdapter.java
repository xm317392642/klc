package com.xr.ychat.main.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.xr.ychat.common.ui.viewpager.SlidingTabPagerAdapter;
import com.xr.ychat.main.fragment.MainShareTabFragment;
import com.xr.ychat.main.model.MainShareTab;

import java.util.List;

public class MainShareTabPagerAdapter extends SlidingTabPagerAdapter {

    @Override
    public int getCacheCount() {
        return MainShareTab.values().length;
    }

    public MainShareTabPagerAdapter(FragmentManager fm, Context context, ViewPager pager) {
        super(fm, MainShareTab.values().length, context.getApplicationContext(), pager);

        for (MainShareTab tab : MainShareTab.values()) {
            try {
                MainShareTabFragment fragment = null;

                List<Fragment> fs = fm.getFragments();
                if (fs != null) {
                    for (Fragment f : fs) {
                        if (f.getClass() == tab.clazz) {
                            fragment = (MainShareTabFragment) f;
                            break;
                        }
                    }
                }

                if (fragment == null) {
                    fragment = tab.clazz.newInstance();
                }

                fragment.setState(this);
                fragment.attachTabData(tab);

                fragments[tab.tabIndex] = fragment;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return MainShareTab.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        MainShareTab tab = MainShareTab.fromTabIndex(position);

        int resId = tab != null ? tab.resId : 0;

        return resId != 0 ? context.getText(resId) : "";
    }

}