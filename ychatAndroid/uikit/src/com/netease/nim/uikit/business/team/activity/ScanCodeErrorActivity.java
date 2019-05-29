package com.netease.nim.uikit.business.team.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.SwipeBackUI;

public class ScanCodeErrorActivity extends SwipeBackUI {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.scan_code_error);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("加入群组");
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

}
