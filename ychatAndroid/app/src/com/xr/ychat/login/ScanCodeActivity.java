package com.xr.ychat.login;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.blankj.utilcode.util.EncodeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.helper.SendImageHelper;
import com.netease.nim.uikit.business.team.activity.ScanCodeErrorActivity;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.xr.ychat.R;
import com.xr.ychat.session.SessionHelper;

import java.io.File;
import java.net.URISyntaxException;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ScanCodeActivity extends SwipeBackUI implements QRCodeView.Delegate {
    // 支付宝包名
    private static final String ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone";
    private static final int PICK_AVATAR_REQUEST = 0x0E;
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private TextView album;
    private ZBarView mZBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_scan_qrcode);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("扫码");
        mToolbar.setNavigationOnClickListener(v -> finish());
        mZBarView = (ZBarView) findViewById(R.id.zbarview);
        mZBarView.setDelegate(this);
        album = (TextView) findViewById(R.id.btn_next_step);
        album.setOnClickListener(v -> {
            PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
            option.crop = true;
            option.multiSelect = false;
            option.cropOutputImageWidth = 720;
            option.cropOutputImageHeight = 720;
            PickImageHelper.pickCode(ScanCodeActivity.this, PICK_AVATAR_REQUEST, option);
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ScanCodeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mZBarView.startCamera();
        mZBarView.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        mZBarView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZBarView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        dealScanQRCodeResult(result);
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        YchatToastUtils.showShort("未发现二维码");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mZBarView.showScanRect();
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_AVATAR_REQUEST) {
            String path = data.getStringExtra(com.netease.nim.uikit.business.session.constant.Extras.EXTRA_FILE_PATH);
            Observable.create((ObservableEmitter<String> emitter) -> {
                String result = QRCodeDecoder.syncDecodeQRCode(path);
                emitter.onNext(TextUtils.isEmpty(result) ? "" : result);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String result) throws Exception {
                            dealScanQRCodeResult(result);
                        }
                    });
        }
    }

    /**
     * 图片选取回调
     */
    private void onPickImageActivityResult(Intent data) {
        boolean local = data.getBooleanExtra(Extras.EXTRA_FROM_LOCAL, false);
        if (local) {
            SendImageHelper.sendImageAfterSelfImagePicker(ScanCodeActivity.this, data, new SendImageHelper.Callback() {
                @Override
                public void sendImage(File file, boolean isOrig) {
                    mZBarView.decodeQRCode(getRealFilePath(ScanCodeActivity.this, Uri.parse(file.getAbsolutePath())));
                }
            });
        }
    }

    /**
     * 根据Uri获取文件真实地址
     */
    private String getRealFilePath(Context context, Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String realPath = null;
        if (scheme == null)
            realPath = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            realPath = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA},
                    null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        realPath = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        if (TextUtils.isEmpty(realPath)) {
            if (uri != null) {
                String uriString = uri.toString();
                int index = uriString.lastIndexOf("/");
                String imageName = uriString.substring(index);
                File storageDir;

                storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File file = new File(storageDir, imageName);
                if (file.exists()) {
                    realPath = file.getAbsolutePath();
                } else {
                    storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File file1 = new File(storageDir, imageName);
                    realPath = file1.getAbsolutePath();
                }
            }
        }
        return realPath;
    }

    public static boolean startAlipayClient(Activity activity, String urlCode) {
        try {
            String intentFullUrl = "alipayqr://platformapi/startapp?saId=10000007&qrcode=" + EncodeUtils.urlEncode(urlCode);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentFullUrl));
            activity.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 打开 Intent Scheme Url
     *
     * @param intentFullUrl 跳转地址
     */
    public static boolean startIntentUrl(Activity activity, String intentFullUrl) {
        try {
            Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
            activity.startActivity(intent);
            return true;
        } catch (URISyntaxException e) {
            return false;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /**
     * 判断支付宝客户端是否已安装，建议调用转账前检查
     */
    public static boolean hasInstalledAlipayClient(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(ALIPAY_PACKAGE_NAME, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void dealScanQRCodeResult(String result) {
        try {
            if (result.contains(MyCodeActivity.USER_FORMAT)) {
                vibrate();
                if (!NetworkUtil.isNetAvailable(ScanCodeActivity.this)) {
                    YchatToastUtils.showShort(R.string.network_is_not_available);
                    return;
                }
                Uri uri = Uri.parse(result);
                ContactHttpClient.getInstance().querySearching(Preferences.getWeiranUid(ScanCodeActivity.this), Preferences.getWeiranToken(ScanCodeActivity.this), 3, uri.getQueryParameter("accid"), new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                    @Override
                    public void onSuccess(RequestInfo aVoid) {
                        if (!TextUtils.isEmpty(aVoid.getAccid()) && aVoid.getAccid().length() > 1) {
                            NimUIKitImpl.getContactEventListener().onItemClick(ScanCodeActivity.this, aVoid.getAccid());
                            finish();
                        } else {
                            YchatToastUtils.showShort("该用户可能关闭了查找权限");
                        }
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        YchatToastUtils.showShort("该用户可能关闭了查找权限");
                    }
                });
            } else if (result.contains(MyCodeActivity.GROUP_FORMAT)) {
                vibrate();
                if (!NetworkUtil.isNetAvailable(ScanCodeActivity.this)) {
                    YchatToastUtils.showShort(R.string.network_is_not_available);
                    return;
                }
                Uri uri = Uri.parse(result);
                String teamId = uri.getQueryParameter("groupid");
                NIMClient.getService(TeamService.class).searchTeam(teamId).setCallback(new RequestCallback<Team>() {
                    @Override
                    public void onSuccess(Team result) {
                        if (TeamHelper.isTeamMember(result.getId(), NimUIKit.getAccount())) {
                            SessionHelper.startTeamSession(ScanCodeActivity.this, teamId);
                            finish();
                        } else {
                            Gson gson = new Gson();
                            TeamExtension extension = gson.fromJson(result.getExtension(), new TypeToken<TeamExtension>() {
                            }.getType());
                            if (extension != null && TeamExtras.OPEN.equals(extension.getInviteVerity())) {
                                //该群已开启进群验证，只可通过邀请进群
                                startActivity(new Intent(ScanCodeActivity.this, ScanCodeErrorActivity.class));
                            } else {
                                if (result.getVerifyType() != VerifyTypeEnum.Free) {
                                    String url = "scheme://ychat/jointeam?EXTRA_ID=" + teamId;
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    startActivity(intent);
                                } else {
                                    final EasyEditDialog requestDialog = new EasyEditDialog(ScanCodeActivity.this);
                                    requestDialog.setTitle("申请加入群组");
                                    requestDialog.setEditText("我是" + UserInfoHelper.getUserDisplayName(NimUIKit.getAccount()));
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
                                            UpdateMemberChangeService.start(ScanCodeActivity.this, NimUIKit.getAccount(), teamId, 1, "qr");
                                            String msg = requestDialog.getEditMessage();
                                            if (TextUtils.isEmpty(msg)) {
                                                msg = String.format("我是%1$s", UserInfoHelper.getUserName(NimUIKit.getAccount()));
                                            }
                                            NIMClient.getService(TeamService.class).applyJoinTeam(result.getId(), msg).setCallback(new RequestCallback<Team>() {
                                                @Override
                                                public void onSuccess(Team team) {
                                                    SessionHelper.startTeamSession(ScanCodeActivity.this, teamId);
                                                    finish();
                                                }

                                                @Override
                                                public void onFailed(int code) {
                                                    //仅仅是申请成功
                                                    if (code == ResponseCode.RES_TEAM_APPLY_SUCCESS) {
                                                        YchatToastUtils.showShort(R.string.team_apply_to_join_send_success);
                                                    } else if (code == ResponseCode.RES_TEAM_ALREADY_IN) {
                                                        YchatToastUtils.showShort(R.string.has_exist_in_team);
                                                    } else if (code == ResponseCode.RES_TEAM_LIMIT) {
                                                        YchatToastUtils.showShort(R.string.team_num_limit);
                                                    } else {
                                                        YchatToastUtils.showShort("failed, error code =" + code);
                                                    }
                                                    finish();
                                                }

                                                @Override
                                                public void onException(Throwable exception) {

                                                }
                                            });
                                        }
                                    });
                                    requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {

                                        }
                                    });
                                    requestDialog.show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        YchatToastUtils.showShort("群组不存在");
                    }

                    @Override
                    public void onException(Throwable exception) {
                        YchatToastUtils.showShort("网络不可用");
                    }
                });
            } else if (result.toLowerCase().contains("qr.alipay.com")) {
                if (hasInstalledAlipayClient(ScanCodeActivity.this)) {
                    startAlipayClient(ScanCodeActivity.this, result);
                } else {
                    YchatToastUtils.showShort("未安装支付宝");
                }
            } else {
                if (URLUtil.isNetworkUrl(result)) {
                    Intent intent = new Intent();
                    intent.setData(Uri.parse(result));
                    intent.setAction(Intent.ACTION_VIEW);
                    startActivity(intent);
                } else {
                    YchatToastUtils.showShort("未发现二维码");
                }
            }
        } catch (Exception e) {
            YchatToastUtils.showShort("未发现二维码");
        }
    }

}
