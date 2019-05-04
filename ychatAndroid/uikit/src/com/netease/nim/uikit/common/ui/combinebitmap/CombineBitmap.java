package com.netease.nim.uikit.common.ui.combinebitmap;

import android.content.Context;

import com.netease.nim.uikit.common.ui.combinebitmap.helper.Builder;

public class CombineBitmap {
    public static Builder init(Context context) {
        return new Builder(context);
    }
}
