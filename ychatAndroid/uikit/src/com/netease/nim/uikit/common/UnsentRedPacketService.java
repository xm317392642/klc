package com.netease.nim.uikit.common;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.netease.nim.uikit.business.session.extension.RedPacketAttachment;
import com.netease.nim.uikit.business.session.fragment.MessageFragment;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.Iterator;
import java.util.List;

public class UnsentRedPacketService extends IntentService {
    public static final String EXTRA_ACCOUNT = "SessionAccount";
    private String redPacketAccount;
    private LocalBroadcastManager localBroadcastManager;

    public static void start(Context context, String sessionAccount) {
        Intent service = new Intent(context, UnsentRedPacketService.class);
        service.putExtra(EXTRA_ACCOUNT, sessionAccount);
        context.startService(service);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(UnsentRedPacketService.this);
    }

    public UnsentRedPacketService() {
        super("UnsentRedPacketService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        redPacketAccount = intent.getStringExtra(EXTRA_ACCOUNT);
        List<UnsentRedPacket> datalist = UnsentRedPacketCache.getDataList(redPacketAccount);
        if (datalist != null && datalist.size() > 0) {
            String account = Preferences.getWeiranUid(UnsentRedPacketService.this);
            String token = Preferences.getWeiranToken(UnsentRedPacketService.this);
            Iterator<UnsentRedPacket> iterator = datalist.iterator();
            while (iterator.hasNext()) {
                UnsentRedPacket unsentRedPacket = iterator.next();
                if (unsentRedPacket.getTime() < 10) {
                    verifyPaymentResult(account, token, unsentRedPacket);
                } else {
                    UnsentRedPacketCache.removeUnsentRedPacket(unsentRedPacket.getRedPacketID());
                }
            }
        }
    }

    private void verifyPaymentResult(String uid, String mytoken, UnsentRedPacket orderno) {
        ContactHttpClient.getInstance().verifyPaymentResult(uid, mytoken, orderno.getRedPacketID(), new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                orderno.setTime(orderno.getTime() + 1);
                UnsentRedPacketCache.updateUnsentRedPacket(orderno);
                if (aVoid.getPay_status() == 0 && contains(orderno)) {
                    sendRepacketMessage(orderno);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    private boolean contains(UnsentRedPacket orderno) {
        boolean contains = false;
        List<UnsentRedPacket> datalist = UnsentRedPacketCache.getDataList(redPacketAccount);
        if (datalist != null && datalist.size() > 0) {
            Iterator<UnsentRedPacket> iterator = datalist.iterator();
            while (iterator.hasNext()) {
                UnsentRedPacket unsentRedPacket = iterator.next();
                if (TextUtils.equals(orderno.getRedPacketID(), unsentRedPacket.getRedPacketID())) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    private void sendRepacketMessage(UnsentRedPacket orderno) {
        RedPacketAttachment attachment = new RedPacketAttachment();
        attachment.setRpId(orderno.getRedPacketID());
        attachment.setRpContent(orderno.getRedPacketMessage());
        attachment.setRpTitle(orderno.getRedPacketMessage());
        attachment.setRpType(5);
        String content = "发来了一个红包";
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = false;
        config.enableUnreadCount = false;
        IMMessage message = MessageBuilder.createCustomMessage(orderno.getRedPacketAccount(), orderno.getSessionTypeEnum(), content, attachment, config);
        message.setStatus(MsgStatusEnum.success);
        Intent intent = new Intent("com.xr.ychat.NewMessageBroadcastReceiver");
        intent.putExtra(MessageFragment.NEW_MESSAGE, message);
        intent.putExtra(MessageFragment.NEW_MESSAGE_TYPE, 2);
        localBroadcastManager.sendBroadcast(intent);
    }

}
