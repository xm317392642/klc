package com.xr.ychat.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.microquation.linkedme.android.LinkedME;
import com.netease.nim.uikit.common.activity.UI;

/**
 * UriSchemeProcessActivity不继承基类，
 * 继承AppCompatActivity或者Activity（你的基类继承哪个Activity，此处也同基类一样继承相同的Activity）
 */
public class UriSchemeProcessActivity extends UI {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 唤起自身
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        assert intent != null;
        intent.setFlags(getIntent().getFlags());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // App打开后无广告展示及登录等条件限制，直接在此处调用以下方法跳转到具体页面，若有条件限制请参考Demo
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            LinkedME.getInstance().setImmediate(true);
        }
        // 防止跳转后一直停留在该页面
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // 请重写改方法并且设置该Activity的launchmode为singleTask
        setIntent(intent);
    }

}
