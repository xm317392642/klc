package com.netease.nim.uikit.common;

import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnsentRedPacketCache {
    public static final String TAG = "UnReceiveRedPacket";

    public static void addUnsentRedPacket(UnsentRedPacket unsentRedPacket) {
        if (unsentRedPacket == null) {
            return;
        }
        List<UnsentRedPacket> datas = getDataList();
        if (!datas.contains(unsentRedPacket)) {
            datas.add(0, unsentRedPacket);
            setDataList(datas);
        }
    }

    public static void updateUnsentRedPacket(UnsentRedPacket unsentRedPacket) {
        boolean update = false;
        List<UnsentRedPacket> datas = getDataList();
        if (datas != null && datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                UnsentRedPacket redPacket = datas.get(i);
                if (TextUtils.equals(unsentRedPacket.getRedPacketID(), redPacket.getRedPacketID())) {
                    datas.set(i, unsentRedPacket);
                    update = true;
                    break;
                }
            }
        }
        if (update) {
            setDataList(datas);
        }
    }

    public static void removeUnsentRedPacket(String redPacketID) {
        boolean hasRemove = false;
        List<UnsentRedPacket> datas = getDataList();
        if (datas != null && datas.size() > 0) {
            Iterator<UnsentRedPacket> iterator = datas.iterator();
            while (iterator.hasNext()) {
                UnsentRedPacket unsentRedPacket = iterator.next();
                if (TextUtils.equals(unsentRedPacket.getRedPacketID(), redPacketID)) {
                    iterator.remove();
                    hasRemove = true;
                    break;
                }
            }
        }
        if (hasRemove) {
            setDataList(datas);
        }
    }

    public static void setDataList(List<UnsentRedPacket> datalist) {
        if (null == datalist) {
            return;
        }
        Gson gson = new Gson();
        String strJson = gson.toJson(datalist);
        SPUtils.getInstance().put(TAG, strJson);
    }

    public static List<UnsentRedPacket> getDataList() {
        List<UnsentRedPacket> datalist = new ArrayList<>();
        String strJson = SPUtils.getInstance().getString(TAG, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<ArrayList<UnsentRedPacket>>() {
        }.getType());
        return datalist;
    }

    public static List<UnsentRedPacket> getDataList(String redPacketAccount) {
        List<UnsentRedPacket> datalist = new ArrayList<>();
        List<UnsentRedPacket> datas = getDataList();
        if (datas != null && datas.size() > 0) {
            Iterator<UnsentRedPacket> iterator = datas.iterator();
            while (iterator.hasNext()) {
                UnsentRedPacket unsentRedPacket = iterator.next();
                if (TextUtils.equals(unsentRedPacket.getRedPacketAccount(), redPacketAccount)) {
                    datalist.add(unsentRedPacket);
                }
            }
        }
        return datalist;
    }

}
