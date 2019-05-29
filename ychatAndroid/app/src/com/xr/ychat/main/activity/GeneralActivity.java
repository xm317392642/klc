package com.xr.ychat.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;

import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.lucene.LuceneService;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.misc.MiscService;
import com.netease.nimlib.sdk.msg.MsgService;
import com.xr.ychat.R;
import com.xr.ychat.session.activity.ChatBackgroundActivity;

import java.util.ArrayList;
import java.util.List;

public class GeneralActivity extends SwipeBackUI {
    private ConstraintLayout cacheLayout;
    private ConstraintLayout typefaceLayout;
    private ConstraintLayout backgroudLayout;
    private ConstraintLayout clearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_general);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        cacheLayout = (ConstraintLayout) findViewById(R.id.cache_layout);
//        cacheLayout.setOnClickListener(view -> {
//            final EasyAlertDialog alertDialog = new EasyAlertDialog(GeneralActivity.this);
//            alertDialog.setMessage("确定清空所有缓存?");
//            alertDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
//                    v -> {
//                        alertDialog.dismiss();
//                    }
//            );
//            alertDialog.addPositiveButton("清空", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
//                    v -> {
//                        alertDialog.dismiss();
//                        clearCache();
//                    });
//            alertDialog.show();
//        });
        clearLayout = (ConstraintLayout) findViewById(R.id.clear_layout);
        clearLayout.setOnClickListener(view -> {
            final EasyAlertDialog alertDialog = new EasyAlertDialog(GeneralActivity.this);
            alertDialog.setMessage("确定清空所有聊天记录?");
            alertDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        alertDialog.dismiss();
                    }
            );
            alertDialog.addPositiveButton("清空", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        alertDialog.dismiss();
                        NIMClient.getService(MsgService.class).clearMsgDatabase(true);
                        YchatToastUtils.showShort( R.string.clear_msg_history_success);
                    });
            alertDialog.show();
        });
        typefaceLayout = (ConstraintLayout) findViewById(R.id.typeface_layout);
        typefaceLayout.setOnClickListener(v -> {
            startActivity(new Intent(this, TypefaceActivity.class));
        });
        backgroudLayout = (ConstraintLayout) findViewById(R.id.backgroud_layout);
        backgroudLayout.setOnClickListener(v -> {
            ChatBackgroundActivity.start(this);
        });
    }

    private void clearCache() {
        NIMClient.getService(LuceneService.class).clearCache();
        List<DirCacheFileType> types = new ArrayList<>();
        types.add(DirCacheFileType.AUDIO);
        types.add(DirCacheFileType.THUMB);
        types.add(DirCacheFileType.IMAGE);
        types.add(DirCacheFileType.VIDEO);
        types.add(DirCacheFileType.OTHER);
        NIMClient.getService(MiscService.class).clearDirCache(types, 0, 0);
        YchatToastUtils.showShort( R.string.clear_msg_cache_success);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GeneralActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
