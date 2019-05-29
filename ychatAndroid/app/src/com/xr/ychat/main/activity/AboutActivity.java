package com.xr.ychat.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CacheMemoryUtils;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.UpdateInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.CustomClickListener;
import com.netease.nim.uikit.common.util.DownloadUtils;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.xr.ychat.R;

public class AboutActivity extends SwipeBackUI {

    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private TextView versionDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.about_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("关于");
        mToolbar.setNavigationOnClickListener(v -> finish());
        findViews();
        initViewData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void findViews() {
        versionDate = (TextView) findViewById(R.id.version_detail_date);
        findView(R.id.check_new_version).setOnClickListener(new CustomClickListener() {
            @Override
            protected void onSingleClick(View v) {
                DialogMaker.showProgressDialog(AboutActivity.this,"");
                CommonUtil.setCancelValue(false);
                DownloadUtils.queryAppVersion((boolean success, Object result, int code)-> {
                    DialogMaker.dismissProgressDialog();
                    if (success){
                        CommonUtil.updateDialogIsShow=false;
                        update((UpdateInfo)result);
                        CommonUtil.setCancelValue(true);
                    }else{
                        if(code==-1){
                            YchatToastUtils.showShort("服务器繁忙，请稍后再试");
                        }else{
                            YchatToastUtils.showShort("当前是最新版本");
                        }
                        CommonUtil.setCancelValue(true);
                    }
                });
            }
        });
    }

    private void initViewData() {
        versionDate.setText(getString(R.string.app_name) + AppUtils.getAppVersionName());
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
