package com.xr.ychat.redpacket;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ImageUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.helper.SendImageHelper;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.widget.AspectRatioImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.R;
import com.xr.ychat.contact.helper.UserUpdateHelper;

import java.io.File;

import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PaymentCodeActivity extends SwipeBackUI implements View.OnClickListener {
    private static final int PICK_AVATAR_REQUEST = 0x0E;
    private static final int AVATAR_TIME_OUT = 30000;
    private Toolbar mToolbar;
    private TextView toolbarAction;
    private ConstraintLayout noPaymentCodeLayout;
    private Button noPaymentCodeAppend;
    private ConstraintLayout bindPaymentCodeLayout;
    private AspectRatioImageView bindPaymentCodeHeight;
    private AspectRatioImageView bindPaymentCodeWidth;
    private Button bindPaymentCodeReplace;
    private Button bindPaymentCodeSend;
    private NimUserInfo userInfo;
    private AbortableFuture<String> uploadAvatarFuture;
    private EasyAlertDialog alertDialog;
    private String userAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_payment_code);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarAction = (TextView) findViewById(R.id.action_bar_right_clickable_textview);
        toolbarAction.setOnClickListener(this::onClick);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        findViews();
        getUserInfo();
    }

    private void findViews() {
        noPaymentCodeLayout = (ConstraintLayout) findViewById(R.id.no_payment_code_layout);
        noPaymentCodeAppend = (Button) findViewById(R.id.no_payment_code_append);
        noPaymentCodeAppend.setOnClickListener(this::onClick);
        bindPaymentCodeLayout = (ConstraintLayout) findViewById(R.id.binding_payment_code_layout);
        bindPaymentCodeHeight = (AspectRatioImageView) findViewById(R.id.payment_code_height);
        bindPaymentCodeWidth = (AspectRatioImageView) findViewById(R.id.payment_code_width);
        bindPaymentCodeReplace = (Button) findViewById(R.id.payment_code_replace);
        bindPaymentCodeReplace.setOnClickListener(this::onClick);
        bindPaymentCodeSend = (Button) findViewById(R.id.payment_code_send);
        bindPaymentCodeSend.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_right_clickable_textview: {
                if (alertDialog == null) {
                    alertDialog = new EasyAlertDialog(PaymentCodeActivity.this);
                    alertDialog.setMessage("确定删除支付宝收款码?");
                    alertDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                            view -> {
                                alertDialog.dismiss();
                            }
                    );
                    alertDialog.addPositiveButton("确定", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                            view -> {
                                alertDialog.dismiss();
                                UserUpdateHelper.update(UserInfoFieldEnum.SIGNATURE, "", new RequestCallbackWrapper<Void>() {
                                    @Override
                                    public void onResult(int code, Void result, Throwable exception) {
                                        if (code == ResponseCode.RES_SUCCESS) {
                                            bindPaymentCodeLayout.setVisibility(View.GONE);
                                            toolbarAction.setVisibility(View.GONE);
                                            noPaymentCodeLayout.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            });
                }
                alertDialog.show();
            }
            break;
            case R.id.payment_code_replace:
            case R.id.no_payment_code_append: {
                int from = PickImageActivity.FROM_LOCAL;
                PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
                option.titleResId = R.string.set_head_image;
                option.crop = false;
                option.multiSelect = false;
                PickImageActivity.start(PaymentCodeActivity.this, PICK_AVATAR_REQUEST, from, option.outputPath, option.multiSelect,
                        option.multiSelectMaxCount, true, false, 0, 0);
            }
            break;
            case R.id.payment_code_send: {
                File file = new File(StorageUtil.getSystemImagePath() + "UserPaymentCode.jpg");
                file.deleteOnExit();
                Glide.with(PaymentCodeActivity.this)
                        .asBitmap()
                        .load(userAvatar)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                ImageUtils.save(resource, file, Bitmap.CompressFormat.PNG);
                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri contentUri = Uri.fromFile(file);
                                mediaScanIntent.setData(contentUri);
                                sendBroadcast(mediaScanIntent);
                                Intent intent = new Intent();
                                intent.putExtra(Extras.EXTRA_FILE_PATH, contentUri);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        });
            }
            break;
        }
    }


    private void getUserInfo() {
        String userAccount = NimUIKit.getAccount();
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
        userAvatar = userInfo.getSignature();
        boolean isHttpUrl = !TextUtils.isEmpty(userAvatar) && URLUtil.isNetworkUrl(userAvatar);
        bindPaymentCodeLayout.setVisibility(isHttpUrl ? View.VISIBLE : View.GONE);
        toolbarAction.setVisibility(isHttpUrl ? View.VISIBLE : View.GONE);
        noPaymentCodeLayout.setVisibility(!isHttpUrl ? View.VISIBLE : View.GONE);
        if (isHttpUrl) {
            Glide.with(PaymentCodeActivity.this)
                    .asBitmap()
                    .load(userAvatar)
                    .apply(new RequestOptions().error(com.netease.nim.uikit.R.drawable.nim_default_img_failed).diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            int height = resource.getHeight();
                            int width = resource.getWidth();
                            if (height > width) {
                                bindPaymentCodeHeight.setVisibility(View.VISIBLE);
                                bindPaymentCodeWidth.setVisibility(View.GONE);
                                bindPaymentCodeHeight.setAspectRatio(height / width);
                                bindPaymentCodeHeight.setImageBitmap(resource);
                            } else {
                                bindPaymentCodeHeight.setVisibility(View.GONE);
                                bindPaymentCodeWidth.setVisibility(View.VISIBLE);
                                bindPaymentCodeWidth.setAspectRatio(width / height);
                                bindPaymentCodeWidth.setImageBitmap(resource);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_AVATAR_REQUEST) {
            onPickImageActivityResult(data);
        }
    }

    /**
     * 图片选取回调
     */
    private void onPickImageActivityResult(Intent data) {
        boolean local = data.getBooleanExtra(Extras.EXTRA_FROM_LOCAL, false);
        if (local) {
            SendImageHelper.sendImageAfterSelfImagePicker(PaymentCodeActivity.this, data, new SendImageHelper.Callback() {
                @Override
                public void sendImage(File file, boolean isOrig) {
                    Observable.create((ObservableEmitter<String> emitter) -> {
                        Bitmap bitmap = ImageUtils.getBitmap(file);
                        String result = QRCodeDecoder.syncDecodeQRCode(bitmap);
                        emitter.onNext(TextUtils.isEmpty(result) ? "" : result);
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                @Override
                                public void accept(String result) throws Exception {
                                    if (!TextUtils.isEmpty(result) && result.toLowerCase().contains("qr.alipay.com")) {
                                        updateAvatar(file);
                                    } else {
                                        YchatToastUtils.showShort("请选择支付宝收款码图片哦");
                                    }
                                }
                            });
                }
            });
        }
    }

    private void updateAvatar(File file) {
        bindPaymentCodeWidth.setImageBitmap(null);
        bindPaymentCodeHeight.setImageBitmap(null);
        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload();
            }
        }).setCanceledOnTouchOutside(false);
        new Handler().postDelayed(outimeTask, AVATAR_TIME_OUT);
        uploadAvatarFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadAvatarFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    UserUpdateHelper.update(UserInfoFieldEnum.SIGNATURE, url, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            uploadAvatarFuture = null;
                            DialogMaker.dismissProgressDialog();
                            if (code == ResponseCode.RES_SUCCESS) {
                                userAvatar = url;
                                bindPaymentCodeLayout.setVisibility(View.VISIBLE);
                                toolbarAction.setVisibility(View.VISIBLE);
                                noPaymentCodeLayout.setVisibility(View.GONE);
                                Glide.with(PaymentCodeActivity.this)
                                        .asBitmap()
                                        .load(userAvatar)
                                        .apply(new RequestOptions().error(com.netease.nim.uikit.R.drawable.nim_default_img_failed).diskCacheStrategy(DiskCacheStrategy.NONE))
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                                int height = resource.getHeight();
                                                int width = resource.getWidth();
                                                if (height > width) {
                                                    bindPaymentCodeHeight.setVisibility(View.VISIBLE);
                                                    bindPaymentCodeWidth.setVisibility(View.GONE);
                                                    bindPaymentCodeHeight.setAspectRatio(height / width);
                                                    bindPaymentCodeHeight.setImageBitmap(resource);
                                                } else {
                                                    bindPaymentCodeHeight.setVisibility(View.GONE);
                                                    bindPaymentCodeWidth.setVisibility(View.VISIBLE);
                                                    bindPaymentCodeWidth.setAspectRatio(width / height);
                                                    bindPaymentCodeWidth.setImageBitmap(resource);
                                                }
                                            }
                                        });
                            }
                        }
                    });
                } else {
                    uploadAvatarFuture = null;
                    DialogMaker.dismissProgressDialog();
                }
            }
        });
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload();
        }
    };

    private void cancelUpload() {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture.abort();
            uploadAvatarFuture = null;
            DialogMaker.dismissProgressDialog();
        }
    }

    public static void start(Activity context, int requestCode) {
        Intent intent = new Intent(context, PaymentCodeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

}
