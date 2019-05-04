package com.netease.nim.uikit.business.contact.core.provider;

import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nim.uikit.impl.preference.UserPreferences;

import java.util.ArrayList;
import java.util.List;

public class ContactDataProvider implements IContactDataProvider {

    private int[] itemTypes;

    public ContactDataProvider(int... itemTypes) {
        this.itemTypes = itemTypes;
    }

    @Override
    public List<AbsContactItem> provide(TextQuery query) {
        List<AbsContactItem> data = new ArrayList<>();

        for (int itemType : itemTypes) {
            data.addAll(provide(itemType, query));
        }

        return data;
    }

    private final List<AbsContactItem> provide(int itemType, TextQuery query) {
        switch (itemType) {
            case ItemTypes.FRIEND:
                return UserDataProvider.provide(query);
            case ItemTypes.TEAM:
            case ItemTypes.TEAMS.ADVANCED_TEAM:
            case ItemTypes.TEAMS.NORMAL_TEAM:
                return TeamDataProvider.provide(query, itemType);
            case ItemTypes.TEAM_MEMBER:
                return TeamDataProvider.provide(query);
            case ItemTypes.MSG:
                if (!UserPreferences.getShare()) {
                    return MsgDataProvider.provide(query);//不是分享的情况下，才会搜索出聊天记录
                }
            default:
                return new ArrayList<>();
        }
    }
}
