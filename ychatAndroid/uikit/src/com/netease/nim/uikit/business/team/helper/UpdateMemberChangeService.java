package com.netease.nim.uikit.business.team.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.TeamMethodInfo;
import com.netease.nim.uikit.common.ui.combinebitmap.CombineBitmap;
import com.netease.nim.uikit.common.ui.combinebitmap.layout.WechatLayoutManager;
import com.netease.nim.uikit.common.ui.combinebitmap.listener.OnProgressListener;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class UpdateMemberChangeService extends IntentService {
    public static final int DEFAULT_AVATAR_THUMB_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_max_size);
    public static final String EXTRA_DOACCID = "DoAccID";
    public static final String EXTRA_TEAM = "team";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_NOTE = "note";
    private String account;
    private String token;
    private String teamId;

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
        account = Preferences.getWeiranUid(UpdateMemberChangeService.this);
        token = Preferences.getWeiranToken(UpdateMemberChangeService.this);
        teamId = intent.getStringExtra(EXTRA_TEAM);
        String DoAccID = intent.getStringExtra(EXTRA_DOACCID);
        int action = intent.getIntExtra(EXTRA_ACTION, 0);
        if (action != 0) {
            ContactHttpClient.getInstance().updateTeamMemberChange(account, token, DoAccID, action, teamId, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo o) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {

                }
            });
            Team team = NIMClient.getService(TeamService.class).queryTeamBlock(teamId);
            NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallback<List<TeamMember>>() {
                @Override
                public void onSuccess(List<TeamMember> members) {
                    if (members != null && !members.isEmpty()) {
                        if (members.size() < 11) {
                            loadTeamIconByTeam(team, members);
                        }
                    } else {
                        members = NimUIKit.getTeamProvider().getTeamMemberList(teamId);
                        if (members != null && !members.isEmpty()) {
                            Iterator<TeamMember> iterator = members.iterator();
                            while (iterator.hasNext()) {
                                TeamMember teamMember = iterator.next();
                                if (TextUtils.equals(NimUIKit.getAccount(), teamMember.getAccount())) {
                                    iterator.remove();
                                    break;
                                }
                            }
                            if (members.size() < 11) {
                                loadTeamIconByTeam(team, members);
                            }
                        }
                    }
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }
        if (action == 1) {
            String note = intent.getStringExtra(EXTRA_NOTE);
            ContactHttpClient.getInstance().addTeamMemberChangeMethod(account, token, teamId, DoAccID, note, new ContactHttpClient.ContactHttpCallback<TeamMethodInfo>() {
                @Override
                public void onSuccess(TeamMethodInfo o) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {

                }
            });
        } else if (action == 2) {
            ContactHttpClient.getInstance().removeTeamMemberChangeMethod(account, token, teamId, DoAccID, new ContactHttpClient.ContactHttpCallback<TeamMethodInfo>() {
                @Override
                public void onSuccess(TeamMethodInfo o) {

                }

                @Override
                public void onFailed(int code, String errorMsg) {

                }
            });
        }
    }

    private void loadTeamIconByTeam(Team team, List<TeamMember> members) {
        String robotId = getRobotId(team);
        if (!TextUtils.isEmpty(robotId)) {
            Iterator<TeamMember> iterator = members.iterator();
            while (iterator.hasNext()) {
                TeamMember teamMember = iterator.next();
                if (TextUtils.equals(teamMember.getAccount(), robotId)) {
                    iterator.remove();
                }
            }
        }
        if (members.size() > 9) {
            members = members.subList(0, 9);
        }
        CombineBitmap.init(UpdateMemberChangeService.this)
                .setLayoutManager(new WechatLayoutManager())
                .setSize(DEFAULT_AVATAR_THUMB_SIZE)
                .setGap(2)
                .setGapColor(Color.parseColor("#E8E8E8"))
                .setMembers(members)
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(Bitmap bitmap) {
                        String dstPath = StorageUtil.getSystemImagePath() + "TeamIcon" + team.getId() + ".jpg";
                        File file = new File(dstPath);
                        ImageUtils.save(bitmap, file, Bitmap.CompressFormat.PNG);
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(file);
                        mediaScanIntent.setData(contentUri);
                        sendBroadcast(mediaScanIntent);
                        NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG).setCallback(new RequestCallbackWrapper<String>() {
                            @Override
                            public void onResult(int code, String url, Throwable exception) {
                                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                                    ContactHttpClient.getInstance().updateGroupHead(account, token, team.getCreator(), teamId, url, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                                        @Override
                                        public void onSuccess(RequestInfo aVoid) {
                                            FileUtils.delete(file);
                                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                            Uri contentUri = Uri.fromFile(file);
                                            mediaScanIntent.setData(contentUri);
                                            sendBroadcast(mediaScanIntent);
                                        }

                                        @Override
                                        public void onFailed(int code, String errorMsg) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                }).build();
    }

    private String getRobotId(Team team) {
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

}
