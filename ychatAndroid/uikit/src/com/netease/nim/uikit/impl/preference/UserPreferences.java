package com.netease.nim.uikit.impl.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.nim.uikit.api.NimUIKit;

/**
 * Created by hzxuwen on 2015/10/21.
 */
public class UserPreferences {

    private final static String KEY_EARPHONE_MODE = "KEY_EARPHONE_MODE";
    private final static String KEY_SHARE = "is_share";//是否需要分享
    public final static String KEY_SCHEMA = "share_schema";//schema
    private final static String KEY_SHARE_V = "share_img_url";//分享图片还是游戏链接url
    public final static String KEY_SHARE_URI = "share_uri";//分享的uri数据

    public static final String SHARE_IMG="image_share";
    public static final String SHARE_URL="url_share";

    public static String getShareValue() {
        return getSharedPreferences().getString(KEY_SHARE_V, null);
    }

    public static void setShareValue(String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(KEY_SHARE_V, value);
        editor.apply();
    }

    public static String getString(String key,String value) {
        return getSharedPreferences().getString(key, "");
    }

    public static void setString(String key,String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static boolean getShare() {
        return getBoolean(KEY_SHARE, false);
    }

    public static void setShare(boolean on) {
        saveBoolean(KEY_SHARE, on);
    }
    public static void setEarPhoneModeEnable(boolean on) {
        saveBoolean(KEY_EARPHONE_MODE, on);
    }

    public static boolean isEarPhoneModeEnable() {
        return getBoolean(KEY_EARPHONE_MODE, true);
    }

    private static boolean getBoolean(String key, boolean value) {
        return getSharedPreferences().getBoolean(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences() {
        return NimUIKit.getContext().getSharedPreferences("UIKit." + NimUIKit.getAccount(), Context.MODE_PRIVATE);
    }
}
