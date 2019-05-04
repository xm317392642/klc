package com.netease.nim.uikit.business.session.helper;

import android.util.TypedValue;
import android.widget.TextView;

public class TypefaceHelper {
    public static void setTextSize( int progress,TextView...textViewArray){
        switch (progress) {
            case 0:
                for (TextView tv:textViewArray) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,14f);
                }
                break;
            case 1:
                for (TextView tv:textViewArray) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f);
                }
                break;
            case 2:
                for (TextView tv:textViewArray) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f);
                }
                break;
            case 3:
                for (TextView tv:textViewArray) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,20f);
                }
                break;
            case 4:
                for (TextView tv:textViewArray) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,22f);
                }
                break;
            case 5:
                for (TextView tv:textViewArray) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,24f);
                }
                break;
        }
    }
}
