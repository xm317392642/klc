package com.netease.nim.uikit.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class Preferences {
    private static final String KEY_USER_ACCOUNT = "account";
    private static final String KEY_USER_TOKEN = "token";
    private static final String KEY_USER_PHONE = "phone";
    private static final String KEY_WEIRAN_TOKEN = "weiran_token";
    private static final String KEY_WEIRAN_UID = "weiran_uid";

    public static void saveUserPhone(Context context, String phone) {
        saveString(context, KEY_USER_PHONE, phone);
    }

    public static String getUserPhone(Context context) {
        return getString(context, KEY_USER_PHONE);
    }

    public static String getWeiranUid(Context context) {
        return getString(context, KEY_WEIRAN_UID);
    }

    public static void saveWeiranUid(Context context, String weiran_uid) {
        saveString(context, KEY_WEIRAN_UID, weiran_uid);
    }

    public static String getWeiranToken(Context context) {
        return getString(context, KEY_WEIRAN_TOKEN);
    }

    public static void saveWeiranToken(Context context, String weiran_token) {
        saveString(context, KEY_WEIRAN_TOKEN, weiran_token);
    }

    public static void saveUserAccount(Context context, String account) {
        saveString(context, KEY_USER_ACCOUNT, account);
    }

    public static String getUserAccount(Context context) {
        return getString(context, KEY_USER_ACCOUNT);
    }

    public static void saveUserToken(Context context, String token) {
        saveString(context, KEY_USER_TOKEN, token);
    }

    public static String getUserToken(Context context) {
        return getString(context, KEY_USER_TOKEN);
    }

    private static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(Context context, String key) {
        return getSharedPreferences(context).getString(key, null);
    }

    static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Demo", Context.MODE_PRIVATE);
    }
}
