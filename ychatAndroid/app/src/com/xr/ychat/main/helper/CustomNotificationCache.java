package com.xr.ychat.main.helper;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.common.ApplyLeaveTeam;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义通知缓存
 * <p/>
 * Created by huangjun on 2015/5/29.
 */
public class CustomNotificationCache {
    private static final String TAG = "CustomNotification";

    public static void addCustomNotification(CustomNotification notification) {
        if (notification == null) {
            return;
        }
        List<CustomNotification> datas = getDataList();
        if (!datas.contains(notification)) {
            datas.add(0, notification);
            setDataList(datas);
        }
    }

    public static void clearCustomNotification() {
        SPUtils.getInstance().remove(TAG);
    }

    public static int getUnreadCount() {
        int unreadCount = 0;
        Gson gson = new Gson();
        List<CustomNotification> datas = getDataList();
        for (CustomNotification customNotification : datas) {
            ApplyLeaveTeam applyLeaveTeam = gson.fromJson(customNotification.getContent(), new TypeToken<ApplyLeaveTeam>() {
            }.getType());
            if (!applyLeaveTeam.isHasRead()) {
                unreadCount += 1;
            }
        }
        return unreadCount;
    }

    public static void setDataList(List<CustomNotification> datalist) {
        if (null == datalist) {
            return;
        }
        Gson gson = new Gson();
        String strJson = gson.toJson(datalist);
        SPUtils.getInstance().put(TAG, strJson);
    }

    public static List<CustomNotification> getDataList() {
        List<CustomNotification> datalist = new ArrayList<CustomNotification>();
        String strJson = SPUtils.getInstance().getString(TAG, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<ArrayList<CustomNotification>>() {
        }.getType());
        return datalist;
    }
}
