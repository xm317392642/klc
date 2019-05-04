package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.nim.uikit.api.model.team.TeamMemberDataChangedObserver;
import com.netease.nim.uikit.api.model.user.UserInfoObserver;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.recent.RecentContactsFragment;
import com.netease.nim.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter.TeamMemberItem;
import com.netease.nim.uikit.business.team.helper.AnnouncementHelper;
import com.netease.nim.uikit.business.team.helper.RemovePreventRedpacketService;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.team.helper.UpdateTeamStatusService;
import com.netease.nim.uikit.business.team.model.Announcement;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.business.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ApplyLeaveTeam;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.GroupCodeActivity;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高级群群资料页
 * Created by huangjun on 2015/3/17.
 */
public class AdvancedTeamInfoActivity extends SwipeBackUI implements
        TAdapterDelegate, TeamMemberAdapter.AddMemberCallback, TeamMemberAdapter.RemoveMemberCallback, TeamMemberHolder.TeamMemberHolderEventListener {

    private static final int REQUEST_CODE_TRANSFER = 101;//转让群
    private static final int REQUEST_CODE_TRANSFER_AND_EXIT = 104;//群主转让群并且退出，先调用转让群transferTeam(final String account)  接口成功后，成为普通成员后在调用 退出接口 quitTeam
    private static final int REQUEST_CODE_MEMBER_LIST = 102;
    private static final int REQUEST_CODE_CONTACT_SELECT = 103;
    private static final int REQUEST_CODE_CONTACT_SELECT_DELETE = 105;

    private static final int ICON_TIME_OUT = 30000;

    // constant
    private static final String TAG = "RegularTeamInfoActivity";

    private static final String EXTRA_ID = "EXTRA_ID";
    public static final String RESULT_EXTRA_REASON = "RESULT_EXTRA_REASON";
    public static final String RESULT_EXTRA_REASON_QUIT = "RESULT_EXTRA_REASON_QUIT";
    public static final String RESULT_EXTRA_REASON_DISMISS = "RESULT_EXTRA_REASON_DISMISS";

    private static final int TEAM_MEMBERS_SHOW_LIMIT = 20;

    // data
    private TeamMemberAdapter adapter;
    private String teamId;
    private Team team;
    private String creator;
    private String robotId;
    private List<String> memberAccounts;
    private List<TeamMember> members;
    private List<TeamMemberAdapter.TeamMemberItem> dataSource;
    private MenuDialog dialog;
    private MenuDialog authenDialog;
    private List<String> managerList;
    private UserInfoObserver userInfoObserver;
    private AbortableFuture<String> uploadFuture;

    // view
    private Toolbar mToolbar;
    private TextView toolbarTitle;

    private TextView teamBusinessCard; // 我的群名片

    private View layoutMime;
    private TeamInfoGridView gridView;
    private View layoutTeamName;
    private View layoutTeamNumber;
    private View layoutTeamCode;
    private View layoutTeamIntroduce;
    private View layoutTeamAnnouncement;
    private View layoutFindChat;
    private View layoutUnclaimedEnvelope;
    private View teamManagmentlayout;//群管理
    private View layoutNotificationConfig;
    private View layoutBurnView, layoutAutoClear, screenCaptureView;
    private TextView introduceEdit;
    private TextView announcementEdit;
    private TextView deleteAndExitText;
    private TextView teamSeeAllMember;
    //private TextView notificationConfigText;
    private SwitchButton msgSwitchButton, topSwitchButton, burnSwitchBtn, autoClearSwitchBtn, screenCaptureSwitchBtn;
    // state
    private boolean isSelfAdmin = false;
    private boolean isSelfManager = false;

    public static void start(Context context, String tid) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, AdvancedTeamInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * ************************ TAdapterDelegate **************************
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return TeamMemberHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    /**
     * ***************************** Life cycle *****************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nim_advanced_team_info_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());

        parseIntentData();
        //截屏通知
        initTeamScreenshot();
        findViews();
        //initActionbar();//不显示右上角的菜单了
        initAdapter();
        loadTeamInfo();

        requestMembers();
        registerObservers(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CONTACT_SELECT:
                final ArrayList<String> addSelected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (addSelected != null && !addSelected.isEmpty()) {
                    inviteMembers(addSelected);
                }
                break;
            case REQUEST_CODE_CONTACT_SELECT_DELETE:
                final ArrayList<String> removeSelected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (removeSelected != null && !removeSelected.isEmpty()) {
                    removeMembers(removeSelected);
                }
                break;
            case REQUEST_CODE_TRANSFER://转让群
                int type = data.getIntExtra("TRANSFER_TYPE", 0);
                if (type == 0) {
                    String target = data.getStringExtra("TRANSFER_NAME");
                    transferTeam(target, REQUEST_CODE_TRANSFER);
                } else if (type == 1) {
                    String target = data.getStringExtra("TRANSFER_NAME");
                    transferTeam(target, REQUEST_CODE_TRANSFER_AND_EXIT);
                } else {
                    ArrayList<String> targetRemove = data.getStringArrayListExtra("TRANSFER_NAME");
                    removeMembers(targetRemove);
                }
                break;
            case REQUEST_CODE_TRANSFER_AND_EXIT://转让群并退出
                final ArrayList<String> targetEixt = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (targetEixt != null && !targetEixt.isEmpty()) {
                    transferTeam(targetEixt.get(0), REQUEST_CODE_TRANSFER_AND_EXIT);
                }
                break;
            case AdvancedTeamNicknameActivity.REQ_CODE_TEAM_NAME:
                setBusinessCard(data.getStringExtra(AdvancedTeamNicknameActivity.EXTRA_NAME));
                break;
            case AdvancedTeamMemberInfoActivity.REQ_CODE_REMOVE_MEMBER:
                String account = data.getStringExtra(EXTRA_ID);
                if (TextUtils.equals(account, teamId)) {
                    boolean isSetAdmin = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISADMIN, false);
                    boolean isRemoveMember = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISREMOVE, false);
                    refreshAdmin(isSetAdmin, account);
                    if (isRemoveMember) {
                        removeMember(account);
                    }
                }
                break;
            case REQUEST_CODE_MEMBER_LIST:
                boolean isMemberChange = data.getBooleanExtra(AdvancedTeamMemberActivity.EXTRA_DATA, false);
                if (isMemberChange) {
                    requestMembers();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            dialog.dismiss();
        }

        if (authenDialog != null) {
            authenDialog.dismiss();
        }

        registerObservers(false);

        super.onDestroy();
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void findViews() {
        layoutMime = findViewById(R.id.team_mime_layout);
        ((TextView) layoutMime.findViewById(R.id.item_title)).setText(R.string.my_team_card);
        teamBusinessCard = (TextView) layoutMime.findViewById(R.id.item_detail);
        String teamMemberDisplayName = TeamHelper.getTeamMemberDisplayName(teamId, NimUIKit.getAccount());
        teamBusinessCard.setText(teamMemberDisplayName);
        layoutMime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvancedTeamNicknameActivity.start(AdvancedTeamInfoActivity.this, teamBusinessCard.getText().toString(), UserInfoHelper.getUserName(NimUIKit.getAccount()));
            }
        });

        gridView = (TeamInfoGridView) findViewById(R.id.team_member_grid_view);
        gridView.setVisibility(View.GONE);

        layoutTeamName = findViewById(R.id.team_name_layout);
        ((TextView) layoutTeamName.findViewById(R.id.item_title)).setText(R.string.team_name);
        layoutTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TeamHelper.changeTeamInfo(teamId, NimUIKit.getAccount())) {
                    TeamPropertySettingActivity.start(AdvancedTeamInfoActivity.this, teamId, TeamFieldEnum.Name, team.getName());
                } else {
                    YchatToastUtils.showShort("群主管理员更改");
                }
            }
        });
        layoutTeamNumber = findViewById(R.id.team_number_layout);
        ((TextView) layoutTeamNumber.findViewById(R.id.item_title)).setText("群ID");

        layoutTeamCode = findViewById(R.id.team_code_layout);
        ((TextView) layoutTeamCode.findViewById(R.id.item_title)).setText(R.string.team_code);
        layoutTeamCode.setOnClickListener(v -> {
            GroupCodeActivity.start(AdvancedTeamInfoActivity.this, team);
        });

        layoutTeamIntroduce = findViewById(R.id.team_introduce_layout);
        layoutTeamIntroduce.findViewById(R.id.line).setVisibility(View.GONE);
        ((TextView) layoutTeamIntroduce.findViewById(R.id.item_title)).setText(R.string.team_introduce);
        introduceEdit = layoutTeamIntroduce.findViewById(R.id.item_detail);
        introduceEdit.setHint("");
        introduceEdit.setSingleLine(false);
        introduceEdit.setMaxLines(1);
        introduceEdit.setMaxEms(12);
        introduceEdit.setEllipsize(TextUtils.TruncateAt.END);
        layoutTeamIntroduce.setOnClickListener(v ->
        {
            if (TeamHelper.changeTeamInfo(teamId, NimUIKit.getAccount())) {
                TeamPropertySettingActivity.start(AdvancedTeamInfoActivity.this, teamId, TeamFieldEnum.Introduce, team.getIntroduce());
            } else {
                YchatToastUtils.showShort("群主管理员更改");
            }
        });
        layoutTeamAnnouncement = findViewById(R.id.team_announcement_layout);

        teamManagmentlayout = findViewById(R.id.team_managment_layout);

        ((TextView) layoutTeamAnnouncement.findViewById(R.id.item_title)).setText(R.string.team_annourcement);
        ((TextView) teamManagmentlayout.findViewById(R.id.item_title)).setText("群管理");
        ((TextView) teamManagmentlayout.findViewById(R.id.item_detail)).setHint("");
        teamManagmentlayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeamManagmentActivity.class);
            intent.putExtra("teamId", teamId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, REQUEST_CODE_TRANSFER);
        });
        announcementEdit = ((TextView) layoutTeamAnnouncement.findViewById(R.id.item_detail));
        announcementEdit.setHint(R.string.team_announce_hint);
        layoutTeamAnnouncement.setOnClickListener(v ->
        {
            AdvancedTeamAnnounceActivity.start(AdvancedTeamInfoActivity.this, teamId, TeamHelper.changeTeamInfo(teamId, NimUIKit.getAccount()));
        });
        //清空聊天记录
        findViewById(R.id.item_clear_chat_record).setOnClickListener((v) -> {
            clearMsgRecordDialog();
        });
        //查找聊天记录
        layoutFindChat = findViewById(R.id.team_find_chat_layout);
        ((TextView) layoutFindChat.findViewById(R.id.item_title)).setText("查找聊天内容");
        ((TextView) layoutFindChat.findViewById(R.id.item_detail)).setHint("");
        layoutFindChat.setOnClickListener(v -> {
            String url = String.format("scheme://ychat/sessionsearch?EXTRA_SESSION_ID=%1$s&EXTRA_SESSION_TYPE=%2$s", teamId, String.valueOf(SessionTypeEnum.Team.getValue()));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        //长时间未领取红包
        layoutUnclaimedEnvelope = findViewById(R.id.team_unclaimed_envelope_layout);
        ((TextView) layoutUnclaimedEnvelope.findViewById(R.id.item_title)).setText("长时间未领红包");
        ((TextView) layoutUnclaimedEnvelope.findViewById(R.id.item_detail)).setHint("");
        layoutUnclaimedEnvelope.findViewById(R.id.line).setVisibility(View.GONE);
        layoutUnclaimedEnvelope.setOnClickListener(v -> {
            String url = String.format("scheme://ychat/unclaimedenvelope?EXTRA_SESSION_ID=%1$s", teamId);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        //删除并退出
        deleteAndExitText = (TextView) findViewById(R.id.item_delete_and_exit);
        teamSeeAllMember = (TextView) findViewById(R.id.team_see_all_member);

        deleteAndExitText.setOnClickListener(v -> {
            deleteDialog();
        });
        //群消息置顶设置
        initTopWithToggle();
        // 群消息提醒设置
        initNotifyWithToggle();
        //阅后即焚设置-48小时自动清理
        initBurnAndAutoClear();

    }


    /**
     * 清空聊天记录对话框提示
     */
    public void clearMsgRecordDialog() {
        List<String> btnNames = new ArrayList<>(1);
        btnNames.add(getString(R.string.clear_team_msg));
        btnNames.add(getString(R.string.ok));
        btnNames.add(getString(R.string.cancel));
        dialog = new MenuDialog(this, btnNames, name -> {
            if (name.equals(getString(R.string.ok))) {
                NIMClient.getService(MsgService.class).clearChattingHistory(teamId, SessionTypeEnum.Team);
                MessageListPanelHelper.getInstance().notifyClearMessages(teamId);
                YchatToastUtils.showShort("聊天记录已清空");
            }
            if (name.equals(getString(R.string.ok)) || name.equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 删除并退出的对话框提示
     */
    public void deleteDialog() {

        List<String> btnNames = new ArrayList<>(3);
        if (isSelfAdmin) {
            btnNames.add(getString(R.string.delete_and_exit));
        } else {
            btnNames.add(getString(R.string.out_team));
        }
        btnNames.add(getString(R.string.ok));
        btnNames.add(getString(R.string.cancel));
        dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
            @Override
            public void onButtonClick(String name) {
                if (name.equals(getString(R.string.ok))) {
                    if (isSelfAdmin) {
                        dismissTeam();
                    } else {
                        quitTeam();
                    }
                }
                if (name.equals(getString(R.string.ok)) || name.equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }


    /**
     * 48小时自动清除该群的聊天记录
     */
    private void initAutoClearView() {
        autoClearSwitchBtn.setOnChangedListener((v, checkState) -> {
            if (checkState) {
                baMap.put(TeamExtras.AUTO_CLEAR, TeamExtras.OPEN);//1是开启
            } else {
                baMap.put(TeamExtras.AUTO_CLEAR, TeamExtras.CLOSE);//0是关闭
            }
            NIMClient.getService(TeamService.class).updateMyMemberExtension(teamId, baMap);
        });
    }

    private Map<String, Object> baMap = new HashMap<>(2);
    Gson gson = new Gson();

    /**
     * 更新截屏通知开关
     */
    private void updateScreenshotSwitch() {
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                TeamExtension extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
                if (TeamExtras.OPEN.equals(extension.getScreenshotNotify())) {
                    screenCaptureSwitchBtn.setCheck(true);
                } else {
                    screenCaptureSwitchBtn.setCheck(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            screenCaptureSwitchBtn.setCheck(false);
        }
    }

    /**
     * 初始化截屏通知
     */
    private void initTeamScreenshot() {
        screenCaptureView = findViewById(R.id.team_screen_capture_layout);
        screenCaptureSwitchBtn = screenCaptureView.findViewById(R.id.setting_item_toggle);
        screenCaptureView.findViewById(R.id.line).setVisibility(View.GONE);
        ((TextView) screenCaptureView.findViewById(R.id.item_title)).setText("截屏通知");
        screenCaptureSwitchBtn.setOnChangedListener((v, checkState) -> {
            if (isSelfManager || isSelfAdmin) {
                if (checkState) {
                    synExtensionToServer(TeamExtras.OPEN, true);
                } else {
                    synExtensionToServer(TeamExtras.CLOSE, false);
                }
            } else {
                screenCaptureSwitchBtn.setCheck(!checkState);
                YchatToastUtils.showShort("只有群管理员可以切换截屏通知开关");
            }

        });
    }

    /**
     * 把新增的截屏通知字段同步存储到服务器
     *
     * @param value
     */
    private void synExtensionToServer(String value, boolean checkState) {
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
        extension.setScreenshotNotify(value);
        extension.setExtensionType(3);
        String extensionString = gson.toJson(extension);
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.Extension, extensionString).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                team.setExtension(extensionString);
                screenCaptureSwitchBtn.setCheck(checkState);
                if (checkState) {
                    YchatToastUtils.showShort("截屏通知开启");
                } else {
                    YchatToastUtils.showShort("截屏通知关闭");
                }
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                screenCaptureSwitchBtn.setCheck(!checkState);
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
                screenCaptureSwitchBtn.setCheck(!checkState);
            }
        });
    }

    private void initBurnAndAutoClear() {
        layoutBurnView = findViewById(R.id.team_burn_config_layout);
        burnSwitchBtn = layoutBurnView.findViewById(R.id.setting_item_toggle);
        layoutBurnView.findViewById(R.id.line).setVisibility(View.GONE);
        ((TextView) layoutBurnView.findViewById(R.id.item_title)).setText("阅后即焚");

        layoutAutoClear = findViewById(R.id.team_msg_auto_clear_layout);
        autoClearSwitchBtn = layoutAutoClear.findViewById(R.id.setting_item_toggle);
        layoutAutoClear.findViewById(R.id.line).setVisibility(View.GONE);
        ((TextView) layoutAutoClear.findViewById(R.id.item_title)).setText("群消息自动清理");

        NimUIKit.getTeamProvider().fetchTeamMember(teamId, NimUIKit.getAccount(), (boolean success, TeamMember teamMember, int code) -> {
            if (success && teamMember != null) {
                Map<String, Object> map = teamMember.getExtension();
                if (map.containsKey(TeamExtras.FIRE_MSG)) {
                    if (TeamExtras.OPEN.equals(map.get(TeamExtras.FIRE_MSG))) {
                        burnSwitchBtn.setCheck(true);
                        baMap.put(TeamExtras.FIRE_MSG, TeamExtras.OPEN);
                    } else {
                        burnSwitchBtn.setCheck(false);
                        baMap.put(TeamExtras.FIRE_MSG, TeamExtras.CLOSE);
                    }
                } else {
                    burnSwitchBtn.setCheck(false);
                    baMap.put(TeamExtras.FIRE_MSG, TeamExtras.CLOSE);
                }


                if (map.containsKey(TeamExtras.AUTO_CLEAR)) {
                    if (TeamExtras.OPEN.equals(map.get(TeamExtras.AUTO_CLEAR))) {
                        autoClearSwitchBtn.setCheck(true);
                        baMap.put(TeamExtras.AUTO_CLEAR, TeamExtras.OPEN);
                    } else {
                        autoClearSwitchBtn.setCheck(false);
                        baMap.put(TeamExtras.AUTO_CLEAR, TeamExtras.CLOSE);
                    }
                } else {
                    autoClearSwitchBtn.setCheck(false);
                    baMap.put(TeamExtras.AUTO_CLEAR, TeamExtras.CLOSE);
                }
            } else {
                baMap.put(TeamExtras.FIRE_MSG, TeamExtras.CLOSE);
                baMap.put(TeamExtras.AUTO_CLEAR, TeamExtras.CLOSE);
            }
        });

        initBurnView();
        initAutoClearView();
    }

    /**
     * 阅后即焚初始化
     */
    private void initBurnView() {
        burnSwitchBtn.setOnChangedListener((v, checkState) -> {
            if (checkState) {
                baMap.put(TeamExtras.FIRE_MSG, TeamExtras.OPEN);//1是开启
            } else {
                baMap.put(TeamExtras.FIRE_MSG, TeamExtras.CLOSE);//0是关闭
            }
            NIMClient.getService(TeamService.class).updateMyMemberExtension(teamId, baMap).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
//                    if (checkState) {
//                        ToastHelper.showToast(AdvancedTeamInfoActivity.this, "阅后即焚开启");
//                    } else {
//                        ToastHelper.showToast(AdvancedTeamInfoActivity.this, "阅后即焚关闭");
//                    }
                }

                @Override
                public void onFailed(int code) {
                    burnSwitchBtn.setCheck(!checkState);
                    YchatToastUtils.showShort("onFailed=" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    YchatToastUtils.showShort("exception=" + exception.getMessage());
                }
            });
        });
    }

    private void clearTeamMemberStatus() {
        Map<String, Object> baMap = new HashMap<>(2);
        baMap.put(TeamExtras.FIRE_MSG, TeamExtras.CLOSE);
        baMap.put(TeamExtras.AUTO_CLEAR, TeamExtras.CLOSE);
        NIMClient.getService(TeamService.class).updateMyMemberExtension(teamId, baMap);
    }

    /**
     * 群消息置顶(带开关的)
     */
    private void initTopWithToggle() {
        layoutNotificationConfig = findViewById(R.id.team_top_setup_config_layout);
        ((TextView) layoutNotificationConfig.findViewById(R.id.item_title)).setText("聊天置顶");
        topSwitchButton = layoutNotificationConfig.findViewById(R.id.setting_item_toggle);
        ////查询之前是不是存在会话记录
        RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(teamId, SessionTypeEnum.Team);
        boolean isSticky = recentContact != null && CommonUtil.isTagSet(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
        topSwitchButton.setCheck(isSticky);//是否置顶
        topSwitchButton.setOnChangedListener((View v, boolean checkState) -> {
            //查询之前是不是存在会话记录
            //RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(teamId, SessionTypeEnum.Team);
            //置顶
            if (checkState) {
                //如果之前不存在，创建一条空的会话记录
                if (recentContact == null) {
                    // RecentContactsFragment 的 MsgServiceObserve#observeRecentContact 观察者会收到通知
                    NIMClient.getService(MsgService.class).createEmptyRecentContact(teamId,
                            SessionTypeEnum.Team,
                            RecentContactsFragment.RECENT_TAG_STICKY,
                            System.currentTimeMillis(),
                            true);
                }
                // 之前存在，更新置顶flag
                else {
                    CommonUtil.addTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                    NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                }
            }
            //取消置顶
            else {
                if (recentContact != null) {
                    CommonUtil.removeTag(recentContact, RecentContactsFragment.RECENT_TAG_STICKY);
                    NIMClient.getService(MsgService.class).updateRecentAndNotify(recentContact);
                }
            }
            topSwitchButton.setCheck(checkState);//是否置顶

        });
    }

    /**
     * 群消息提醒设置(带开关的)
     */
    private void initNotifyWithToggle() {
        layoutNotificationConfig = findViewById(R.id.team_notification_config_layout2);
        ((TextView) layoutNotificationConfig.findViewById(R.id.item_title)).setText("消息免打扰");
        msgSwitchButton = layoutNotificationConfig.findViewById(R.id.setting_item_toggle);
        msgSwitchButton.setOnChangedListener((View v, boolean checkState) -> {
            String name = checkState ? getString(R.string.team_notify_mute) : getString(R.string.team_notify_all);
            TeamMessageNotifyTypeEnum type = TeamHelper.getNotifyType(name);
            DialogMaker.showProgressDialog(AdvancedTeamInfoActivity.this, getString(R.string.empty), true);
            NIMClient.getService(TeamService.class).muteTeam(teamId, type).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    DialogMaker.dismissProgressDialog();
                    msgSwitchButton.setCheck(checkState);
                }

                @Override
                public void onFailed(int code) {
                    DialogMaker.dismissProgressDialog();
                    msgSwitchButton.setCheck(!checkState);
                }

                @Override
                public void onException(Throwable exception) {
                    DialogMaker.dismissProgressDialog();
                    msgSwitchButton.setCheck(!checkState);
                }
            });

        });
    }

    private void initAdapter() {
        memberAccounts = new ArrayList<>();
        members = new ArrayList<>();
        dataSource = new ArrayList<>();
        managerList = new ArrayList<>();
        adapter = new TeamMemberAdapter(this, dataSource, this, this, this);
        adapter.setEventListener(this);

        gridView.setSelector(R.color.transparent);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        gridView.setAdapter(adapter);
    }

    /**
     * 初始化群组基本信息
     */
    private void loadTeamInfo() {
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    } else {
                        onGetTeamInfoFailed();
                    }
                }
            });
        }
    }

    private void onGetTeamInfoFailed() {
        YchatToastUtils.showShort(getString(R.string.team_not_exist));
        finish();
    }

    /**
     * 更新群信息
     *
     * @param t
     */
    private void updateTeamInfo(final Team t) {
        this.team = t;

        if (team == null) {
            YchatToastUtils.showShort(getString(R.string.team_not_exist));
            finish();
            return;
        } else {
            creator = team.getCreator();
            robotId = getRobotId(team);
            if (creator.equals(NimUIKit.getAccount())) {
                isSelfAdmin = true;
            }
        }


        ((TextView) layoutTeamName.findViewById(R.id.item_detail)).setText(team.getName());
        ((TextView) layoutTeamNumber.findViewById(R.id.item_detail)).setText(team.getId());
        introduceEdit.setText(team.getIntroduce());
        int memberNumber = team.getMemberCount() - (TextUtils.isEmpty(robotId) ? 0 : 1);
        toolbarTitle.setText(team.getName() + "(" + memberNumber + ")");
        if (memberNumber > TEAM_MEMBERS_SHOW_LIMIT) {
            teamSeeAllMember.setVisibility(View.VISIBLE);
            teamSeeAllMember.setOnClickListener(v -> AdvancedTeamMemberActivity.startActivityForResult(AdvancedTeamInfoActivity.this, teamId, REQUEST_CODE_MEMBER_LIST));
        } else {
            teamSeeAllMember.setVisibility(View.GONE);

        }

        //setAnnouncement(team.getAnnouncement());
        updateTeamNotifyText(team.getMessageNotifyType());
        if (isSelfAdmin) {
            deleteAndExitText.setText(getString(R.string.dismiss_team));
        } else {
            deleteAndExitText.setText(getString(R.string.quit_team));
        }
        updateScreenshotSwitch();
    }

    /**
     * 更新群成员信息
     *
     * @param m
     */
    private void updateTeamMember(final List<TeamMember> m) {
        if (m != null && m.isEmpty()) {
            return;
        }

        updateTeamBusinessCard(m);
        addTeamMembers(m, true);
    }

    /**
     * 更新我的群名片
     *
     * @param m
     */
    private void updateTeamBusinessCard(List<TeamMember> m) {
        for (TeamMember teamMember : m) {
            if (teamMember != null && teamMember.getAccount().equals(NimUIKit.getAccount())) {
                String teamMemberDisplayName = TeamHelper.getTeamMemberDisplayName(teamMember.getTid(), teamMember.getAccount());
                teamBusinessCard.setText(!TextUtils.isEmpty(teamMember.getTeamNick()) ? teamMember.getTeamNick() : teamMemberDisplayName);
            }
        }
    }

    /**
     * 添加群成员到列表
     *
     * @param m     群成员列表
     * @param clear 是否清除
     */
    private void addTeamMembers(final List<TeamMember> m, boolean clear) {
        if (m == null || m.isEmpty()) {
            return;
        }

        isSelfManager = false;
        isSelfAdmin = false;

        if (clear) {
            this.members.clear();
            this.memberAccounts.clear();
        }

        // add
        if (this.members.isEmpty()) {
            this.members.addAll(m);
        } else {
            for (TeamMember tm : m) {
                if (!this.memberAccounts.contains(tm.getAccount())) {
                    this.members.add(tm);
                }
            }
        }

        // sort
        Collections.sort(this.members, TeamHelper.teamMemberComparator);

        // accounts, manager, creator
        this.memberAccounts.clear();
        this.managerList.clear();
        for (TeamMember tm : members) {
            if (tm == null) {
                continue;
            }
            if (tm.getType() == TeamMemberType.Manager) {
                managerList.add(tm.getAccount());
            }
            if (tm.getAccount().equals(NimUIKit.getAccount())) {
                if (tm.getType() == TeamMemberType.Manager) {
                    isSelfManager = true;
                } else if (tm.getType() == TeamMemberType.Owner) {
                    isSelfAdmin = true;
                    creator = NimUIKit.getAccount();
                }
            }
            if (TeamHelper.isTeamMember(teamId, tm.getAccount())) {
                this.memberAccounts.add(tm.getAccount());
            }
        }

        updateAuthenView();
        updateTeamMemberDataSource();
        updateUnclaimedEnvelope();
    }

    /**
     * 群管理,群ID是否显示
     */
    private void updateAuthenView() {
        if (isSelfAdmin || isSelfManager) {
            teamManagmentlayout.setVisibility(View.VISIBLE);
        } else {
            teamManagmentlayout.setVisibility(View.GONE);
        }
        if (isSelfAdmin || isSelfManager) {
            layoutTeamNumber.setVisibility(View.VISIBLE);
        } else {
            layoutTeamNumber.setVisibility(View.GONE);
        }
       /* if (isSelfAdmin || isSelfManager) {
            layoutAuthentication.setVisibility(View.VISIBLE);
            layoutInvite.setVisibility(View.VISIBLE);
            layoutInfoUpdate.setVisibility(View.VISIBLE);
            layoutInviteeAuthen.setVisibility(View.VISIBLE);
        } else {
            layoutAuthentication.setVisibility(View.GONE);
            layoutInvite.setVisibility(View.GONE);
            layoutInfoUpdate.setVisibility(View.GONE);
            layoutInviteeAuthen.setVisibility(View.GONE);
            introduceEdit.setHint(R.string.without_content);
        }
        //只有群主才有转让群的权限
        if (isSelfAdmin) {
            layoutTransferTeam.setVisibility(View.VISIBLE);
        } else {
            layoutTransferTeam.setVisibility(View.GONE);
        }*/
    }

    /**
     * 长时间未领取红包是否显示
     */
    private void updateUnclaimedEnvelope() {
        if (isSelfAdmin || isSelfManager) {
            layoutUnclaimedEnvelope.setVisibility(View.GONE);
            layoutFindChat.findViewById(R.id.line).setVisibility(View.VISIBLE);
        } else {
            layoutUnclaimedEnvelope.setVisibility(View.GONE);
            layoutFindChat.findViewById(R.id.line).setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新成员信息
     */
    private void updateTeamMemberDataSource() {
        if (members.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
        } else {
            gridView.setVisibility(View.GONE);
            return;
        }

        dataSource.clear();


        // member item
        int count = 0;
        String identity = null;
        for (String account : memberAccounts) {
            int limit = TEAM_MEMBERS_SHOW_LIMIT;

            if ((isSelfAdmin || isSelfManager) && memberAccounts.size() > 1) {
                limit = TEAM_MEMBERS_SHOW_LIMIT - 2;
            } else if (team.getTeamInviteMode() == TeamInviteModeEnum.All || isSelfAdmin || isSelfManager) {
                limit = TEAM_MEMBERS_SHOW_LIMIT - 1;
            }
            if (count < limit) {
                if (!TextUtils.equals(robotId, account)) {
                    identity = getIdentity(account);
                    dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                            .NORMAL, teamId, account, identity));
                }
            }
            count++;
        }

        // add item
        if (team.getTeamInviteMode() == TeamInviteModeEnum.All || isSelfAdmin || isSelfManager) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null,
                    null));
        }
        // remove item
        //只有群主和管理员才有移除人的权限  并且  有群成员的情况下才添加删除图标
        if ((isSelfAdmin || isSelfManager) && memberAccounts.size() > 1) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.DELETE, null, null,
                    null));
        }

        // refresh
        adapter.notifyDataSetChanged();
    }

    private String getIdentity(String account) {
        String identity;
        if (creator.equals(account)) {
            identity = TeamMemberHolder.OWNER;
        } else if (managerList.contains(account)) {
            identity = TeamMemberHolder.ADMIN;
        } else {
            identity = null;
        }
        return identity;
    }

    /**
     * *************************** 加载&变更数据源 ********************************
     */
    private void requestMembers() {
        NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> members, int code) {
                if (success && members != null && !members.isEmpty()) {
                    updateTeamMember(members);
                }
            }
        });
    }

    /**
     * ************************** 群信息变更监听 **************************
     */
    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
        registerUserInfoChangedObserver(register);
    }

    TeamMemberDataChangedObserver teamMemberObserver = new TeamMemberDataChangedObserver() {

        @Override
        public void onUpdateTeamMember(List<TeamMember> m) {
            if (m != null && m.size() > 0) {
                TeamMember teamMember = m.get(0);
                if (TextUtils.equals(teamMember.getTid(), teamId)) {
                    for (TeamMember mm : m) {
                        for (TeamMember member : members) {
                            if (mm.getAccount().equals(member.getAccount())) {
                                members.set(members.indexOf(member), mm);
                                break;
                            }
                        }
                    }
                    addTeamMembers(m, false);
                }
            }
        }

        @Override
        public void onRemoveTeamMember(List<TeamMember> removedList) {
            if (removedList != null && removedList.size() > 0) {
                TeamMember teamMember = removedList.get(0);
                if (TextUtils.equals(teamMember.getTid(), teamId)) {
                    for (TeamMember removeTeamMember : removedList) {
                        //removeMember(removeTeamMember.getAccount());
                        String account = removeTeamMember.getAccount();
                        memberAccounts.remove(account);
                        for (TeamMember m : members) {
                            if (m.getAccount().equals(account)) {
                                members.remove(m);
                                break;
                            }
                        }
                        for (TeamMemberItem item : dataSource) {
                            if (item.getAccount() != null && item.getAccount().equals(account)) {
                                dataSource.remove(item);
                                break;
                            }
                        }
                    }
                    //updateAuthenView();
                    updateTeamMemberDataSource();
                }
            }
        }
    };

    TeamDataChangedObserver teamDataObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            for (Team team : teams) {
                if (team.getId().equals(teamId)) {
                    updateTeamInfo(team);
                    updateTeamMemberDataSource();
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team.getId().equals(teamId)) {
                AdvancedTeamInfoActivity.this.team = team;
                clearTeamMemberStatus();
                finish();
            }
        }
    };

    /**
     * ******************************* Action *********************************
     */

    /**
     * 从联系人选择器发起邀请成员
     */
    @Override
    public void onAddMember() {
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        option.title = "邀请成员";
        NimUIKit.startContactSelector(AdvancedTeamInfoActivity.this, option, REQUEST_CODE_CONTACT_SELECT);
    }

    /**
     * 选择删除的群成员
     *
     * @param account
     */
    @Override
    public void onRemoveMember(String account) {
        if (memberAccounts.size() <= 1) {
            YchatToastUtils.showShort("没有可删除的群成员");
            return;
        }
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = "删除成员";
        option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
        option.teamId = teamId;
        option.multi = true;
        option.searchVisible = true;
        //需要过滤（不显示）的联系人项
        String userAccount = NimUIKit.getAccount();
        ArrayList<String> includeAccounts = new ArrayList<>();
        if (!TextUtils.isEmpty(robotId)) {
            includeAccounts.add(robotId);
        }
        if (isSelfManager) {
            includeAccounts.add(team.getCreator());
            includeAccounts.addAll(managerList);
        } else {
            includeAccounts.add(userAccount);
        }
        option.itemFilter = new ContactIdFilter(includeAccounts, true);
        NimUIKit.startContactSelector(AdvancedTeamInfoActivity.this, option, REQUEST_CODE_CONTACT_SELECT_DELETE);
    }

    /**
     * 从联系人选择器选择群转移对象
     * requestCode:是转让群 还是  转让并退出
     */
    private void onTransferTeam(int requestCode) {
        if (memberAccounts.size() <= 1) {
            YchatToastUtils.showShort(R.string.team_transfer_without_member);
            return;
        }
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
        NimUIKit.startContactSelector(AdvancedTeamInfoActivity.this, option, requestCode);
    }

    /**
     * 删除选中的群成员
     *
     * @param accounts
     */
    private void removeMembers(ArrayList<String> accounts) {
        if (accounts == null || accounts.size() == 0) {
            YchatToastUtils.showShort("没有可删除的群成员");
            return;
        }
        if (accounts.contains(creator)) {
            YchatToastUtils.showShort("不能移除群主");
            return;
        }
        if (isSelfManager) {
            for (String account : accounts) {
                if (managerList.contains(account)) {
                    YchatToastUtils.showShort("不能移除管理员");
                    return;
                }
            }
        }
        NIMClient.getService(TeamService.class).removeMembers(teamId, accounts).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                UpdateMemberChangeService.start(AdvancedTeamInfoActivity.this, createMembersString(accounts), teamId, 2);
                YchatToastUtils.showShort("删除群成员成功");
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_TEAM_ENACCESS) {
                    YchatToastUtils.showShort("没有权限删除群成员");
                } else {
                    YchatToastUtils.showShort("delete members failed,code=" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                YchatToastUtils.showShort("exception=" + exception.getMessage());

            }
        });
    }

    /**
     * 邀请群成员
     *
     * @param accounts 邀请帐号
     */
    private void inviteMembers(ArrayList<String> accounts) {
        NIMClient.getService(TeamService.class).addMembers(teamId, accounts).setCallback(new RequestCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> failedAccounts) {
                if (failedAccounts == null || failedAccounts.isEmpty()) {
                    UpdateMemberChangeService.start(AdvancedTeamInfoActivity.this, createMembersString(accounts), teamId, 1, NimUIKit.getAccount());
                    YchatToastUtils.showShort("添加群成员成功");
                } else {
                    TeamHelper.onMemberTeamNumOverrun(failedAccounts, AdvancedTeamInfoActivity.this);
                }
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_TEAM_INVITE_SUCCESS) {
                    YchatToastUtils.showShort(R.string.team_invite_members_success);
                } else if (code == ResponseCode.RES_TEAM_ENACCESS) {
                    YchatToastUtils.showShort("您没有邀请进群的权限");
                } else {
                    YchatToastUtils.showShort("invite members failed, code=" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    /**
     * 转让群
     * <p>
     * type 分为单纯的转让群 and   转让并退出
     *
     * @param account 转让的帐号
     */
    private void transferTeam(final String account, final int type) {
        TeamMember member = NimUIKit.getTeamProvider().getTeamMember(teamId, account);
        if (member == null) {
            YchatToastUtils.showShort("成员不存在");
            return;
        }
        if (member.isMute()) {
            YchatToastUtils.showShort("该成员已被禁言，请先取消禁言");
            return;
        }
        NIMClient.getService(TeamService.class).transferTeam(teamId, account, false)
                .setCallback(new RequestCallback<List<TeamMember>>() {
                    @Override
                    public void onSuccess(List<TeamMember> members) {
                        creator = account;
                        RemovePreventRedpacketService.start(AdvancedTeamInfoActivity.this, teamId, creator);
                        updateTeamMember(NimUIKit.getTeamProvider().getTeamMemberList(teamId));
                        if (type == REQUEST_CODE_TRANSFER_AND_EXIT) {
                            quitTeam(REQUEST_CODE_TRANSFER_AND_EXIT);//转让群成功后，成为普通成员，再调用一次 退出群
                        } else {
                            YchatToastUtils.showShort(R.string.team_transfer_success);
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        YchatToastUtils.showShort(R.string.team_transfer_failed);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
    }


    /**
     * 群主退出群
     */
    private void quitTeam(int type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).quitTeam(teamId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort(R.string.quit_team_success);
                UpdateMemberChangeService.start(AdvancedTeamInfoActivity.this, NimUIKit.getAccount(), teamId, 2);
                setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_EXTRA_REASON, RESULT_EXTRA_REASON_QUIT));
                finish();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort(R.string.quit_team_failed);
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });

    }

    private void quitTeam() {
        Gson gson = new Gson();
        TeamExtension extension;
        try {
            extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
            }.getType());
        } catch (Exception e) {
            extension = new TeamExtension();
        }
        if (extension != null && TeamExtras.OPEN.equals(extension.getLeaveTeamVerify())) {
            CustomNotification command = new CustomNotification();
            command.setFromAccount(NimUIKit.getAccount());
            command.setSessionId(creator);
            command.setSessionType(SessionTypeEnum.P2P);
            ApplyLeaveTeam applyLeaveTeam = new ApplyLeaveTeam();
            applyLeaveTeam.setLeaveTeamID(teamId);
            applyLeaveTeam.setTeamLeaveType(0);
            applyLeaveTeam.setId(2);
            applyLeaveTeam.setContent(UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()) + "申请退出群聊" + TeamHelper.getTeamName(teamId));
            command.setContent(gson.toJson(applyLeaveTeam));
            command.setApnsText(UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()) + "[退群请求]");
            command.setSendToOnlineUserOnly(false);
            NIMClient.getService(MsgService.class).sendCustomNotification(command).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    YchatToastUtils.showShort("已发送退群申请");
                }

                @Override
                public void onFailed(int code) {
                    YchatToastUtils.showShort("退群申请发送失败");
                }

                @Override
                public void onException(Throwable exception) {
                    YchatToastUtils.showShort("退群申请发送失败");
                }
            });
        } else {
            DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
            NIMClient.getService(TeamService.class).quitTeam(teamId).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    DialogMaker.dismissProgressDialog();
                    YchatToastUtils.showShort(R.string.quit_team_success);
                    clearTeamMemberStatus();
                    UpdateMemberChangeService.start(AdvancedTeamInfoActivity.this, NimUIKit.getAccount(), teamId, 2);
                    setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_EXTRA_REASON, RESULT_EXTRA_REASON_QUIT));
                    finish();
                }

                @Override
                public void onFailed(int code) {
                    DialogMaker.dismissProgressDialog();
                    YchatToastUtils.showShort(R.string.quit_team_failed);
                }

                @Override
                public void onException(Throwable exception) {
                    DialogMaker.dismissProgressDialog();
                }
            });
        }
    }

    /**
     * 群主解散群(直接退出)
     */
    private void dismissTeam() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).dismissTeam(teamId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                UpdateTeamStatusService.start(AdvancedTeamInfoActivity.this, teamId, 2);
                DialogMaker.dismissProgressDialog();
                setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_EXTRA_REASON, RESULT_EXTRA_REASON_DISMISS));
                YchatToastUtils.showShort(R.string.dismiss_team_success);
                finish();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort(R.string.dismiss_team_failed);
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * ******************************* Event *********************************
     */
    /**
     * 显示菜单
     */
    private void showRegularTeamMenu() {
        List<String> btnNames = new ArrayList<>();
        if (isSelfAdmin) {
            btnNames.add(getString(R.string.dismiss_team));
            btnNames.add(getString(R.string.transfer_team));
            btnNames.add(getString(R.string.cancel));
        } else {
            btnNames.add(getString(R.string.quit_team));
            btnNames.add(getString(R.string.cancel));
        }
        dialog = new MenuDialog(this, btnNames, (name -> {
            if (name.equals(getString(R.string.quit_team))) {
                quitTeam();
            } else if (name.equals(getString(R.string.dismiss_team))) {
                dismissTeam();
            } else if (name.equals(getString(R.string.transfer_team))) {
                onTransferTeam(REQUEST_CODE_TRANSFER);
            }
            dialog.dismiss();
        }));

        dialog.show();
    }

    /**
     * 显示验证菜单
     */
    private void showTeamAuthenMenu() {
        if (authenDialog == null) {
            List<String> btnNames = TeamHelper.createAuthenMenuStrings();

            int type = team.getVerifyType().getValue();
            authenDialog = new MenuDialog(AdvancedTeamInfoActivity.this, btnNames, type, 3, new MenuDialog
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
     * 设置我的名片
     *
     * @param nickname 群昵称
     */
    private void setBusinessCard(final String nickname) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).updateMemberNick(teamId, NimUIKit.getAccount(), nickname).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                teamBusinessCard.setText(nickname);
                YchatToastUtils.showShort(R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
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
    public void onHeadImageViewClick(String account) {
        // 打开群成员信息详细页面
        AdvancedTeamMemberInfoActivity.startActivityForResult(AdvancedTeamInfoActivity.this, account, teamId, TextUtils.equals(creator, account));
    }

    /**
     * 设置群公告
     *
     * @param announcement 群公告
     */
    private void setAnnouncement(String announcement) {
        Announcement a = AnnouncementHelper.getLastAnnouncement(teamId, announcement);
        if (a == null) {
            announcementEdit.setText("");
        } else {
            announcementEdit.setText(a.getContent());
        }
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


    private void updateTeamNotifyText(TeamMessageNotifyTypeEnum typeEnum) {
        if (typeEnum == TeamMessageNotifyTypeEnum.All) {
            //notificationConfigText.setText(getString(R.string.team_notify_all));
            msgSwitchButton.setCheck(false);
        } else if (typeEnum == TeamMessageNotifyTypeEnum.Manager) {
            //notificationConfigText.setText(getString(R.string.team_notify_manager));
            msgSwitchButton.setCheck(false);
        } else if (typeEnum == TeamMessageNotifyTypeEnum.Mute) {
            //notificationConfigText.setText(getString(R.string.team_notify_mute));
            msgSwitchButton.setCheck(true);
        }
    }


    /**
     * 移除群成员成功后，删除列表中的群成员
     *
     * @param account 被删除成员帐号
     */
    private void removeMember(String account) {
        if (TextUtils.isEmpty(account)) {
            return;
        }

        memberAccounts.remove(account);
        for (TeamMember m : members) {
            if (m.getAccount().equals(account)) {
                members.remove(m);
                break;
            }
        }
        for (TeamMemberItem item : dataSource) {
            if (item.getAccount() != null && item.getAccount().equals(account)) {
                dataSource.remove(item);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 是否设置了管理员刷新界面
     *
     * @param isSetAdmin
     * @param account
     */
    private void refreshAdmin(boolean isSetAdmin, String account) {
        if (isSetAdmin) {
            if (managerList.contains(account)) {
                return;
            }
            managerList.add(account);
            updateTeamMemberDataSource();
        } else {
            if (managerList.contains(account)) {
                managerList.remove(account);
                updateTeamMemberDataSource();
            }
        }
    }

    private void registerUserInfoChangedObserver(boolean register) {
        if (register) {
            if (userInfoObserver == null) {
                userInfoObserver = new UserInfoObserver() {
                    @Override
                    public void onUserInfoChanged(List<String> accounts) {
                        adapter.notifyDataSetChanged();
                    }
                };
            }
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true);
        } else {
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false);
        }
    }

    private String createMembersString(ArrayList<String> accounts) {
        if (accounts == null || accounts.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int selectedNumber = accounts.size();
        for (int i = 0; i < selectedNumber; i++) {
            builder.append(accounts.get(i));
            if (i != selectedNumber - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
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
