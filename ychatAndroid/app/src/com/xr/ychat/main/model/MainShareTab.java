package com.xr.ychat.main.model;

import com.xr.ychat.R;
import com.xr.ychat.main.fragment.MainShareTabFragment;
import com.xr.ychat.main.fragment.ShareFriendFragment;
import com.xr.ychat.main.fragment.ShareSessionListFragment;
import com.xr.ychat.main.fragment.ShareTeamFragment;
import com.xr.ychat.main.reminder.ReminderId;

public enum MainShareTab {
    //分享：最近聊天，群聊，好友
    RECENT_CONTACTS(0, ReminderId.SESSION, ShareSessionListFragment.class, R.string.share_tab_recently_chat, R.layout.session_list),
    TEAM_CHAT(1, ReminderId.INVALID, ShareTeamFragment.class, R.string.share_tab_team, R.layout.team_chat_tab),
    FRIEND(2, ReminderId.CONTACT, ShareFriendFragment.class, R.string.share_tab_friend, R.layout.contacts_list);
    public final int tabIndex;

    public final int reminderId;

    public final Class<? extends MainShareTabFragment> clazz;

    public final int resId;

    public final int fragmentId;

    public final int layoutId;

    MainShareTab(int index, int reminderId, Class<? extends MainShareTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.reminderId = reminderId;
        this.clazz = clazz;
        this.resId = resId;
        this.fragmentId = index;
        this.layoutId = layoutId;
    }

    public static final MainShareTab fromReminderId(int reminderId) {
        for (MainShareTab value : MainShareTab.values()) {
            if (value.reminderId == reminderId) {
                return value;
            }
        }

        return null;
    }

    public static final MainShareTab fromTabIndex(int tabIndex) {
        for (MainShareTab value : MainShareTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }

        return null;
    }
}
