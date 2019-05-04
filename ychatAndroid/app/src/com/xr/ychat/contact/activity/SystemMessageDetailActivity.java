package com.xr.ychat.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ApplyLeaveTeam;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.R;
import com.xr.ychat.main.helper.MessageHelper;

public class SystemMessageDetailActivity extends SwipeBackUI {
    public static final String EXTRA_STATUS = "message_status";
    public static final String EXTRA_DATA = "message_data";
    private static final String EXTRA_SYSTEM = "system_message";
    private static final String EXTRA_NOTIFICATION = "custom_notification";
    // 基本信息
    private HeadImageView headImageView;
    private TextView nameText;
    private ImageView genderImage;
    private TextView accountText;
    private TextView nickText;
    private TextView messageContent;
    private LinearLayout passLayout;
    private LinearLayout rejectLayout;
    private String account;
    private SystemMessage message;
    private CustomNotification customNotification;
    private NimUserInfo userInfo;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_message_detail);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        localBroadcastManager = LocalBroadcastManager.getInstance(SystemMessageDetailActivity.this);
        findViews();
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SYSTEM)) {
            message = (SystemMessage) intent.getSerializableExtra(EXTRA_SYSTEM);
            messageContent.setText(MessageHelper.getVerifyNotificationText(message));
            account = message.getFromAccount();
        } else {
            customNotification = (CustomNotification) intent.getSerializableExtra(EXTRA_NOTIFICATION);
            Gson gson = new Gson();
            ApplyLeaveTeam applyLeaveTeam = gson.fromJson(customNotification.getContent(), new TypeToken<ApplyLeaveTeam>() {
            }.getType());
            messageContent.setText("申请退出 " + TeamHelper.getTeamName(applyLeaveTeam.getLeaveTeamID()));
            account = customNotification.getFromAccount();
        }
        updateUserInfo();
    }

    private void findViews() {
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
        nickText = findView(R.id.user_nick);
        messageContent = findView(R.id.system_message_content);
        passLayout = findView(R.id.add_buddy_layout);
        passLayout.setOnClickListener(v -> {
            if (message != null) {
                dealMessage(true);
            }
            if (customNotification != null) {
                dealNotification(true);
            }
        });
        rejectLayout = findView(R.id.remove_buddy_layout);
        rejectLayout.setOnClickListener(v -> {
            if (message != null) {
                dealMessage(false);
            }
            if (customNotification != null) {
                dealNotification(false);
            }
        });
    }

    private void dealMessage(boolean b) {
        Intent intent = new Intent("com.xr.ychat.DisposeSystemMessageReceiver");
        intent.putExtra(EXTRA_STATUS, b);
        intent.putExtra(EXTRA_DATA, message);
        localBroadcastManager.sendBroadcast(intent);
        UserProfileActivity.start(SystemMessageDetailActivity.this, message.getFromAccount());
        overridePendingTransition(0, 0);
        finish();
    }

    private void dealNotification(boolean b) {
        Intent intent = new Intent("com.xr.ychat.DisposeCustomNotificationReceiver");
        intent.putExtra(EXTRA_STATUS, b);
        intent.putExtra(EXTRA_DATA, customNotification);
        localBroadcastManager.sendBroadcast(intent);
        UserProfileActivity.start(SystemMessageDetailActivity.this, customNotification.getFromAccount());
        overridePendingTransition(0, 0);
        finish();
    }

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
        nameText.setText(userInfo.getName());
        accountText.setText(String.format("昵称: %1$s", userInfo.getName()));
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

    public static void start(Context context, SystemMessage message) {
        Intent intent = new Intent();
        intent.setClass(context, SystemMessageDetailActivity.class);
        intent.putExtra(EXTRA_SYSTEM, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void start(Context context, CustomNotification message) {
        Intent intent = new Intent();
        intent.setClass(context, SystemMessageDetailActivity.class);
        intent.putExtra(EXTRA_NOTIFICATION, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
