package com.netease.nim.uikit.common.swipebacklayout.lib.app;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.blankj.utilcode.util.KeyboardUtils;
import com.netease.nim.uikit.common.swipebacklayout.lib.SwipeBackLayout;
import com.netease.nim.uikit.common.swipebacklayout.lib.Utils;

import java.lang.ref.WeakReference;


/**
 * Created by laysionqet on 2018/4/24.
 */
public class SwipeBackListenerActivityAdapter implements SwipeBackLayout.SwipeListenerEx {
    private final WeakReference<Activity> mActivity;

    public SwipeBackListenerActivityAdapter(@NonNull Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void onScrollStateChange(int state, float scrollPercent) {

    }

    @Override
    public void onEdgeTouch(int edgeFlag) {
        Activity activity = mActivity.get();
        if (null != activity) {
            Utils.convertActivityToTranslucent(activity);
        }
    }

    @Override
    public void onScrollOverThreshold() {

    }

    @Override
    public void onContentViewSwipedBack() {
        Activity activity = mActivity.get();
        if (null != activity && !activity.isFinishing()) {
            KeyboardUtils.hideSoftInput(activity);
            activity.onBackPressed();
            activity.overridePendingTransition(0, 0);
        }
    }
}
