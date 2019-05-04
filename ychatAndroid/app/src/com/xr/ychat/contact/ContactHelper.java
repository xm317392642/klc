package com.xr.ychat.contact;

import android.content.Context;

import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nim.uikit.impl.preference.UserPreferences;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.xr.ychat.contact.activity.UserProfileActivity;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.contact.ContactEventListener;

/**
 * UIKit联系人列表定制展示类
 * <p/>
 * Created by huangjun on 2015/9/11.
 */
public class ContactHelper {

    public static void init() {
        setContactEventListener();
    }

    private static void setContactEventListener() {
        NimUIKit.setContactEventListener(new ContactEventListener() {
            @Override
            public void onItemClick(Context context, String account) {
                jump(context,account);
            }

            @Override
            public void onItemLongClick(Context context, String account) {

            }

            @Override
            public void onAvatarClick(Context context, String account) {
                jump(context,account);
            }
        });
    }
private static void jump(Context context,String accountId){
        if(UserPreferences.getShare()){//分享的话，就弹出对话框
            String value=UserPreferences.getShareValue();
            if(UserPreferences.SHARE_IMG.equals(value)){
                CommonUtil.sharePicDialog(context,accountId, SessionTypeEnum.P2P,null,null);
            }else if(UserPreferences.SHARE_URL.equals(value)){
                CommonUtil.shareGameDialog(context,accountId, SessionTypeEnum.P2P,null,null);
            }
        }else{
            UserProfileActivity.start(context, accountId);
        }
}
}
