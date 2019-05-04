package com.netease.nim.uikit.business.team.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;

public class UpdateTeamStatusService extends IntentService {
    public static final String EXTRA_TEAM = "team";
    public static final String EXTRA_ACTION = "action";

    public UpdateTeamStatusService() {
        super("UpdateTeamStatusService");
    }

    //action=1 代表创建  Action=2 代表解散
    public static void start(Context context, String team, int action) {
        Intent service = new Intent(context, UpdateTeamStatusService.class);
        service.putExtra(EXTRA_TEAM, team);
        service.putExtra(EXTRA_ACTION, action);
        context.startService(service);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String account = Preferences.getWeiranUid(UpdateTeamStatusService.this);
        String token = Preferences.getWeiranToken(UpdateTeamStatusService.this);
        String team = intent.getStringExtra(EXTRA_TEAM);
        int action = intent.getIntExtra(EXTRA_ACTION, 0);
        if (action != 0) {
            ContactHttpClient.getInstance().updateTeamStatusService(account, token, action, team, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo o) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {

                }
            });
        }
    }
}
