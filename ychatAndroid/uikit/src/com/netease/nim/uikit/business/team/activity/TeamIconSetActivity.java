package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 群头像设置
 */
public class TeamIconSetActivity extends SwipeBackUI {
    private static final int REQUEST_PICK_ICON = 104;
    private static final int ICON_TIME_OUT = 30000;
    private AbortableFuture<String> uploadFuture;
    private HeadImageView teamHeadImage;
    private Team team;
    private String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.team_icon_set);
        team = (Team) getIntent().getSerializableExtra("team");
        teamId = getIntent().getStringExtra("teamId");
        teamHeadImage = (HeadImageView) findViewById(R.id.team_head_image);
        TextView teamNameText = (TextView) findViewById(R.id.team_name);
        TextView teamIdText = (TextView) findViewById(R.id.team_id);
        TextView teamCreateTimeText = (TextView) findViewById(R.id.team_create_time);
        findViewById(R.id.team_info_header).setOnClickListener(v -> showSelector(R.string.set_head_image, REQUEST_PICK_ICON));
        teamHeadImage.loadTeamIconByTeam(team);
        teamNameText.setText(team.getName());
        teamIdText.setText(team.getId());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        teamCreateTimeText.setText(TimeUtils.millis2String(team.getCreateTime(), dateFormat));

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        TextView action_bar_right_clickable_textview = (TextView) findViewById(R.id.action_bar_right_clickable_textview);
        toolbarTitle.setText("群头像");
        //action_bar_right_clickable_textview.setText("上传");
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        registerObservers(true);

    }

    MenuDialog dialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /**
     * 打开图片选择器
     */
    private void showSelector(int titleId, final int requestCode) {
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = titleId;
        option.multiSelect = false;
        option.crop = true;
        option.cropOutputImageWidth = 720;
        option.cropOutputImageHeight = 720;
//
//      PickImageHelper.pickImage(this, requestCode, option);
        List<String> btnNames = new ArrayList<>(2);
        //btnNames.add("设置头像");
        btnNames.add("拍照");
        btnNames.add("从手机相册选择");
        btnNames.add("取消");
        dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
            @Override
            public void onButtonClick(String name) {
                if (name.equals("拍照")) {
                    int from = PickImageActivity.FROM_CAMERA;
                    if (!option.crop) {
                        PickImageActivity.start(TeamIconSetActivity.this, requestCode, from, option.outputPath, option.multiSelect, 1,
                                true, false, 0, 0);
                    } else {
                        PickImageActivity.start(TeamIconSetActivity.this, requestCode, from, option.outputPath, false, 1,
                                false, true, option.cropOutputImageWidth, option.cropOutputImageHeight);
                    }
                } else if (name.equals("从手机相册选择")) {
                    int from = PickImageActivity.FROM_LOCAL;
                    if (!option.crop) {
                        PickImageActivity.start(TeamIconSetActivity.this, requestCode, from, option.outputPath, option.multiSelect,
                                option.multiSelectMaxCount, true, false, 0, 0);
                    } else {
                        PickImageActivity.start(TeamIconSetActivity.this, requestCode, from, option.outputPath, false, 1,
                                false, true, option.cropOutputImageWidth, option.cropOutputImageHeight);
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_PICK_ICON:
                String path = data.getStringExtra(Extras.EXTRA_FILE_PATH);
                updateTeamIcon(path);
                break;
        }
    }

    /**
     * 更新头像
     */
    private void updateTeamIcon(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file == null) {
            return;
        }
        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(R.string.team_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);
        
        new Handler().postDelayed(outimeTask, ICON_TIME_OUT);
        uploadFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {

                    NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.ICON, url).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            DialogMaker.dismissProgressDialog();
                            YchatToastUtils.showShort( R.string.update_success);
                            onUpdateDone();
                        }

                        @Override
                        public void onFailed(int code) {
                            DialogMaker.dismissProgressDialog();
                            YchatToastUtils.showShort( String.format(getString(R.string.update_failed), code));
                        }

                        @Override
                        public void onException(Throwable exception) {
                            DialogMaker.dismissProgressDialog();
                        }
                    }); // 更新资料
                } else {
                    YchatToastUtils.showShort(R.string.team_update_failed);
                    onUpdateDone();
                }
            }
        });
    }

    private void cancelUpload(int resId) {
        if (uploadFuture != null) {
            uploadFuture.abort();
            YchatToastUtils.showShort(resId);
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(R.string.team_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadFuture = null;
        DialogMaker.dismissProgressDialog();
    }

    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
    }

    TeamDataChangedObserver teamDataObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            for (Team team : teams) {
                if (team.getId().equals(teamId)) {
                    teamHeadImage.loadTeamIconByTeam(team);
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
        }
    };
}
