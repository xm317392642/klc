package com.xr.ychat.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.business.session.helper.SendImageHelper;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.media.picker.activity.PreviewImageFromCameraActivity;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.xr.ychat.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatBackgroundActivity extends SwipeBackUI {
    public static final String MIME_JPEG = "image/jpeg";
    public static final String JPG = ".jpg";
    private FrameLayout oneLayout;
    private ConstraintLayout typefaceLayout;
    private ImageView oneCheck;
    private FrameLayout twoLayout;
    private ImageView twoCheck;
    private FrameLayout threeLayout;
    private ImageView threeCheck;
    private FrameLayout fourLayout;
    private ImageView fourCheck;
    private FrameLayout fiveLayout;
    private ImageView fiveCheck;
    private FrameLayout sixLayout;
    private ImageView sixCheck;
    private MenuDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_session_background);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        typefaceLayout = (ConstraintLayout) findViewById(R.id.typeface_layout);
        typefaceLayout.setOnClickListener(v -> {
            showSelector(RequestCode.PICK_IMAGE);
        });
        oneLayout = (FrameLayout) findViewById(R.id.session_background_one);
        oneCheck = (ImageView) findViewById(R.id.session_background_one_check);
        oneLayout.setOnClickListener(v -> {
            deleteBackground();
            setCheck(1);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND, 1);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, getBackgroundUrl(1));
        });
        twoLayout = (FrameLayout) findViewById(R.id.session_background_two);
        twoCheck = (ImageView) findViewById(R.id.session_background_two_check);
        twoLayout.setOnClickListener(v -> {
            deleteBackground();
            setCheck(2);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND, 2);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, getBackgroundUrl(2));
        });
        threeLayout = (FrameLayout) findViewById(R.id.session_background_three);
        threeCheck = (ImageView) findViewById(R.id.session_background_three_check);
        threeLayout.setOnClickListener(v -> {
            deleteBackground();
            setCheck(3);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND, 3);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, getBackgroundUrl(3));
        });
        fourLayout = (FrameLayout) findViewById(R.id.session_background_four);
        fourCheck = (ImageView) findViewById(R.id.session_background_four_check);
        fourLayout.setOnClickListener(v -> {
            deleteBackground();
            setCheck(4);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND, 4);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, getBackgroundUrl(4));
        });
        fiveLayout = (FrameLayout) findViewById(R.id.session_background_five);
        fiveCheck = (ImageView) findViewById(R.id.session_background_five_check);
        fiveLayout.setOnClickListener(v -> {
            deleteBackground();
            setCheck(5);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND, 5);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, getBackgroundUrl(5));
        });
        sixLayout = (FrameLayout) findViewById(R.id.session_background_six);
        sixCheck = (ImageView) findViewById(R.id.session_background_six_check);
        sixLayout.setOnClickListener(v -> {
            deleteBackground();
            setCheck(6);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND, 6);
            SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, getBackgroundUrl(6));
        });
        setCheck(SPUtils.getInstance().getInt(CommonUtil.BACKGROUND, 1));
    }

    private String getBackgroundUrl(int state) {
        return "android.resource://com.xr.ychat/drawable/" + CommonUtil.strings[state - 1];
    }

    private void setCheck(int state) {
        oneCheck.setVisibility(state == 1 ? View.VISIBLE : View.GONE);
        twoCheck.setVisibility(state == 2 ? View.VISIBLE : View.GONE);
        threeCheck.setVisibility(state == 3 ? View.VISIBLE : View.GONE);
        fourCheck.setVisibility(state == 4 ? View.VISIBLE : View.GONE);
        fiveCheck.setVisibility(state == 5 ? View.VISIBLE : View.GONE);
        sixCheck.setVisibility(state == 6 ? View.VISIBLE : View.GONE);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ChatBackgroundActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * 打开图片选择器
     */
    private void showSelector(final int requestCode) {
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = R.string.set_head_image;
        option.crop = false;
        option.multiSelect = false;
        List<String> btnNames = new ArrayList<>();
        btnNames.add("拍照");
        btnNames.add("从手机相册选择");
        btnNames.add("取消");
        dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
            @Override
            public void onButtonClick(String name) {
                if (name.equals("拍照")) {
                    int from = PickImageActivity.FROM_CAMERA;
                    if (!option.crop) {
                        PickImageActivity.start(ChatBackgroundActivity.this, requestCode, from, option.outputPath, option.multiSelect, 1,
                                true, false, 0, 0);
                    } else {
                        PickImageActivity.start(ChatBackgroundActivity.this, requestCode, from, option.outputPath, false, 1,
                                false, true, option.cropOutputImageWidth, option.cropOutputImageHeight);
                    }
                } else if (name.equals("从手机相册选择")) {
                    int from = PickImageActivity.FROM_LOCAL;
                    if (!option.crop) {
                        PickImageActivity.start(ChatBackgroundActivity.this, requestCode, from, option.outputPath, option.multiSelect,
                                option.multiSelectMaxCount, true, false, 0, 0);
                    } else {
                        PickImageActivity.start(ChatBackgroundActivity.this, requestCode, from, option.outputPath, false, 1,
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
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RequestCode.PICK_IMAGE:
                    onPickImageActivityResult(data);
                    break;
                case RequestCode.PREVIEW_IMAGE_FROM_CAMERA:
                    onPreviewImageActivityResult(requestCode, data);
                    break;
            }
        }
    }

    /**
     * 图片选取回调
     */
    private void onPickImageActivityResult(Intent data) {
        boolean local = data.getBooleanExtra(Extras.EXTRA_FROM_LOCAL, false);
        if (local) {
            SendImageHelper.sendImageAfterSelfImagePicker(ChatBackgroundActivity.this, data, new SendImageHelper.Callback() {
                @Override
                public void sendImage(File file, boolean isOrig) {
                    SPUtils.getInstance().put(CommonUtil.BACKGROUND, 0);
                    SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, "file://" + file.getAbsolutePath());
                    setCheck(0);
                }
            });
        } else {
            // 拍照
            Intent intent = new Intent();
            if (!handleImagePath(intent, data)) {
                return;
            }
            intent.setClass(ChatBackgroundActivity.this, PreviewImageFromCameraActivity.class);
            startActivityForResult(intent, RequestCode.PREVIEW_IMAGE_FROM_CAMERA);
        }
    }

    /**
     * 是否可以获取图片
     */
    private boolean handleImagePath(Intent intent, Intent data) {
        String photoPath = data.getStringExtra(Extras.EXTRA_FILE_PATH);
        if (TextUtils.isEmpty(photoPath)) {
            return false;
        }
        File imageFile = new File(photoPath);
        intent.putExtra("OrigImageFilePath", photoPath);
        File scaledImageFile = ImageUtil.getScaledImageFileWithMD5(imageFile, MIME_JPEG);
        boolean local = data.getExtras().getBoolean(Extras.EXTRA_FROM_LOCAL, true);
        if (!local) {
            // 删除拍照生成的临时文件
            AttachmentStore.delete(photoPath);
        }
        if (scaledImageFile == null) {
            return false;
        } else {
            ImageUtil.makeThumbnail(scaledImageFile);
        }
        intent.putExtra("ImageFilePath", scaledImageFile.getAbsolutePath());
        return true;
    }

    private void onPreviewImageActivityResult(int requestCode, Intent data) {
        if (data.getBooleanExtra(PreviewImageFromCameraActivity.RESULT_SEND, false)) {
            sendImageAfterPreviewPhotoActivityResult(data);
        } else if (data.getBooleanExtra(PreviewImageFromCameraActivity.RESULT_RETAKE, false)) {
            String filename = StringUtil.get32UUID() + JPG;
            String path = StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);
            if (requestCode == RequestCode.PREVIEW_IMAGE_FROM_CAMERA) {
                PickImageActivity.start(ChatBackgroundActivity.this, RequestCode.PICK_IMAGE, PickImageActivity.FROM_CAMERA, path);
            }
        }
    }

    private void sendImageAfterPreviewPhotoActivityResult(Intent data) {
        SendImageHelper.sendImageAfterPreviewPhotoActivityResult(data, new SendImageHelper.Callback() {
            @Override
            public void sendImage(File file, boolean isOrig) {
                SPUtils.getInstance().put(CommonUtil.BACKGROUND, 0);
                SPUtils.getInstance().put(CommonUtil.BACKGROUND_URL, "file://" + file.getAbsolutePath());
                setCheck(0);
            }
        });
    }

    private void deleteBackground() {
        int type = SPUtils.getInstance().getInt(CommonUtil.BACKGROUND, 1);
        if (type == 0) {
            String path = SPUtils.getInstance().getString(CommonUtil.BACKGROUND_URL);
            String head = "file://";
            String newpath = path.substring(head.length());
            File file = new File(newpath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
