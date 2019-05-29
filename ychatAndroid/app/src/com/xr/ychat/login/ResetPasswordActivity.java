package com.xr.ychat.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.xr.ychat.R;

public class ResetPasswordActivity extends SwipeBackUI {
    private static final String NEED_LOGIN = "need_login";
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private boolean needOpenLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_reset_password);
        needOpenLogin = getIntent().getBooleanExtra(NEED_LOGIN, false);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("找回密码");
        mToolbar.setNavigationOnClickListener(v -> finish());
        FindPasswordFragment fragment = FindPasswordFragment.newInstance(needOpenLogin);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_layout, fragment)
                .commit();
    }

    public static void start(Context context, boolean needOpenLogin) {
        Intent intent = new Intent(context, ResetPasswordActivity.class);
        intent.putExtra(NEED_LOGIN, needOpenLogin);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

}
