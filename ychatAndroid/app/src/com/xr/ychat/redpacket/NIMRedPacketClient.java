package com.xr.ychat.redpacket;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.redpacket.RedPacketService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;

import java.util.List;

public class NIMRedPacketClient {

    private static boolean init;

    private static NimUserInfo selfInfo;

    private static String thirdToken;
    private static Observer<StatusCode> observer = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode == StatusCode.LOGINED) {
                getThirdToken();
            }
        }
    };

    private static Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> nimUserInfo) {
            for (NimUserInfo userInfo : nimUserInfo) {
                if (userInfo.getAccount().equals(DemoCache.getAccount())) {
                    // 更新 jrmf 用户昵称、头像信息
                    selfInfo = userInfo;
                    updateMyInfo();
                    return;
                }
            }
        }
    };

    private static void getRpAuthToken() {
        NIMClient.getService(RedPacketService.class).getRedPacketAuthToken().setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    thirdToken = result;
                } else if (code == ResponseCode.RES_RP_INVALID) {
                    // 红包功能不可用
                } else if (code == ResponseCode.RES_FORBIDDEN) {
                    // 应用没开通红包功能
                }
            }
        });
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        initJrmfSDK(context);
        init = true;

        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(observer, true);
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, true);

        RpOpenedMessageFilter.startFilter();
    }

    /**
     * 初始化金融魔方SDK
     *
     * @param context
     */
    private static void initJrmfSDK(Context context) {
        //初始化红包sdk

        // com.jrmf360.neteaselib.base.utils.LogUtil.init(true);
        // 设置微信appid，如果不使用微信支付可以不调用，此处需要开发者到微信支付申请appid
        // JrmfClient.setWxAppid("xxxxxx");
    }

    public static boolean isEnable() {
        return init;
    }

    private static boolean checkValid() {
        return init;
    }

    /**
     * 获取 thirdToken
     *
     * @return thirdToken
     */
    public static String getThirdToken() {
        if (TextUtils.isEmpty(thirdToken)) {
            getRpAuthToken();
        }
        return thirdToken;
    }

    /**
     * 登出之后，清掉token
     */
    public static void clear() {
        thirdToken = null;
    }

    /**
     * 跳转至我的钱包界面
     *
     * @param activity context
     */
    public static void startWalletActivity(Activity activity) {
        if (checkValid()) {

        }
    }

    /**
     * 打开红包发送界面
     *
     * @param activity        context
     * @param sessionTypeEnum 会话类型，支持单聊和群聊
     * @param targetAccount   会话对象目标 account
     * @param requestCode     startActivityForResult requestCode
     */
    public static void startSendRpActivity(Activity activity, SessionTypeEnum sessionTypeEnum, String targetAccount, int requestCode) {
        if (!checkValid()) {
            return;
        }
        if (sessionTypeEnum == SessionTypeEnum.Team) { // 群聊红包
            // 调用群聊红包接口
            Team team = NimUIKit.getTeamProvider().getTeamById(targetAccount);
            SendFlockRedpactActivity.start(activity, team.getId(), team.getName(), team.getCreator(), getRobotId(team), requestCode);
        } else { // 单聊红包
            SendSingleRedpactActivity.start(activity, targetAccount, requestCode);
        }
    }

    private static String getRobotId(Team team) {
        if (team == null) {
            return null;
        }
        TeamExtension extension;
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                Gson gson = new Gson();
                extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
            } catch (Exception exception) {
                extension = new TeamExtension();
            }
        } else {
            extension = new TeamExtension();
        }
        return extension.getRobotId();
    }

    /**
     * 启动拆红包dialog
     *
     * @param activity        context
     * @param sessionTypeEnum 会话类型
     * @param briberyId       红包id
     */
    public static void startOpenRpDialog(final FragmentActivity activity, final String fromSessionId, final String fromAccount, final String fromContent, final SessionTypeEnum sessionTypeEnum, final String briberyId, final int status, final NIMOpenRpCallback cb, final IMMessage message) {
        if (!checkValid()) {
            return;
        }
        if (selfInfo == null) {
            selfInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount());
        }
        String uid = Preferences.getWeiranUid(activity);
        String mytoken = Preferences.getWeiranToken(activity);
        String nickname = selfInfo.getName();
        String aliuid = SPUtils.getInstance().getString(CommonUtil.ALIPAYUID);
        if (TextUtils.isEmpty(aliuid)) {
            BindAlipayActivity.start(activity);
        } else {
            if (sessionTypeEnum == SessionTypeEnum.Team) {
                OpenRedpacketFragment fragment = new OpenRedpacketFragment();
                Bundle bundle = new Bundle();
                bundle.putString("SessionId", fromSessionId);
                bundle.putString("FromAccount", fromAccount);
                bundle.putString("FromContent", fromContent);
                bundle.putString("BriberyId", briberyId);
                bundle.putInt("Type", 2);
                bundle.putInt("Status", status);
                fragment.setArguments(bundle);
                fragment.show(activity.getSupportFragmentManager(), cb, message);
            } else if (sessionTypeEnum == SessionTypeEnum.P2P) {
                if (TextUtils.equals(fromAccount, DemoCache.getAccount())) {
                    RedpactDetailActivity.start(activity, fromAccount, fromContent, briberyId, uid, mytoken, nickname, message);
                } else {
                    OpenRedpacketFragment fragment = new OpenRedpacketFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("SessionId", fromSessionId);
                    bundle.putString("FromAccount", fromAccount);
                    bundle.putString("FromContent", fromContent);
                    bundle.putString("BriberyId", briberyId);
                    bundle.putInt("Type", 1);
                    bundle.putInt("Status", status);
                    fragment.setArguments(bundle);
                    fragment.show(activity.getSupportFragmentManager(), cb, message);
                }
            }
        }
    }

    /**
     * 打开红包详情界面
     *
     * @param activity context
     * @param packetId 红包id
     */
    public static void startRpDetailActivity(Activity activity, String packetId) {
        if (checkValid()) {

        }
    }


    /**
     * 更新个人信息到jrmf
     */
    public static void updateMyInfo() {
        if (init && selfInfo != null) {

        }
    }
}
