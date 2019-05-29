package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.TeamMethodInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群成员详细信息界面
 * Created by hzxuwen on 2015/3/19.
 */
public class AdvancedTeamMemberInfoActivity extends SwipeBackUI implements View.OnClickListener {
    // constant
    public static final int REQ_CODE_REMOVE_MEMBER = 11;
    private static final String EXTRA_ID = "EXTRA_ID";
    private static final String EXTRA_TID = "EXTRA_TID";
    private static final String EXTRA_TNAME = "EXTRA_TNAME";
    public static final String EXTRA_ISADMIN = "EXTRA_ISADMIN";
    public static final String EXTRA_ISREMOVE = "EXTRA_ISREMOVE";
    private final String KEY_MUTE_MSG = "mute_msg";

    // data
    private String account;
    private String teamId;
    private TeamMember viewMember;
    private boolean isSetAdmin;
    private Map<String, Boolean> toggleStateMap;
    private NimUserInfo userInfo;

    // view
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private HeadImageView headImageView;
    private TextView memberName;
    private TextView nickName;
    private TextView identity;
    private View nickContainer;
    private View removeBtn;
    private TextView contactBtn;
    private View identityContainer;
    private MenuDialog setAdminDialog;
    private MenuDialog cancelAdminDialog;
    private MenuDialog removeMenuDialog;
    private ViewGroup toggleLayout;
    private SwitchButton muteSwitch;
    private TextView invite_type_detail;
    private View teamContactView;
    // state
    private boolean isSelfCreator = false;
    private boolean isSelfManager = false;
    private Team currentTeam;

    public static void startActivityForResult(Activity activity, String account, String tid, boolean isOwner) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, account);
        intent.putExtra(EXTRA_TID, tid);
        intent.putExtra(EXTRA_TNAME, isOwner);
        intent.setClass(activity, AdvancedTeamMemberInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, REQ_CODE_REMOVE_MEMBER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.nim_advanced_team_member_info_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText(R.string.team_member_info);
        mToolbar.setNavigationOnClickListener(v -> finish());

        parseIntentData();

        findViews();

        loadMemberInfo();

        initMemberInfo();
        registerTeamUpdateObserver(true);
    }

    private void parseIntentData() {
        account = getIntent().getStringExtra(EXTRA_ID);
        teamId = getIntent().getStringExtra(EXTRA_TID);
    }

    private void registerTeamUpdateObserver(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataChangedObserver, register);
    }

    TeamDataChangedObserver teamDataChangedObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            for (Team team : teams) {
                if (team.getId().equals(teamId)) {
                    currentTeam = team;
                    updateContactBtn();
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        updateToggleView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (setAdminDialog != null) {
            setAdminDialog.dismiss();
        }
        if (cancelAdminDialog != null) {
            cancelAdminDialog.dismiss();
        }
        if (removeMenuDialog != null) {
            removeMenuDialog.dismiss();
        }
        registerTeamUpdateObserver(false);
    }


    /**
     * 添加通讯录或者发消息或者
     * 本应该为添加通讯录的时候隐藏掉（因为群主开启了群成员保护模式，不能私下添加好友）
     */
    private void updateContactBtn() {
        String extension = currentTeam.getExtension();
        //如果是自己的话，就没有添加通讯录这个按钮
        if (TextUtils.equals(account, NimUIKit.getAccount())) {
            teamContactView.setVisibility(View.GONE);
        } else {
            //不是自己的情况
            if (!NimUIKit.getContactProvider().isMyFriend(account)) {
                //不是好友的情况下
                if (!TextUtils.isEmpty(extension)) {
                    //检查群成员保护模式字段
                    try {
                        Gson gson = new Gson();
                        TeamExtension teamExtension = gson.fromJson(extension, new TypeToken<TeamExtension>() {
                        }.getType());
                        //群成员之间无法通过该群添加好友，管理员或者群主除外
                        if (TextUtils.equals(TeamExtras.OPEN, teamExtension.getMemberProtect())) {
                            TeamMemberType teamMemberType = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount()).getType();
                            if (teamMemberType == TeamMemberType.Owner) {
                                setTeamContactViewVisibility();//管理员或者群主的话还是可以添加好友的
                            } else {
                                teamContactView.setVisibility(View.GONE);
                            }
                        } else {
                            setTeamContactViewVisibility();
                        }
                    } catch (Exception e) {
                        setTeamContactViewVisibility();
                    }
                } else {
                    setTeamContactViewVisibility();
                }
            } else {
                contactBtn.setText("发消息");
                contactBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.send_msg_icon), null, null, null);
            }
        }
    }

    private void findViews() {
        nickContainer = findViewById(R.id.nickname_container);
        invite_type_detail = (TextView) findViewById(R.id.invite_type_detail);
        identityContainer = findViewById(R.id.identity_container);
        headImageView = (HeadImageView) findViewById(R.id.team_member_head_view);
        memberName = (TextView) findViewById(R.id.team_member_name);
        nickName = (TextView) findViewById(R.id.team_nickname_detail);
        identity = (TextView) findViewById(R.id.team_member_identity_detail);
        removeBtn = findViewById(R.id.team_remove_member);
        contactBtn = (TextView) findViewById(R.id.team_contact_member);
        teamContactView = findViewById(R.id.team_contact_member_view);
        toggleLayout = findView(R.id.toggle_layout);
        setClickListener();
        currentTeam = NimUIKit.getTeamProvider().getTeamById(teamId);
        //updateContactBtn();
    }

    private void setTeamContactViewVisibility() {
        teamContactView.setVisibility(View.VISIBLE);
        contactBtn.setText("添加到通讯录");
        contactBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private void setClickListener() {
        nickContainer.setOnClickListener(this);
        identityContainer.setOnClickListener(this);
        removeBtn.setOnClickListener(this);
        headImageView.setOnClickListener(this);
        findViewById(R.id.team_contact_member_view).setOnClickListener(this);
    }

    private void updateToggleView() {
        if (getMyPermission()) {
            boolean isMute = NimUIKit.getTeamProvider().getTeamMember(teamId, account).isMute();
            if (muteSwitch == null) {
                addToggleBtn(isMute);
            } else {
                setToggleBtn(muteSwitch, isMute);
            }
        }
    }

    // 判断是否有权限
    private boolean getMyPermission() {
        if (isSelfCreator && !isSelf(account)) {
            return true;
        }
        if (isSelfManager && identity.getText().toString().equals(getString(R.string.team_member))) {
            return true;
        }
        return false;
    }

    private void addToggleBtn(boolean isMute) {
        muteSwitch = addToggleItemView(KEY_MUTE_MSG, R.string.mute_msg, isMute);
    }

    private void setToggleBtn(SwitchButton btn, boolean isChecked) {
        btn.setCheck(isChecked);
    }

    private SwitchButton addToggleItemView(String key, int titleResId, boolean initState) {
        ViewGroup vp = (ViewGroup) getLayoutInflater().inflate(R.layout.nim_user_profile_toggle_item, null);
        vp.findViewById(R.id.line).setVisibility(View.GONE);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.isetting_item_height));
        vp.setLayoutParams(vlp);

        TextView titleText = ((TextView) vp.findViewById(R.id.user_profile_title));
        titleText.setText(titleResId);

        SwitchButton switchButton = (SwitchButton) vp.findViewById(R.id.user_profile_toggle);
        switchButton.setCheck(initState);
        switchButton.setOnChangedListener(onChangedListener);
        switchButton.setTag(key);

        toggleLayout.addView(vp);

        if (toggleStateMap == null) {
            toggleStateMap = new HashMap<>();
        }
        toggleStateMap.put(key, initState);
        return switchButton;
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            final String key = (String) v.getTag();
            if (!NetworkUtil.isNetAvailable(AdvancedTeamMemberInfoActivity.this)) {
                YchatToastUtils.showShort(R.string.network_is_not_available);
                if (key.equals(KEY_MUTE_MSG)) {
                    muteSwitch.setCheck(!checkState);
                }
                return;
            }

            updateStateMap(checkState, key);

            if (key.equals(KEY_MUTE_MSG)) {
                NIMClient.getService(TeamService.class).muteTeamMember(teamId, account, checkState).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        if (checkState) {
                            YchatToastUtils.showShort("群禁言成功");
                        } else {
                            YchatToastUtils.showShort("取消群禁言成功");
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 408) {
                            YchatToastUtils.showShort(R.string.network_is_not_available);
                        } else {
                            YchatToastUtils.showShort("on failed:" + code);
                        }
                        updateStateMap(!checkState, key);
                        muteSwitch.setCheck(!checkState);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
            }
        }
    };

    private void updateStateMap(boolean checkState, String key) {
        if (toggleStateMap.containsKey(key)) {
            toggleStateMap.put(key, checkState);  // update state
        }
    }

    private void loadMemberInfo() {
        viewMember = NimUIKit.getTeamProvider().getTeamMember(teamId, account);
        if (viewMember != null) {
            updateMemberInfo();
        } else {
            requestMemberInfo();
        }
    }

    /**
     * 查询群成员的信息
     */
    private void requestMemberInfo() {
        NimUIKit.getTeamProvider().fetchTeamMember(teamId, account, new SimpleCallback<TeamMember>() {
            @Override
            public void onResult(boolean success, TeamMember member, int code) {
                if (success && member != null) {
                    viewMember = member;
                    updateMemberInfo();
                }
            }
        });
    }

    private void initMemberInfo() {
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        userInfo = result;
                        updateUI();
                    }
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        memberName.setText(userInfo.getName());
        headImageView.loadAvatar(userInfo.getAvatar());
    }

    private void updateMemberInfo() {
        updateMemberExtent();
        updateMemberIdentity();
        updateMemberNickname();
        updateSelfIndentity();
        updateRemoveBtn();
        updateContactBtn();
    }

    private void updateMemberExtent() {
        String weiranUid = Preferences.getWeiranUid(AdvancedTeamMemberInfoActivity.this);
        String weiranToken = Preferences.getWeiranToken(AdvancedTeamMemberInfoActivity.this);
        ContactHttpClient.getInstance().queryTeamMemberChangeMethod(weiranUid, weiranToken, teamId, account, new ContactHttpClient.ContactHttpCallback<TeamMethodInfo>() {
            @Override
            public void onSuccess(TeamMethodInfo info) {
                String note = info.getNote();
                if (TextUtils.isEmpty(note)) {
                    invite_type_detail.setText("暂无");
                } else {
                    if (TextUtils.equals(note, "qr")) {
                        invite_type_detail.setText("扫描二维码进群");
                    } else {
                        if (TextUtils.equals(account, note)) {
                            invite_type_detail.setText("创建者");
                        } else {
                            SpannableStringBuilder sb = new SpannableStringBuilder();
                            String displayName = UserInfoHelper.getUserDisplayName(note);
                            sb.append(displayName);
                            String content = " 邀请进群";
                            sb.append(content);
                            sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(AdvancedTeamMemberInfoActivity.this, R.color.color_be6913)), 0, displayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            invite_type_detail.setText(sb);
                        }
                    }
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    /**
     * 更新群成员的身份
     */
    private void updateMemberIdentity() {
        if (viewMember.getType() == TeamMemberType.Manager) {
            identity.setText(R.string.team_admin);
            isSetAdmin = true;
        } else {
            isSetAdmin = false;
            if (viewMember.getType() == TeamMemberType.Owner) {
                identity.setText(R.string.team_creator);
            } else {
                identity.setText(R.string.team_member);
            }
        }
    }

    /**
     * 更新成员群昵称
     */
    private void updateMemberNickname() {
        nickName.setText(viewMember.getTeamNick() != null ? viewMember.getTeamNick() : TeamHelper.getTeamMemberDisplayName(teamId, NimUIKit.getAccount()));
    }

    /**
     * 获得用户自己的身份
     */
    private void updateSelfIndentity() {
        TeamMember selfTeamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
        if (selfTeamMember == null) {
            return;
        }
        if (selfTeamMember.getType() == TeamMemberType.Manager) {
            isSelfManager = true;
        } else if (selfTeamMember.getType() == TeamMemberType.Owner) {
            isSelfCreator = true;
        }
    }

    /**
     * 更新是否显移除本群按钮
     */
    private void updateRemoveBtn() {
        if (TextUtils.equals(viewMember.getAccount(), NimUIKit.getAccount())) {
            removeBtn.setVisibility(View.GONE);
        } else {
            if (isSelfCreator) {
                removeBtn.setVisibility(View.VISIBLE);
            } else if (isSelfManager) {
                if (viewMember.getType() == TeamMemberType.Owner) {
                    removeBtn.setVisibility(View.GONE);
                } else if (viewMember.getType() == TeamMemberType.Normal) {
                    removeBtn.setVisibility(View.VISIBLE);
                } else {
                    removeBtn.setVisibility(View.GONE);
                }
            } else {
                removeBtn.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 更新群昵称
     *
     * @param name
     */
    private void setNickname(final String name) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).updateMemberNick(teamId, account, name).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                nickName.setText(name != null ? name : getString(R.string.team_nickname_none));
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
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.nickname_container) {
            editNickname();
        } else if (i == R.id.identity_container) {
            showManagerButton();
        } else if (i == R.id.team_remove_member) {
            showConfirmButton();
        } else if (i == R.id.team_member_head_view) {
            if (userInfo != null) {
                String url = String.format("scheme://ychat/imagepreview?extra_image=%1$s", userInfo.getAvatar());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        } else {
            if (!NimUIKit.getContactProvider().isMyFriend(account)) {
                onAddFriendByVerify();
            } else {
                NimUIKit.startP2PSession(this, account);
            }
        }
    }

    private void onAddFriendByVerify() {
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().querySearching(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), 2, account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (!TextUtils.isEmpty(aVoid.getAccid()) && aVoid.getAccid().length() > 1) {
                    final EasyEditDialog requestDialog = new EasyEditDialog(AdvancedTeamMemberInfoActivity.this);
                    requestDialog.setEditText("我是群聊'" + TeamHelper.getTeamName(teamId) + "'的" + TeamHelper.getTeamMemberDisplayName(teamId, NimUIKit.getAccount()));
                    requestDialog.setTitle("好友验证请求");
                    requestDialog.addNegativeButtonListener(R.string.cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestDialog.dismiss();
                        }
                    });
                    requestDialog.addPositiveButtonListener(R.string.send, R.color.color_activity_blue_bg, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestDialog.dismiss();
                            String msg = requestDialog.getEditMessage();
                            if (TextUtils.isEmpty(msg)) {
                                msg = String.format("我是%1$s", UserInfoHelper.getUserName(account));
                            }
                            doAddFriend(msg, false);
                        }
                    });
                    requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    });
                    requestDialog.show();
                } else {
                    YchatToastUtils.showShort("由于对方的隐私设置，您无法通过群聊将其添加至通讯录");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("由于对方的隐私设置，您无法通过群聊将其添加至通讯录");
            }
        });
    }

    private void doAddFriend(String msg, boolean addDirectly) {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        if (!TextUtils.isEmpty(account) && account.equals(NimUIKit.getAccount())) {
            YchatToastUtils.showShort("不能加自己为好友");
            return;
        }
        boolean black = NIMClient.getService(FriendService.class).isInBlackList(account);
        if (black) {
            YchatToastUtils.showShort("该用户在您的黑名单中，请先移除再添加");
            return;
        }
        final VerifyType verifyType = addDirectly ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
        DialogMaker.showProgressDialog(this, "", true);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(account, verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        DialogMaker.dismissProgressDialog();
                        if (VerifyType.DIRECT_ADD == verifyType) {
                            contactBtn.setText("发消息");
                            Map<String, Object> content = new HashMap<>(1);
                            content.put("content", "我们已经是好友，现在可以开始聊天了");
                            IMMessage msg = MessageBuilder.createTipMessage(account, SessionTypeEnum.P2P);
                            msg.setRemoteExtension(content);
                            CustomMessageConfig config = new CustomMessageConfig();
                            config.enableUnreadCount = false;
                            msg.setConfig(config);
                            msg.setStatus(MsgStatusEnum.success);
                            NIMClient.getService(MsgService.class).sendMessage(msg, false);
                            YchatToastUtils.showShort("添加好友成功");
                        } else {
                            YchatToastUtils.showShort("添加好友请求发送成功");
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == 408) {
                            YchatToastUtils.showShort(R.string.network_is_not_available);
                        } else {
                            YchatToastUtils.showShort("on failed:" + code);
                        }
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
        if (requestCode == AdvancedTeamNicknameActivity.REQ_CODE_TEAM_NAME && resultCode == Activity.RESULT_OK) {
            final String teamName = data.getStringExtra(AdvancedTeamNicknameActivity.EXTRA_NAME);
            setNickname(teamName);
        }
    }

    /**
     * 设置群昵称
     */
    private void editNickname() {
        if (isSelfCreator || isSelf(account)) {
            AdvancedTeamNicknameActivity.start(AdvancedTeamMemberInfoActivity.this, nickName.getText().toString(), UserInfoHelper.getUserDisplayName(account));
        } else if (isSelfManager && identity.getText().toString().equals(getString(R.string.team_member))) {
            AdvancedTeamNicknameActivity.start(AdvancedTeamMemberInfoActivity.this, nickName.getText().toString(), UserInfoHelper.getUserDisplayName(account));
        } else {
            YchatToastUtils.showShort(R.string.no_permission);
        }
    }


    /**
     * 显示设置管理员按钮
     */
    private void showManagerButton() {
        if (identity.getText().toString().equals(getString(R.string.team_creator))) {
            return;
        }
        if (!isSelfCreator)
            return;

        if (identity.getText().toString().equals(getString(R.string.team_member))) {
            switchManagerButton(true);
        } else {
            switchManagerButton(false);
        }
    }

    /**
     * 转换设置或取消管理员按钮
     *
     * @param isSet 是否设置
     */
    private void switchManagerButton(boolean isSet) {
        if (isSet) {
            if (setAdminDialog == null) {
                List<String> btnNames = new ArrayList<>();
                btnNames.add(getString(R.string.set_team_admin));
                setAdminDialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
                    @Override
                    public void onButtonClick(String name) {
                        addManagers();
                        setAdminDialog.dismiss();
                    }
                });
            }
            setAdminDialog.show();
        } else {
            if (cancelAdminDialog == null) {
                List<String> btnNames = new ArrayList<>();
                btnNames.add(getString(R.string.cancel_team_admin));
                cancelAdminDialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
                    @Override
                    public void onButtonClick(String name) {
                        removeManagers();
                        cancelAdminDialog.dismiss();
                    }
                });
            }
            cancelAdminDialog.show();
        }
    }

    /**
     * 添加管理员权限
     */
    private void addManagers() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        ArrayList<String> accountList = new ArrayList<>();
        accountList.add(account);
        NIMClient.getService(TeamService.class).addManagers(teamId, accountList).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> managers) {
                DialogMaker.dismissProgressDialog();
                identity.setText(R.string.team_admin);
                YchatToastUtils.showShort(R.string.update_success);

                viewMember = managers.get(0);
                updateMemberInfo();
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

    /**
     * 撤销管理员权限
     */
    private void removeManagers() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        ArrayList<String> accountList = new ArrayList<>();
        accountList.add(account);
        NIMClient.getService(TeamService.class).removeManagers(teamId, accountList).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> members) {
                DialogMaker.dismissProgressDialog();
                identity.setText(R.string.team_member);
                YchatToastUtils.showShort(R.string.update_success);

                viewMember = members.get(0);
                updateMemberInfo();
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

    /**
     * 移除群成员确认
     */
    private void showConfirmButton() {
//        EasyAlertDialogHelper.OnDialogActionListener listener = new EasyAlertDialogHelper.OnDialogActionListener() {
//
//            @Override
//            public void doCancelAction() {
//            }
//
//            @Override
//            public void doOkAction() {
//                removeMember();
//            }
//        };
//        final EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(this, null, getString(R.string.team_member_remove_confirm),
//                getString(R.string.remove), getString(R.string.cancel), true, listener);
//        dialog.show();
//
        if (removeMenuDialog == null) {
            List<String> btnNames = new ArrayList<>();
            btnNames.add(getString(R.string.team_member_remove_confirm));
            btnNames.add(getString(R.string.ok));
            btnNames.add(getString(R.string.cancel));
            removeMenuDialog = new MenuDialog(this, btnNames, (name -> {
                if (name.equals(getString(R.string.ok))) {
                    removeMember();
                }
                removeMenuDialog.dismiss();
            }));
        }
        removeMenuDialog.show();

    }

    /**
     * 移除群成员
     */
    private void removeMember() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).removeMember(teamId, account).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                UpdateMemberChangeService.start(AdvancedTeamMemberInfoActivity.this, account, teamId, 2);
                DialogMaker.dismissProgressDialog();
                makeIntent(account, isSetAdmin, true);
                finish();
                YchatToastUtils.showShort("移除成功");
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                if (code == ResponseCode.RES_TEAM_ENACCESS) {
                    YchatToastUtils.showShort("踢人失败");
                } else {
                    YchatToastUtils.showShort(String.format(getString(R.string.update_failed), code));
                }
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        makeIntent(account, isSetAdmin, false);
        super.onBackPressed();
    }

    /**
     * 设置返回的Intent
     *
     * @param account    帐号
     * @param isSetAdmin 是否设置为管理员
     * @param value      是否移除群成员
     */
    private void makeIntent(String account, boolean isSetAdmin, boolean value) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, account);
        intent.putExtra(EXTRA_ISADMIN, isSetAdmin);
        intent.putExtra(EXTRA_ISREMOVE, value);
        setResult(RESULT_OK, intent);
    }

    private boolean isSelf(String account) {
        return NimUIKit.getAccount().equals(account);
    }
}
