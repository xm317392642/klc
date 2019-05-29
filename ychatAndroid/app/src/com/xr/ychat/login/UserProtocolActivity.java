package com.xr.ychat.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.xr.ychat.R;

public class UserProtocolActivity extends SwipeBackUI {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.user_protocol_layout);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UserProtocolActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
