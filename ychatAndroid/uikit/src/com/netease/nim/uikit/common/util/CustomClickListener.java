package com.netease.nim.uikit.common.util;

import android.view.View;

public abstract class CustomClickListener implements View.OnClickListener {
    private long mLastClickTime;
    private long timeInterval = 500L;

    public CustomClickListener() {

    }

    @Override
    public void onClick(View view) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > timeInterval) {
            // 单次点击事件
            onSingleClick(view);
            mLastClickTime = nowTime;
        }
    }

    protected abstract void onSingleClick(View v);
}