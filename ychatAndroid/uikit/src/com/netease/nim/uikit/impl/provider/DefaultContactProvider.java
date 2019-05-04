package com.netease.nim.uikit.impl.provider;

import android.text.TextUtils;

import com.netease.nim.uikit.api.model.contact.ContactProvider;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.impl.cache.FriendDataCache;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.Friend;

import java.util.List;

/**
 * UIKit默认的通讯录（联系人）数据源提供者，
 * Created by hzchenkang on 2016/12/19.
 */

public class DefaultContactProvider implements ContactProvider {

    @Override
    public List<String> getUserInfoOfMyFriends() {
        List<String> friends = FriendDataCache.getInstance().getMyFriendAccounts();
        List<String> blacks = NIMClient.getService(FriendService.class).getBlackList();
        if (blacks != null && blacks.size() > 0) {
            for (String black : blacks) {
                if (!friends.contains(black)) {
                    boolean isMyFriend = NIMClient.getService(FriendService.class).isMyFriend(black);
                    if (isMyFriend) {
                        friends.add(black);
                    }
                }
            }
        }
        return friends;
    }

    @Override
    public int getMyFriendsCount() {
        List<String> friends = FriendDataCache.getInstance().getMyFriendAccounts();
        List<String> blacks = NIMClient.getService(FriendService.class).getBlackList();
        if (blacks != null && blacks.size() > 0) {
            for (String black : blacks) {
                if (!friends.contains(black)) {
                    boolean isMyFriend = NIMClient.getService(FriendService.class).isMyFriend(black);
                    if (isMyFriend) {
                        friends.add(black);
                    }
                }
            }
        }
        return friends.size();
    }

    @Override
    public String getAlias(String account) {
        Friend friend = FriendDataCache.getInstance().getFriendByAccount(account);
        if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
            return friend.getAlias();
        }
        return null;
    }

    @Override
    public boolean isMyFriend(String account) {
        return FriendDataCache.getInstance().isMyFriend(account);
    }
}
