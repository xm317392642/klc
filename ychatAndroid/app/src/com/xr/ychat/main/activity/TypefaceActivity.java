package com.xr.ychat.main.activity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.xr.ychat.R;
import com.xr.ychat.common.ui.FontSizeView;

/**
 * 字体大小
 */
public class TypefaceActivity extends SwipeBackUI {
    private TextView txLeft1, txLeft2, txRight, rightTextview;
    private FontSizeView fsvFontSize;
    private float fontSizeScale, defaultScale;
    private int defaultPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_typeface);
        initView();
        initData();
    }

    private void initData() {

        defaultScale = SPUtils.getInstance().getFloat(Extras.EXTRA_TYPEFACE);
        if (defaultScale > 0.5) {
            defaultPos = (int) ((defaultScale - 0.875) / 0.125);
        } else {
            defaultPos = 1;
        }
        //注意： 写在改变监听下面 —— 否则初始字体不会改变
        fsvFontSize.setDefaultPosition(defaultPos);
    }

    /**
     * 改变textsize 大小
     */
    private void changeTextSize(float dimension) {
        txLeft1.setTextSize(dimension);
        txLeft2.setTextSize(dimension);
        txRight.setTextSize(dimension);
        rightTextview.setTextSize(dimension - 1f);
        toolbarTitle.setTextSize(dimension + 1.5f);
    }

    TextView toolbarTitle;

    public void initView() {
        fsvFontSize = findView(R.id.fsv_font_size);
        //滑动返回监听
        fsvFontSize.setChangeCallbackListener(new FontSizeView.OnChangeCallbackListener() {
            @Override
            public void onChangeListener(int position) {
                int dimension = getResources().getDimensionPixelSize(R.dimen.sp_stander);
                //根据position 获取字体倍数
                fontSizeScale =(0.875f + 0.125f * position);
                //放大后的sp单位
                float v = fontSizeScale *  ScreenUtil.px2sp(getApplicationContext(), dimension);
                //改变当前页面大小
                changeTextSize(v);

            }
        });

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        rightTextview = (TextView) findViewById(R.id.action_bar_right_clickable_textview);
        rightTextview.setOnClickListener(v -> {
            SPUtils.getInstance().put(Extras.EXTRA_TYPEFACE, fontSizeScale);
            SPUtils.getInstance().put(Extras.EXTRA_TYPEFACE, fontSizeScale);
            ActivityUtils.finishAllActivities();
            AppUtils.launchApp(AppUtils.getAppPackageName());
        });
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("设置字体大小");
        rightTextview.setText("完成");
        mToolbar.setNavigationOnClickListener(v -> finish());
        txLeft1 = findView(R.id.tx_left1);
        txLeft2 = findView(R.id.tx_left2);
        txRight = findView(R.id.tx_right);
    }

    /**
     * 重新配置缩放系数
     *
     * @return
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        config.fontScale = 1;//1 设置正常字体大小的倍数
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

}
