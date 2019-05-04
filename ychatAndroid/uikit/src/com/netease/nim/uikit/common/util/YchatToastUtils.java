package com.netease.nim.uikit.common.util;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.netease.nim.uikit.R;

public class YchatToastUtils {
    public static void showToastLong(String content) {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        View view = ToastUtils.showCustomLong(R.layout.layout_toast);
        TextView textView = view.findViewById(R.id.tv_toast);
        textView.setText(content);
    }

    public static void showShort(String content) {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        View view = ToastUtils.showCustomShort(R.layout.layout_toast);
        TextView textView = view.findViewById(R.id.tv_toast);
        textView.setText(content);
    }

    public static void showShort(int res) {
        showShort(Utils.getApp().getString(res));
    }

    public static void showToastLong(int res) {
        showToastLong(Utils.getApp().getString(res));
    }
}
