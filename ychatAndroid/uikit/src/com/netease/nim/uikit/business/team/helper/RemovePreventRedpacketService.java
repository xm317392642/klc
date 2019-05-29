package com.netease.nim.uikit.business.team.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;

public class RemovePreventRedpacketService extends IntentService {
    public static final String EXTRA_TEAM = "team";
    public static final String EXTRA_ACCOUNT = "account";

    public RemovePreventRedpacketService() {
        super("RemovePreventRedpacketService");
    }

    public static void start(Context context, String team, String account) {
        Intent service = new Intent(context, RemovePreventRedpacketService.class);
        service.putExtra(EXTRA_TEAM, team);
        service.putExtra(EXTRA_ACCOUNT, account);
        context.startService(service);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String account = Preferences.getWeiranUid(RemovePreventRedpacketService.this);
        String token = Preferences.getWeiranToken(RemovePreventRedpacketService.this);
        String team = intent.getStringExtra(EXTRA_TEAM);
        String name = intent.getStringExtra(EXTRA_ACCOUNT);
        updatePreventRedpacketMemberList(account, token, team, 2, name);
    }

    /**
     * 设置能否发送或者能否接收红包
     */
    private void updatePreventRedpacketMemberList(String uid, String mytoken, String qunID, int IsAdd, String DoAccID) {
        ContactHttpClient.getInstance().updatePreventRedpacketMemberList(uid, mytoken, qunID, IsAdd, DoAccID, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
            }

            @Override
            public void onFailed(int code, String errorMsg) {
            }
        });
    }
}
