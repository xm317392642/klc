package com.xr.ychat.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.activity.UI;
import com.xr.ychat.R;

public class DownloadTipsActivity extends SwipeBackUI {

    private Button commit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_tips);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        commit = (Button) findViewById(R.id.download_commit);
        commit.setOnClickListener(v -> {
            Uri uri = Uri.parse("http://fx.weirankeji.com");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, DownloadTipsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

}
