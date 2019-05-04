package com.xr.ychat.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ImageUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.AspectRatioImageView;
import com.netease.nim.uikit.common.util.QrCodeUtils;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCodeActivity extends SwipeBackUI {
    public static final String USER_FORMAT = "http://share.yaoliaoim.com?accid=";
    public static final String GROUP_FORMAT = "http://share.yaoliaoim.com?groupid=";

    public static String ROOTPAHT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ychat" + File.separator;
    private TextView toolbarTitle;
    private TextView slogan;
    private ImageView moreAction;
    private HeadImageView userAvatar;
    private TextView userName;
    private AspectRatioImageView codeImage;
    private ConstraintLayout constraintLayout;
    private String userAccount;
    private MenuDialog dialog;
    private NimUserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_qrcode);
        userAccount = DemoCache.getAccount();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("我的二维码");
        mToolbar.setNavigationOnClickListener(v -> finish());
        moreAction = (ImageView) findViewById(R.id.toolbar_more);
        moreAction.setOnClickListener(v -> {
            showRegularTeamMenu();
        });
        userAvatar = (HeadImageView) findViewById(R.id.user_card_avatar);
        userName = (TextView) findViewById(R.id.user_card_name);
        codeImage = (AspectRatioImageView) findViewById(R.id.group_card_image);
        constraintLayout = (ConstraintLayout) findViewById(R.id.group_card_info);
        slogan = (TextView) findViewById(com.netease.nim.uikit.R.id.group_card_slogan);
        slogan.setText("扫一扫上面的二维码图案，加我空了吹");
        getUserInfo();
    }

    private void getUserInfo() {
        userAvatar.loadBuddyAvatar(userAccount);
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(userAccount);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(userAccount, new SimpleCallback<NimUserInfo>() {

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
        userName.setText(userInfo.getName());
        ContactHttpClient.getInstance().getYchatAccount(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), userAccount, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                codeImage.post(new Runnable() {
                    @Override
                    public void run() {
                        int width = codeImage.getWidth();
                        int height = codeImage.getHeight();
                        if (width > 0 && height > 0) {
                            String name = USER_FORMAT + aVoid.getYchatNo();
                            Bitmap bitmap = QrCodeUtils.createQRCodeBitmap(name, width, height);
                            codeImage.setImageBitmap(bitmap);
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                codeImage.post(new Runnable() {
                    @Override
                    public void run() {
                        int width = codeImage.getWidth();
                        int height = codeImage.getHeight();
                        if (width > 0 && height > 0) {
                            String name = USER_FORMAT + userInfo.getAccount();
                            Bitmap bitmap = QrCodeUtils.createQRCodeBitmap(name, width, height);
                            codeImage.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MyCodeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * ******************************* Event *********************************
     */
    /**
     * 显示菜单
     */
    private void showRegularTeamMenu() {
        if (dialog == null) {
            List<String> btnNames = new ArrayList<>();
            btnNames.add("保存图片");
            btnNames.add("分享");
            btnNames.add("取消");
            dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    if (name.equals(btnNames.get(0))) {
                        saveCard();
                    } else if (name.equals(btnNames.get(1))) {
                        Bitmap bitmap = Bitmap.createBitmap(constraintLayout.getWidth(), constraintLayout.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        constraintLayout.draw(canvas);
                        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("image/*");//设置分享内容的类型
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent = Intent.createChooser(intent, "分享到");
                        startActivity(intent);
                    }
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    private void saveCard() {
        Bitmap bitmap = Bitmap.createBitmap(constraintLayout.getWidth(), constraintLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        constraintLayout.draw(canvas);
        File file = new File(ROOTPAHT, "user_info.jpg");
        ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        YchatToastUtils.showShort("图片保存成功");
    }

}
