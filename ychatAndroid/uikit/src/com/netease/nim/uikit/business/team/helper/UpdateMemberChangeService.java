package com.netease.nim.uikit.business.team.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.TeamMethodInfo;

public class UpdateMemberChangeService extends IntentService {
    public static final String EXTRA_DOACCID = "DoAccID";
    public static final String EXTRA_TEAM = "team";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_NOTE = "note";

    public UpdateMemberChangeService() {
        super("UpdateMemberChatTimeService");
    }

    public static void start(Context context, String DoAccID, String team, int action) {
        Intent service = new Intent(context, UpdateMemberChangeService.class);
        service.putExtra(EXTRA_DOACCID, DoAccID);
        service.putExtra(EXTRA_TEAM, team);
        service.putExtra(EXTRA_ACTION, action);
        context.startService(service);
    }

    public static void start(Context context, String DoAccID, String team, int action, String note) {
        Intent service = new Intent(context, UpdateMemberChangeService.class);
        service.putExtra(EXTRA_DOACCID, DoAccID);
        service.putExtra(EXTRA_TEAM, team);
        service.putExtra(EXTRA_ACTION, action);
        service.putExtra(EXTRA_NOTE, note);
        context.startService(service);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String account = Preferences.getWeiranUid(UpdateMemberChangeService.this);
        String token = Preferences.getWeiranToken(UpdateMemberChangeService.this);
        String team = intent.getStringExtra(EXTRA_TEAM);
        String DoAccID = intent.getStringExtra(EXTRA_DOACCID);
        int action = intent.getIntExtra(EXTRA_ACTION, 0);
        if (action != 0) {
            ContactHttpClient.getInstance().updateTeamMemberChange(account, token, DoAccID, action, team, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo o) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {

                }
            });
        }
        if (action == 1) {
            String note = intent.getStringExtra(EXTRA_NOTE);
            ContactHttpClient.getInstance().addTeamMemberChangeMethod(account, token, team, DoAccID, note, new ContactHttpClient.ContactHttpCallback<TeamMethodInfo>() {
                @Override
                public void onSuccess(TeamMethodInfo o) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {

                }
            });
        } else if (action == 2) {
            ContactHttpClient.getInstance().removeTeamMemberChangeMethod(account, token, team, DoAccID, new ContactHttpClient.ContactHttpCallback<TeamMethodInfo>() {
                @Override
                public void onSuccess(TeamMethodInfo o) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {

                }
            });
        }
    }
}
