package com.netease.nim.uikit.business.contact.core.item;

public class SearchItem extends AbsContactItem {
    private final String text;

    public SearchItem(String text) {
        this.text = text;
    }

    @Override
    public int getItemType() {
        return ItemTypes.SEARCH;
    }

    @Override
    public String belongsGroup() {
        return null;
    }

    public final String getText() {
        return text;
    }
}
