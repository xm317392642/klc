package com.xr.ychat.main.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.util.LinkProperties;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.team.TeamService;

import java.util.HashMap;

public class MiddleActivity extends UI {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            LinkProperties linkProperties = getIntent().getParcelableExtra(LinkedME.LM_LINKPROPERTIES);
            if (linkProperties != null) {
                HashMap<String, String> hashMap = linkProperties.getControlParams();
                String accid = hashMap.get("accid");
                String account = NimUIKit.getAccount();
                if (!TextUtils.isEmpty(accid)) {
                    if (!TextUtils.isEmpty(account)) {
                        ContactHttpClient.getInstance().querySearching(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), 3, accid, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                            @Override
                            public void onSuccess(RequestInfo aVoid) {
                                if (!TextUtils.isEmpty(aVoid.getAccid()) && aVoid.getAccid().length() > 1) {
                                    if (!NIMClient.getService(FriendService.class).isMyFriend(aVoid.getAccid())) {
                                        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(aVoid.getAccid(), VerifyType.VERIFY_REQUEST, "我是" + UserInfoHelper.getUserName(account)));
                                    }
                                }
                            }

                            @Override
                            public void onFailed(int code, String errorMsg) {

                            }
                        });
                    } else {
                        SPUtils.getInstance().put(CommonUtil.AUTO_ADD_TYPE, 1);
                        SPUtils.getInstance().put(CommonUtil.AUTO_ADD_VALUE, accid);
                    }
                }
                String groupid = hashMap.get("groupid");
                if (!TextUtils.isEmpty(groupid)) {
                    if (!TextUtils.isEmpty(account)) {
                        if (!TeamHelper.isTeamMember(groupid, account)) {
                            NIMClient.getService(TeamService.class).applyJoinTeam(groupid, "我是" + UserInfoHelper.getUserName(account));
                        }
                    } else {
                        SPUtils.getInstance().put(CommonUtil.AUTO_ADD_TYPE, 2);
                        SPUtils.getInstance().put(CommonUtil.AUTO_ADD_VALUE, groupid);
                    }
                }
            }
        }
        finish();
    }

}
