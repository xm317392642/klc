package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 群管理
 * Created by hzxuwen on 2015/3/18.
 */
public class TeamManagmentActivity extends SwipeBackUI {
    private static final int REQUEST_CODE_REMOVE = 103;//转让群
    private static final int REQUEST_CODE_TRANSFER = 101;//转让群
    private static final int REQUEST_CODE_TRANSFER_AND_EXIT = 104;//群主转让群并且退出，先调用转让群transferTeam(final String account)  接口成功后，成为普通成员后在调用 退出接口 quitTeam
    private View layoutTeamMute, layoutControlExit, layoutAuthentication, layoutInvite, layoutInfoUpdate, layoutInviteeAuthen, layoutTransferTeam, layoutCopyTeam, teamMemberActivity, teamMemberChangeRecord, teamMemberForbidRedPacket, teamRobot;
    private TextView controlExitText, authenticationText, inviteText, infoUpdateText, inviteeAutenText, tx_member_protect;
    private MenuDialog authenDialog, inviteDialog, teamInfoUpdateDialog, teamInviteeDialog, dialog;
    private String teamId;
    private Team team;
    private String robotId;
    private boolean isSelfAdmin;//是否是群主
    private View teamProtectView;
    private SwitchButton switchButton, switchButtonControlExit;
    private Gson gson;
    // state
    private boolean isSelfManager = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_managment_activity);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setNavigationOnClickListener(v -> finish());
        teamId = getIntent().getStringExtra("teamId");
        team = NimUIKit.getTeamProvider().getTeamById(teamId);
        robotId = getRobotId(team);
        gson = new Gson();
        String creator = team.getCreator();
        if (creator.equals(NimUIKit.getAccount())) {
            isSelfAdmin = true;
        }
        TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
        if (teamMember.getType() == TeamMemberType.Manager) {
            isSelfManager = true;
        }

        //群成员保护模式
        initTeamMemberProtectMode();
        //群内禁言-管理员
        initTeamMemberMute();
        //群成员活跃度-管理员
        initTeamMemberActivity();
        //群成员变动记录-管理员
        initTeamMemberChangeRecord();
        //禁止群成员收发红包-管理员
        initTeamMemberForbidRedPacket();
        //机器人
        initTeamMemberRobot();
        // 二维码进群验证
        findLayoutAuthentication();
        // 邀请他人权限
        findLayoutInvite();
        // 群资料修改权限
        findLayoutInfoUpdate();
        // 被邀请人身份验证
        findLayoutInviteeAuthen();
        // 退群验证-管理员
        findLayoutControlExit();
        //一键复制新群
        initCopyTeam();
        //转让群
        initTransferTeam();


        updateAuthenView();
        updateBeInvitedText(team.getTeamBeInviteMode());
        updateInfoUpateText(team.getTeamUpdateMode());
        updateInviteText(team.getTeamInviteMode());
        setAuthenticationText(team.getVerifyType());
    }


    /**
     * 群成员保护模式
     */
    private void initTeamMemberProtectMode() {
        teamProtectView = findView(R.id.team_member_protect_layout);
        tx_member_protect = findView(R.id.tx_member_protect);
        if (isSelfAdmin) {
            teamProtectView.setVisibility(View.VISIBLE);
            tx_member_protect.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            teamProtectView.setVisibility(View.GONE);
            tx_member_protect.setVisibility(View.GONE);
        }
        setMarginTop(teamProtectView);
        teamProtectView.findViewById(R.id.line).setVisibility(View.GONE);
        TextView tpotectTx = teamProtectView.findViewById(R.id.item_title);
        switchButton = teamProtectView.findViewById(R.id.setting_item_toggle);
        tpotectTx.setText("群成员保护模式");
        Log.e("xx", "team.getExtension()==" + team.getExtension());
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                TeamExtension extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
                if (TeamExtras.OPEN.equals(extension.getMemberProtect())) {
                    switchButton.setCheck(true);
                } else {
                    switchButton.setCheck(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            switchButton.setCheck(false);
        }
        switchButton.setOnChangedListener((v, checkState) -> {
            if (checkState) {
                synExtensionToServer(TeamExtras.OPEN, true);
            } else {
                synExtensionToServer(TeamExtras.CLOSE, false);
            }
        });
    }

    /**
     * 把新增的字段同步存储到服务器
     *
     * @param protectValue
     */
    private void synExtensionToServer(String protectValue, boolean checkState) {
        TeamExtension extension;
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
            } catch (Exception exception) {
                extension = new TeamExtension();
            }
        } else {
            extension = new TeamExtension();
        }
        extension.setMemberProtect(protectValue);
        extension.setExtensionType(1);
        String extensionString = gson.toJson(extension);
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.Extension, extensionString).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                team.setExtension(extensionString);
                switchButton.setCheck(checkState);
                if (checkState) {
                    YchatToastUtils.showShort("群成员保护模式开启");
                } else {
                    YchatToastUtils.showShort("群成员保护模式关闭");
                }
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                switchButton.setCheck(!checkState);
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
                switchButton.setCheck(!checkState);
            }
        });
    }

    /**
     * 更新身份验证是否显示
     */
    private void updateAuthenView() {
        //只有群主才有转让群的权限
        if (isSelfAdmin) {
            layoutTransferTeam.setVisibility(View.VISIBLE);
        } else {
            layoutTransferTeam.setVisibility(View.GONE);
        }
    }

    /**
     * 从联系人选择器选择群转移对象
     * requestCode:是转让群 还是  转让并退出
     */
    private void onTransferTeam(int requestCode) {
//        if (memberAccounts.size() <= 1) { 暂时注释掉
//            ToastHelper.showToast(this, R.string.team_transfer_without_member);
//            return;
//        }
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
        option.title = "选择群转移的对象";
        option.teamId = teamId;
        option.multi = true;
        option.maxSelectNum = 1;
        option.maxSelectedTip = "最多选择一位群成员转让";
        //需要过滤（不显示）的联系人项
        ArrayList<String> includeAccounts = new ArrayList<>();
        if (!TextUtils.isEmpty(robotId)) {
            includeAccounts.add(robotId);
        }
        includeAccounts.add(NimUIKit.getAccount());
        option.itemFilter = new ContactIdFilter(includeAccounts, true);
        NimUIKit.startContactSelector(this, option, requestCode);
    }

    /**
     * 转让群并退出
     */
    public void transferTeamAndEixtDialog() {
        List<String> btnNames = new ArrayList<>(1);
        btnNames.add("请操作");
        btnNames.add("转让群");
        btnNames.add("转让群并退出");
        btnNames.add(getString(R.string.cancel));
        dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
            @Override
            public void onButtonClick(String name) {
                if (name.equals("转让群")) {
                    onTransferTeam(REQUEST_CODE_TRANSFER);
                } else if (name.equals("转让群并退出")) {
                    onTransferTeam(REQUEST_CODE_TRANSFER_AND_EXIT);
                }
                if (!name.equals("请操作")) {
                    dialog.dismiss();
                }

            }
        });
        dialog.show();
    }


    /**
     * 禁止群成员收发红包
     */
    private void initTeamMemberForbidRedPacket() {
        teamMemberForbidRedPacket = findViewById(R.id.team_member_forbid_redpacket_layout);
        if (isSelfAdmin) {
            teamMemberForbidRedPacket.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            teamMemberForbidRedPacket.setVisibility(View.VISIBLE);
        }

        setMarginTop(teamMemberForbidRedPacket);
        ((TextView) teamMemberForbidRedPacket.findViewById(R.id.item_title)).setText("禁止群成员收发红包");
        ((TextView) teamMemberForbidRedPacket.findViewById(R.id.item_detail)).setHint("");
        teamMemberForbidRedPacket.findViewById(R.id.line).setVisibility(View.GONE);
        //禁止群成员收发红包layout点击监听
        teamMemberForbidRedPacket.setOnClickListener(v -> {
            String url = "scheme://ychat/preventredpacket?team_id=" + teamId;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    /**
     * 群内禁言
     */
    private void initTeamMemberMute() {
        layoutTeamMute = findViewById(R.id.team_member_mute_layout);
        if (isSelfAdmin) {
            layoutTeamMute.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            layoutTeamMute.setVisibility(View.VISIBLE);
        }


        ((TextView) layoutTeamMute.findViewById(R.id.item_title)).setText("群内禁言");
        ((TextView) layoutTeamMute.findViewById(R.id.item_detail)).setHint("");
        //群成员活跃度layout点击监听
        layoutTeamMute.setOnClickListener(v -> {
            TeamMuteActivity.start(TeamManagmentActivity.this, team.getId(), team.isAllMute());
        });
    }


    /**
     * 机器人
     */
    private void initTeamMemberRobot() {
        teamRobot = findViewById(R.id.team_member_robot_layout);
        if (isSelfAdmin) {
            teamRobot.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            teamRobot.setVisibility(View.GONE);
        }
        setMarginTop(teamRobot);
        teamRobot.findViewById(R.id.line).setVisibility(View.GONE);
        ((TextView) teamRobot.findViewById(R.id.item_title)).setText("机器人");
        ((TextView) teamRobot.findViewById(R.id.item_detail)).setHint("");
        teamRobot.setOnClickListener(v -> {
            String url = String.format("scheme://ychat/teamrobot?teamId=%1$s&teamCreator=%2$s", teamId, team.getCreator());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    /**
     * 群成员活跃度
     */
    private void initTeamMemberActivity() {
        teamMemberActivity = findViewById(R.id.team_member_activity_layout);
        if (isSelfAdmin) {
            teamMemberActivity.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            teamMemberActivity.setVisibility(View.VISIBLE);
        }


        ((TextView) teamMemberActivity.findViewById(R.id.item_title)).setText("群成员活跃度");
        ((TextView) teamMemberActivity.findViewById(R.id.item_detail)).setHint("");
        //群成员活跃度layout点击监听
        teamMemberActivity.setOnClickListener(v -> {
            TeamMemberChatTimeActivity.start(TeamManagmentActivity.this, teamId, REQUEST_CODE_REMOVE);
        });
    }

    /**
     * 群成员变动记录
     */
    private void initTeamMemberChangeRecord() {
        teamMemberChangeRecord = findViewById(R.id.team_member_change_record_layout);
        if (isSelfAdmin) {
            teamMemberChangeRecord.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            teamMemberChangeRecord.setVisibility(View.VISIBLE);
        }

        ((TextView) teamMemberChangeRecord.findViewById(R.id.item_title)).setText("群成员变动记录");
        ((TextView) teamMemberChangeRecord.findViewById(R.id.item_detail)).setHint("");
        teamMemberChangeRecord.findViewById(R.id.line).setVisibility(View.GONE);
        //群成员变动记录layout点击监听
        teamMemberChangeRecord.setOnClickListener(v -> {
            TeamMemberChangeActivity.start(TeamManagmentActivity.this, teamId);
        });
    }

    private void setMarginTop(View layout) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.topMargin = ScreenUtil.dip2px(10f);
    }

    /**
     * 一键复制群
     */
    private void initCopyTeam() {
        layoutCopyTeam = findViewById(R.id.team_copy_layout);
        if (isSelfAdmin) {
            layoutCopyTeam.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            layoutCopyTeam.setVisibility(View.GONE);
        }

        setMarginTop(layoutCopyTeam);
        ((TextView) layoutCopyTeam.findViewById(R.id.item_title)).setText("一键复制新群");
        ((TextView) layoutCopyTeam.findViewById(R.id.item_detail)).setHint("");
        layoutCopyTeam.setOnClickListener(v -> {
            try {
                Class tchClass = Class.forName("com.xr.ychat.team.activity.AdvancedTeamCopyActivity");
                Method method = tchClass.getMethod("start", Activity.class, String.class);//得到方法对象
                method.invoke(null, this, teamId);//调用创建高级群方法
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 转让群
     */
    private void initTransferTeam() {
        layoutTransferTeam = findViewById(R.id.team_transfer_team_layout);
        if (isSelfAdmin) {
            layoutTransferTeam.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            layoutTransferTeam.setVisibility(View.GONE);
        }

        ((TextView) layoutTransferTeam.findViewById(R.id.item_title)).setText(R.string.transfer_team);
        ((TextView) layoutTransferTeam.findViewById(R.id.item_detail)).setHint("");
        layoutTransferTeam.findViewById(R.id.line).setVisibility(View.GONE);
        layoutTransferTeam.setOnClickListener(v -> transferTeamAndEixtDialog());
    }

    /**
     * 更新被邀请人detail显示
     *
     * @param type 被邀请人类型
     */
    private void updateBeInvitedText(TeamBeInviteModeEnum type) {
        inviteeAutenText.setText(TeamHelper.getBeInvitedModeString(type));
    }

    /**
     * 更新邀请他人detail显示
     *
     * @param type 邀请他人类型
     */
    private void updateInviteText(TeamInviteModeEnum type) {
        inviteText.setText(TeamHelper.getInviteModeString(type));
    }

    /**
     * 设置验证模式detail显示
     *
     * @param type 验证类型
     */
    private void setAuthenticationText(VerifyTypeEnum type) {
        authenticationText.setText(TeamHelper.getVerifyString(type));
    }

    /**
     * 更新群资料修改detail显示
     *
     * @param type 群资料修改类型
     */
    private void updateInfoUpateText(TeamUpdateModeEnum type) {
        infoUpdateText.setText(TeamHelper.getInfoUpdateModeString(type));
    }

    /**
     * 更新被邀请人权限
     *
     * @param type 被邀请人类型
     */
    private void updateBeInvitedMode(final TeamBeInviteModeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.BeInviteMode, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                updateBeInvitedText(type);
                YchatToastUtils.showShort(R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
                teamInviteeDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort(String.format(getString(R.string.update_failed), code));
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    // 显示被邀请人身份验证菜单
    private void showTeamInviteeAuthenMenu() {
        if (teamInviteeDialog == null) {
            List<String> btnNames = TeamHelper.createTeamInviteeAuthenMenuStrings();

            int type = team.getTeamBeInviteMode().getValue();
            teamInviteeDialog = new MenuDialog(this, btnNames, type, 2, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    teamInviteeDialog.dismiss();

                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    TeamBeInviteModeEnum type = TeamHelper.getBeInvitedModeEnum(name);
                    if (type != null) {
                        updateBeInvitedMode(type);
                    }
                }
            });
        }
        teamInviteeDialog.show();
    }

    /**
     * 被邀请人身份验证布局初始化
     */
    private void findLayoutInviteeAuthen() {
        layoutInviteeAuthen = findViewById(R.id.team_invitee_authen_layout);
        if (isSelfAdmin) {
            layoutInviteeAuthen.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            layoutInviteeAuthen.setVisibility(View.GONE);
        }

        layoutInviteeAuthen.findViewById(R.id.line).setVisibility(View.GONE);
        ((TextView) layoutInviteeAuthen.findViewById(R.id.item_title)).setText(R.string.team_invitee_authentication);
        inviteeAutenText = ((TextView) layoutInviteeAuthen.findViewById(R.id.item_detail));
        layoutInviteeAuthen.setOnClickListener(v -> showTeamInviteeAuthenMenu());
    }

    /**
     * 更新邀请他人权限
     *
     * @param type 邀请他人类型
     */
    private void updateInviteMode(final TeamInviteModeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.InviteMode, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                updateInviteText(type);
                YchatToastUtils.showShort(R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
                inviteDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort(String.format(getString(R.string.update_failed), code));
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 邀请他人权限布局初始化
     */
    private void findLayoutInvite() {
        layoutInvite = findViewById(R.id.team_invite_layout);
        if (isSelfAdmin) {
            layoutInvite.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            layoutInvite.setVisibility(View.GONE);
        }

        setMarginTop(layoutInvite);
        ((TextView) layoutInvite.findViewById(R.id.item_title)).setText(R.string.team_invite);
        inviteText = ((TextView) layoutInvite.findViewById(R.id.item_detail));
        layoutInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamInviteMenu();
            }
        });
    }

    /**
     * 显示邀请他人权限菜单
     */
    private void showTeamInviteMenu() {
        if (inviteDialog == null) {
            List<String> btnNames = TeamHelper.createInviteMenuStrings();

            int type = team.getTeamInviteMode().getValue();
            inviteDialog = new MenuDialog(this, btnNames, type, 2, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    inviteDialog.dismiss();
                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    TeamInviteModeEnum type = TeamHelper.getInviteModeEnum(name);
                    if (type != null) {
                        updateInviteMode(type);
                    }
                }
            });
        }
        inviteDialog.show();
    }

    // 显示群资料修改权限菜单
    private void showTeamInfoUpdateMenu() {
        if (teamInfoUpdateDialog == null) {
            List<String> btnNames = TeamHelper.createTeamInfoUpdateMenuStrings();

            int type = team.getTeamUpdateMode().getValue();
            teamInfoUpdateDialog = new MenuDialog(this, btnNames, type, 2, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    teamInfoUpdateDialog.dismiss();

                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    TeamUpdateModeEnum type = TeamHelper.getUpdateModeEnum(name);
                    if (type != null) {
                        updateInfoUpdateMode(type);
                    }
                }
            });
        }
        teamInfoUpdateDialog.show();
    }

    /**
     * 群资料修改权限布局初始化
     */
    private void findLayoutInfoUpdate() {
        layoutInfoUpdate = findViewById(R.id.team_info_update_layout);
        if (isSelfAdmin) {
            layoutInfoUpdate.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            layoutInfoUpdate.setVisibility(View.GONE);
        }

        ((TextView) layoutInfoUpdate.findViewById(R.id.item_title)).setText(R.string.team_info_update);
        infoUpdateText = ((TextView) layoutInfoUpdate.findViewById(R.id.item_detail));
        layoutInfoUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamInfoUpdateMenu();
            }
        });
    }

    /**
     * 身份验证布局初始化
     */
    private void findLayoutAuthentication() {
        layoutAuthentication = findViewById(R.id.team_authentication_layout);
        if (isSelfAdmin) {
            layoutAuthentication.setVisibility(View.VISIBLE);
        } else if (isSelfManager) {
            layoutAuthentication.setVisibility(View.GONE);
        }

        setMarginTop(layoutAuthentication);
        ((TextView) layoutAuthentication.findViewById(R.id.item_title)).setText(R.string.team_authentication);
        layoutAuthentication.findViewById(R.id.line).setVisibility(View.GONE);
        authenticationText = ((TextView) layoutAuthentication.findViewById(R.id.item_detail));
        layoutAuthentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamAuthenMenu();
            }
        });
    }

    private void findLayoutControlExit() {
        layoutControlExit = findViewById(R.id.team_member_control_exit_layout);
        layoutControlExit.setVisibility(View.GONE);
        controlExitText = (TextView) findViewById(R.id.team_member_control_exit);
        controlExitText.setVisibility(View.GONE);
        if (isSelfAdmin) {
            layoutControlExit.setVisibility(View.VISIBLE);
            controlExitText.setVisibility(View.VISIBLE);
        }

        setMarginTop(layoutControlExit);
        ((TextView) layoutControlExit.findViewById(R.id.item_title)).setText("退群验证");
        layoutControlExit.findViewById(R.id.line).setVisibility(View.GONE);
        switchButtonControlExit = layoutControlExit.findViewById(R.id.setting_item_toggle);
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                TeamExtension extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
                if (TeamExtras.OPEN.equals(extension.getLeaveTeamVerify())) {
                    switchButtonControlExit.setCheck(true);
                } else {
                    switchButtonControlExit.setCheck(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            switchButtonControlExit.setCheck(false);
        }
        switchButtonControlExit.setOnChangedListener((v, checkState) -> {
            if (checkState) {
                synExtensionControlExit(TeamExtras.OPEN, true);
            } else {
                synExtensionControlExit(TeamExtras.CLOSE, false);
            }
        });
    }

    private void synExtensionControlExit(String protectValue, boolean checkState) {
        TeamExtension extension;
        if (!TextUtils.isEmpty(team.getExtension())) {
            LogUtils.e(team.getExtension());
            try {
                extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
            } catch (Exception exception) {
                extension = new TeamExtension();
            }
        } else {
            extension = new TeamExtension();
        }
        extension.setLeaveTeamVerify(protectValue);
        extension.setExtensionType(2);
        String extensionString = gson.toJson(extension);
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.Extension, extensionString).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                team.setExtension(extensionString);
                switchButtonControlExit.setCheck(checkState);
                if (checkState) {
                    YchatToastUtils.showShort("退群验证开启");
                } else {
                    YchatToastUtils.showShort("退群验证关闭");
                }
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                switchButtonControlExit.setCheck(!checkState);
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
                switchButtonControlExit.setCheck(!checkState);
            }
        });
    }

    /**
     * 显示验证菜单
     */
    private void showTeamAuthenMenu() {
        if (authenDialog == null) {
            List<String> btnNames = TeamHelper.createAuthenMenuStrings();

            int type = team.getVerifyType().getValue();
            authenDialog = new MenuDialog(this, btnNames, type, 3, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    authenDialog.dismiss();

                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    VerifyTypeEnum type = TeamHelper.getVerifyTypeEnum(name);
                    if (type != null) {
                        setAuthen(type);
                    }

                }
            });
        }
        authenDialog.show();
    }

    /**
     * 设置验证模式
     *
     * @param type 验证类型
     */
    private void setAuthen(final VerifyTypeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.VerifyType, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                setAuthenticationText(type);
                YchatToastUtils.showShort(R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
                authenDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort(String.format(getString(R.string.update_failed), code));
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 更新群资料修改权限
     *
     * @param type 群资料修改类型
     */
    private void updateInfoUpdateMode(final TeamUpdateModeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.TeamUpdateMode, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                updateInfoUpateText(type);
                YchatToastUtils.showShort(R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
                teamInfoUpdateDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort(String.format(getString(R.string.update_failed), code));
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_TRANSFER://转让群
                final ArrayList<String> target = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (target != null && !target.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("TRANSFER_NAME", target.get(0));
                    intent.putExtra("TRANSFER_TYPE", 0);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
            case REQUEST_CODE_TRANSFER_AND_EXIT://转让群并退出
                final ArrayList<String> targetEixt = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (targetEixt != null && !targetEixt.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("TRANSFER_NAME", targetEixt.get(0));
                    intent.putExtra("TRANSFER_TYPE", 1);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
            case REQUEST_CODE_REMOVE://删除群成员
                final ArrayList<String> targetRemove = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (targetRemove != null && !targetRemove.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("TRANSFER_NAME", targetRemove);
                    intent.putExtra("TRANSFER_TYPE", 2);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
        }
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
