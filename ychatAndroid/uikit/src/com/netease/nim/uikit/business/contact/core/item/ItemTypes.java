package com.netease.nim.uikit.business.contact.core.item;

/**
 * 通讯录列表项类型
 * Created by huangjun on 2015/2/10.
 */
public interface ItemTypes {

    /**
     * 基础类型
     */
    int TEXT = -2;

    int LABEL = -1;

    /**
     * 扩展类型
     */
    int FUNC = LABEL + 1; // 功能项

    int SEARCH = FUNC + 1; // 搜索

    int FRIEND = SEARCH + 1; // 好友项

    int TEAM = FRIEND + 1; // 群组项

    int TEAM_MEMBER = TEAM + 1; // 群成员

    int MSG = TEAM_MEMBER + 1; // 消息

    /**
     * 子类型
     */
    interface TEAMS {
        int BASE = ItemTypes.TEAM << 16;

        int NORMAL_TEAM = BASE + 1; // 普通群

        int ADVANCED_TEAM = BASE + 2; // 高级群
    }
}
