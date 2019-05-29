package com.xr.ychat.contact.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.R;
import com.xr.ychat.contact.constant.UserConstant;
import com.xr.ychat.contact.helper.UserUpdateHelper;
import com.xr.ychat.login.MyCodeActivity;
import com.xr.ychat.main.activity.SettingYchatActivity;
import com.xr.ychat.main.model.Extras;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2015/9/14.
 */
public class UserProfileSettingActivity extends SwipeBackUI implements View.OnClickListener {
    private final String TAG = UserProfileSettingActivity.class.getSimpleName();

    // constant
    private static final int PICK_AVATAR_REQUEST = 0x0E;
    private static final int AVATAR_TIME_OUT = 30000;

    private String account;

    // view
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private HeadImageView userHead;
    private ConstraintLayout nickLayout;
    private ConstraintLayout genderLayout;
    private ConstraintLayout authenticationLayout;
    private ConstraintLayout codeLayout;
    private ConstraintLayout phoneLayout;
    private ConstraintLayout accountLayout;

    private TextView nickText;
    private TextView genderText;
    private TextView accountText;
    private TextView phoneText;

    // data
    AbortableFuture<String> uploadAvatarFuture;
    private NimUserInfo userInfo;
    private String uid;
    private String myToken;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileSettingActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.user_profile_set_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText(R.string.user_information);
        mToolbar.setNavigationOnClickListener(v -> finish());

        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        uid = Preferences.getWeiranUid(this);
        myToken = Preferences.getWeiranToken(this);
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
    }

    private void findViews() {
        userHead = findView(R.id.user_head);
        userHead.setOnClickListener(this::onClick);
        nickLayout = findView(R.id.nick_layout);
        genderLayout = findView(R.id.gender_layout);
        authenticationLayout = findView(R.id.authentication_layout);
        codeLayout = findView(R.id.code_layout);
        accountLayout = findView(R.id.account_layout);
        phoneLayout = findView(R.id.phone_layout);

        ((TextView) nickLayout.findViewById(R.id.nick_attribute)).setText(R.string.nickname);
        ((TextView) accountLayout.findViewById(R.id.account_attribute)).setText(R.string.account);
        ((TextView) phoneLayout.findViewById(R.id.phone_attribute)).setText(R.string.phone);

        nickText = (TextView) nickLayout.findViewById(R.id.nick_value);
        genderText = (TextView) genderLayout.findViewById(R.id.gender_value);
        accountText = (TextView) accountLayout.findViewById(R.id.account_value);
        phoneText = (TextView) phoneLayout.findViewById(R.id.phone_value);

        authenticationLayout.setOnClickListener(this);
        codeLayout.setOnClickListener(this);
        accountLayout.setOnClickListener(this);
    }

    private void getUserInfo() {
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        userInfo = result;
                        updateUI();
                    } else {
                        YchatToastUtils.showShort("getUserInfoFromRemote failed:" + code);
                    }
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        if (TextUtils.isEmpty(userInfo.getMobile())) {
            phoneLayout.setVisibility(View.GONE);
        }
        userHead.loadAvatar(userInfo.getAvatar());
        nickText.setText(userInfo.getName());
        phoneText.setText(userInfo.getMobile());
        if (userInfo.getGenderEnum() != null) {
            if (userInfo.getGenderEnum() == GenderEnum.MALE) {
                genderText.setText("男");
            } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
                genderText.setText("女");
            }
        }
        String ychatNo = SPUtils.getInstance().getString(CommonUtil.YCHAT_ACCOUNT);
        if (TextUtils.isEmpty(ychatNo)) {
            ContactHttpClient.getInstance().getYchatAccount(uid, myToken, account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo aVoid) {
                    SPUtils.getInstance().put(CommonUtil.YCHAT_ACCOUNT, aVoid.getYchatNo());
                    accountText.setText(aVoid.getYchatNo());
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    accountText.setText("");
                }
            });
        } else {
            accountText.setText(ychatNo);
        }
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
    private void showSelector(final int requestCode) {
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = R.string.set_head_image;
        option.crop = true;
        option.multiSelect = false;
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
                        PickImageActivity.start(UserProfileSettingActivity.this, requestCode, from, option.outputPath, option.multiSelect, 1,
                                true, false, 0, 0);
                    } else {
                        PickImageActivity.start(UserProfileSettingActivity.this, requestCode, from, option.outputPath, false, 1,
                                false, true, option.cropOutputImageWidth, option.cropOutputImageHeight);
                    }
                } else if (name.equals("从手机相册选择")) {
                    int from = PickImageActivity.FROM_LOCAL;
                    if (!option.crop) {
                        PickImageActivity.start(UserProfileSettingActivity.this, requestCode, from, option.outputPath, option.multiSelect,
                                option.multiSelectMaxCount, true, false, 0, 0);
                    } else {
                        PickImageActivity.start(UserProfileSettingActivity.this, requestCode, from, option.outputPath, false, 1,
                                false, true, option.cropOutputImageWidth, option.cropOutputImageHeight);
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_layout:

                //PickImageHelper.pickImage(UserProfileSettingActivity.this, PICK_AVATAR_REQUEST, option);
                showSelector(PICK_AVATAR_REQUEST);
                break;
            case R.id.nick_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_NICKNAME,
                        nickText.getText().toString());
                break;
            case R.id.gender_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_GENDER,
                        String.valueOf(userInfo.getGenderEnum().getValue()));
                break;
            case R.id.authentication_layout: {

            }
            break;
            case R.id.user_head: {
                if (userInfo != null) {
                    String url = String.format("scheme://ychat/imagepreview?extra_image=%1$s", userInfo.getAvatar());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
            break;
            case R.id.code_layout:
                MyCodeActivity.start(this);
                break;
            case R.id.account_layout:
                SettingYchatActivity.start(this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_AVATAR_REQUEST) {
            String path = data.getStringExtra(com.netease.nim.uikit.business.session.constant.Extras.EXTRA_FILE_PATH);
            updateAvatar(path);
        }
    }

    /**
     * 更新头像
     */
    private void updateAvatar(final String path) {
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
                cancelUpload(R.string.user_info_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);

        LogUtil.i(TAG, "start upload avatar, local file path=" + file.getAbsolutePath());
        new Handler().postDelayed(outimeTask, AVATAR_TIME_OUT);
        uploadAvatarFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadAvatarFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    LogUtil.i(TAG, "upload avatar success, url =" + url);

                    UserUpdateHelper.update(UserInfoFieldEnum.AVATAR, url, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                onUpdateDone();
                                ContactHttpClient.getInstance().updateUserInfo(uid, myToken, 1, url, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                                    @Override
                                    public void onSuccess(RequestInfo aVoid) {
                                        YchatToastUtils.showShort(R.string.head_update_success);
                                    }

                                    @Override
                                    public void onFailed(int code, String errorMsg) {
                                        YchatToastUtils.showShort(R.string.head_update_success);
                                    }
                                });
                            } else {
                                YchatToastUtils.showShort(R.string.head_update_failed);
                            }
                        }
                    }); // 更新资料
                } else {
                    YchatToastUtils.showShort(R.string.user_info_update_failed);
                    onUpdateDone();
                }
            }
        });
    }

    private void cancelUpload(int resId) {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture.abort();
            YchatToastUtils.showShort(resId);
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(R.string.user_info_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadAvatarFuture = null;
        DialogMaker.dismissProgressDialog();
        getUserInfo();
    }
}
