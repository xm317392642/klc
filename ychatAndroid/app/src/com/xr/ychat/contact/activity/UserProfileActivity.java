package com.xr.ychat.contact.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.contact.constant.UserConstant;
import com.xr.ychat.main.model.Extras;

import java.util.List;

/**
 * 用户资料页面
 * Created by huangjun on 2015/8/11.
 */
public class UserProfileActivity extends SwipeBackUI {

    private final boolean FLAG_ADD_FRIEND_DIRECTLY = false; // 是否直接加为好友开关，false为需要好友申请

    private String account;

    // 基本信息
    private HeadImageView headImageView;
    private TextView nameText;
    private ImageView genderImage;
    private TextView accountText;
    private TextView nickText;
    private TextView phoneText;
    private ConstraintLayout aliasLayout;
    private ConstraintLayout phoneLayout;

    // 开关
    private ConstraintLayout toggleLayout;
    private ConstraintLayout notificationLayout;
    private SwitchButton blackSwitch;
    private SwitchButton notificationSwitch;
    private LinearLayout addFriendLayout;
    private TextView addFriendBtn;
    private LinearLayout removeFriendLayout;
    private LinearLayout beginChatLayout;
    private TextView removeFriendBtn;
    private View chatBtnView;

    private NimUserInfo userInfo;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        if (TextUtils.isEmpty(account)) {
            finish();
            return;
        }
        initActionbar();
        findViews();
        registerObserver(true);
    }

    private void initActionbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        TextView toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setText(R.string.edit);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileSettingActivity.start(UserProfileActivity.this, account);
            }
        });
        if (!TextUtils.equals(account, DemoCache.getAccount())) {
            toolbarView.setVisibility(View.GONE);
        } else {
            toolbarView.setVisibility(View.GONE);
        }
    }

    private void findViews() {
        addFriendLayout = findView(R.id.add_buddy_layout);
        removeFriendLayout = findView(R.id.remove_buddy_layout);
        beginChatLayout = findView(R.id.begin_chat_layout);
        headImageView = findView(R.id.user_head_image);
        headImageView.setOnClickListener(v -> {
            if (userInfo != null) {
                String url = String.format("scheme://ychat/imagepreview?extra_image=%1$s", userInfo.getAvatar());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
        nameText = findView(R.id.user_name);
        genderImage = findView(R.id.gender_img);
        accountText = findView(R.id.user_account);
        toggleLayout = findView(R.id.toggle_layout);
        notificationLayout = findView(R.id.notification_layout);
        addFriendBtn = findView(R.id.add_buddy);
        chatBtnView = findView(R.id.begin_chat_view);
        removeFriendBtn = findView(R.id.remove_buddy);
        nickText = findView(R.id.user_nick);
        phoneText = findView(R.id.phone_value);
        aliasLayout = findView(R.id.alias);
        phoneLayout = findView(R.id.phone);
        aliasLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileEditItemActivity.startActivity(UserProfileActivity.this, UserConstant.KEY_ALIAS, account);
            }
        });
        addFriendBtn.setOnClickListener(onClickListener);
        chatBtnView.setOnClickListener(onClickListener);
        removeFriendBtn.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObserver(false);
    }

    ContactChangedObserver friendDataChangedObserver = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onDeletedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            updateUserOperatorView();
        }
    };

    private void registerObserver(boolean register) {
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register);
        NIMClient.getService(FriendServiceObserve.class).observeMuteListChangedNotify(muteListChangedNotifyObserver, register);
    }

    Observer<MuteListChangedNotify> muteListChangedNotifyObserver = new Observer<MuteListChangedNotify>() {
        @Override
        public void onEvent(MuteListChangedNotify notify) {
            notificationSwitch.setCheck(notify.isMute());
        }
    };

    private void updateUserInfo() {
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
        updateUserOperatorView();
        headImageView.loadAvatar(userInfo.getAvatar());
        if (userInfo.getGenderEnum() == GenderEnum.MALE) {
            genderImage.setVisibility(View.VISIBLE);
            genderImage.setBackgroundResource(R.drawable.nim_male);
        } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
            genderImage.setVisibility(View.VISIBLE);
            genderImage.setBackgroundResource(R.drawable.nim_female);
        } else {
            genderImage.setVisibility(View.GONE);
        }
    }

    /**
     * 设置空了吹号
     */
    private void setYChatNo() {
        ContactHttpClient.getInstance().getYchatAccount(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                nickText.setText(String.format("空了吹号: %1$s", aVoid.getYchatNo()));
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                nickText.setText(String.format("空了吹号: %1$s", userInfo.getAccount()));
            }
        });

    }

    private void updateUserOperatorView() {
        if (DemoCache.getAccount() != null && !DemoCache.getAccount().equals(account)) {
            if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
                removeFriendLayout.setVisibility(View.VISIBLE);
                addFriendLayout.setVisibility(View.GONE);
                beginChatLayout.setVisibility(View.VISIBLE);
                updateAlias(true);
            } else {
                addFriendLayout.setVisibility(View.VISIBLE);
                removeFriendLayout.setVisibility(View.GONE);
                beginChatLayout.setVisibility(View.GONE);
                updateAlias(false);
            }
            phoneLayout.setVisibility(View.GONE);
        } else {
            beginChatLayout.setVisibility(View.GONE);
            addFriendLayout.setVisibility(View.GONE);
            removeFriendLayout.setVisibility(View.GONE);
            aliasLayout.setVisibility(View.GONE);
            nameText.setText(userInfo.getName());
            accountText.setText(String.format("昵称: %1$s", userInfo.getName()));
            nickText.setVisibility(View.VISIBLE);

            setYChatNo();
            phoneLayout.setVisibility(View.VISIBLE);
            phoneText.setText(userInfo.getMobile());
            toggleLayout.setVisibility(View.GONE);
            notificationLayout.setVisibility(View.GONE);
        }
    }

    private void updateAlias(boolean isFriend) {
        if (isFriend) {
            aliasLayout.setVisibility(View.VISIBLE);
            String alias = NimUIKit.getContactProvider().getAlias(account);
            if (!TextUtils.isEmpty(alias)) {
                nameText.setText(alias);
                accountText.setText(String.format("昵称: %1$s", userInfo.getName()));
                nickText.setVisibility(View.VISIBLE);
                setYChatNo();
            } else {
                nameText.setText(userInfo.getName());
                accountText.setText(String.format("昵称: %1$s", userInfo.getName()));
                nickText.setVisibility(View.VISIBLE);
                setYChatNo();
            }
            toggleLayout.setVisibility(View.VISIBLE);
            blackSwitch = toggleLayout.findViewById(R.id.user_profile_toggle);
            blackSwitch.setTag(1);
            boolean black = NIMClient.getService(FriendService.class).isInBlackList(account);
            blackSwitch.setCheck(black);
            blackSwitch.setOnChangedListener(onChangedListener);
            notificationLayout.setVisibility(View.VISIBLE);
            notificationSwitch = notificationLayout.findViewById(R.id.user_notification_toggle);
            notificationSwitch.setTag(2);
            boolean notice = NIMClient.getService(FriendService.class).isNeedMessageNotify(account);
            notificationSwitch.setCheck(!notice);
            notificationSwitch.setOnChangedListener(onChangedListener);
        } else {
            aliasLayout.setVisibility(View.GONE);
            nameText.setText(userInfo.getName());
            accountText.setText(String.format("昵称: %1$s", userInfo.getName()));
            nickText.setVisibility(View.VISIBLE);
            ContactHttpClient.getInstance().getYchatAccount(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo aVoid) {
                    nickText.setText(String.format("空了吹号: %1$s", aVoid.getYchatNo()));
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    nickText.setText(String.format("空了吹号: %1$s", userInfo.getAccount()));
                }
            });
            toggleLayout.setVisibility(View.GONE);
            notificationLayout.setVisibility(View.GONE);
        }
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            Integer tag = (Integer) v.getTag();
            if (tag == 1) {
                if (!NetworkUtil.isNetAvailable(UserProfileActivity.this)) {
                    YchatToastUtils.showShort(R.string.network_is_not_available);
                    blackSwitch.setCheck(!checkState);
                    return;
                }
                if (checkState) {
                    NIMClient.getService(FriendService.class).addToBlackList(account).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            YchatToastUtils.showShort("拉黑成功");
                        }

                        @Override
                        public void onFailed(int code) {
                            blackSwitch.setCheck(!checkState);
                            if (code == 408) {
                                YchatToastUtils.showShort(R.string.network_is_not_available);
                            } else {
                                YchatToastUtils.showShort("on failed：" + code);
                            }
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                } else {
                    NIMClient.getService(FriendService.class).removeFromBlackList(account).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            YchatToastUtils.showShort("移除黑名单成功");
                        }

                        @Override
                        public void onFailed(int code) {
                            blackSwitch.setCheck(!checkState);
                            if (code == 408) {
                                YchatToastUtils.showShort(R.string.network_is_not_available);
                            } else {
                                YchatToastUtils.showShort("on failed：" + code);
                            }
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                }
            } else {
                if (!NetworkUtil.isNetAvailable(UserProfileActivity.this)) {
                    YchatToastUtils.showShort(R.string.network_is_not_available);
                    notificationSwitch.setCheck(!checkState);
                    return;
                }
                NIMClient.getService(FriendService.class).setMessageNotify(account, !checkState).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        if (checkState) {
                            YchatToastUtils.showShort("已开启消息免打扰");
                        } else {
                            YchatToastUtils.showShort("已关闭消息免打扰");
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        notificationSwitch.setCheck(!checkState);
                        if (code == 408) {
                            YchatToastUtils.showShort(R.string.network_is_not_available);
                        } else {
                            YchatToastUtils.showShort("on failed：" + code);
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == addFriendBtn) {
                if (FLAG_ADD_FRIEND_DIRECTLY) {
                    doAddFriend(null, true);  // 直接加为好友
                } else {
                    onAddFriendByVerify(); // 发起好友验证请求
                }
            } else if (v == removeFriendBtn) {
                onRemoveFriend();
            } else if (v == chatBtnView) {
                onChat();
            }
        }
    };

    /**
     * 通过验证方式添加好友
     */
    private void onAddFriendByVerify() {
        final EasyEditDialog requestDialog = new EasyEditDialog(this);
        requestDialog.setTitle(getString(R.string.add_friend_verify_tip));
        requestDialog.setEditText("我是" + UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()) + "，你好");
        requestDialog.addNegativeButtonListener(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
            }
        });
        requestDialog.addPositiveButtonListener(R.string.send, com.netease.nim.uikit.R.color.color_activity_blue_bg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
                String msg = requestDialog.getEditMessage();
                if (TextUtils.isEmpty(msg)) {
                    msg = String.format("我是%1$s", UserInfoHelper.getUserName(DemoCache.getAccount()));
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
    }

    private void doAddFriend(String msg, boolean addDirectly) {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        if (!TextUtils.isEmpty(account) && account.equals(DemoCache.getAccount())) {
            YchatToastUtils.showShort("不能加自己为好友");
            return;
        }
        final VerifyType verifyType = addDirectly ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
        DialogMaker.showProgressDialog(this, "", true);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(account, verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        DialogMaker.dismissProgressDialog();
                        updateUserOperatorView();
                        if (VerifyType.DIRECT_ADD == verifyType) {
                            YchatToastUtils.showShort("添加好友成功");
                            IMMessage msg = MessageBuilder.createTipMessage(account, SessionTypeEnum.P2P);
                            msg.setContent("我们已经是好友，现在可以开始聊天了");
                            CustomMessageConfig config = new CustomMessageConfig();
                            config.enablePush = false; // 不推送
                            config.enableUnreadCount = false;
                            msg.setConfig(config);
                            msg.setStatus(MsgStatusEnum.success);
                            NIMClient.getService(MsgService.class).sendMessage(msg, false);
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
                            YchatToastUtils.showShort("on failed：" + code);
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                });
    }

    private void onRemoveFriend() {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(this, "删除好友",
                getString(R.string.remove_friend_tip), true,
                new EasyAlertDialogHelper.OnDialogActionListener() {

                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        DialogMaker.showProgressDialog(UserProfileActivity.this, "", true);
                        NIMClient.getService(FriendService.class).deleteFriend(account).setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                DialogMaker.dismissProgressDialog();
                                YchatToastUtils.showShort(R.string.remove_friend_success);
                                finish();
                            }

                            @Override
                            public void onFailed(int code) {
                                DialogMaker.dismissProgressDialog();
                                if (code == 408) {
                                    YchatToastUtils.showShort(R.string.network_is_not_available);
                                } else {
                                    YchatToastUtils.showShort("on failed：" + code);
                                }
                            }

                            @Override
                            public void onException(Throwable exception) {
                                DialogMaker.dismissProgressDialog();
                            }
                        });
                    }
                });
        if (!isFinishing() && !isDestroyedCompatible()) {
            dialog.show();
        }
    }

    private void onChat() {
        NimUIKit.startP2PSession(this, account);
    }
}
