package com.netease.nim.uikit.common;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class UpdateMemberChatTimeService extends IntentService {
    public static final String EXTRA_ACCOUNT = "account";
    public static final String EXTRA_TOKEN = "token";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_TEAM = "team";

    public UpdateMemberChatTimeService() {
        super("UpdateMemberChatTimeService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String account = intent.getStringExtra(EXTRA_ACCOUNT);
        String token = intent.getStringExtra(EXTRA_TOKEN);
        String team = intent.getStringExtra(EXTRA_TEAM);
        long time = intent.getLongExtra(EXTRA_TIME, 0L);
        ContactHttpClient.getInstance().updateMemberChatTime(account, token, time, team, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo o) {

            }

            @Override
            public void onFailed(int code, String errorMsg) {
        
            }
        });
    }
}
