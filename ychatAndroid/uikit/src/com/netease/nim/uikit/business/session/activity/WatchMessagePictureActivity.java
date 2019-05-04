package com.netease.nim.uikit.business.session.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.blankj.utilcode.util.ImageUtils;
import com.bumptech.glide.Glide;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog.onSeparateItemClickListener;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.ui.imageview.BaseZoomableImageView;
import com.netease.nim.uikit.common.ui.imageview.ImageGestureListener;
import com.netease.nim.uikit.common.ui.imageview.MultiTouchZoomableImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * 查看聊天消息原图
 * Created by huangjun on 2015/3/6.
 */
public class WatchMessagePictureActivity extends SwipeBackUI {
    // 支付宝包名
    private static final String ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone";
    // 旧版支付宝二维码通用 Intent Scheme Url 格式
    private static final String INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" + "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
    private static final String YCHAT_USER = "http://share.yaoliaoim.com?accid=";
    private static final String YCHAT_GROUP = "http://share.yaoliaoim.com?groupid=";
    private static final String ALIPAY_QRCODE = "qr.alipay.com";
    private static final String INTENT_EXTRA_IMAGE = "INTENT_EXTRA_IMAGE";
    private static final String INTENT_EXTRA_MENU = "INTENT_EXTRA_MENU";

    private static final int MODE_NOMARL = 0;
    private static final int MODE_GIF = 1;

    private Handler handler;
    private IMMessage message;
    private boolean isShowMenu;
    private List<IMMessage> imageMsgList = new ArrayList<>();
    private int firstDisplayImageIndex = 0;

    private boolean newPageSelected = false;

    private View loadingLayout;
    private MultiTouchZoomableImageView image;
    private ImageView simpleImageView;
    private int mode;
    protected CustomAlertDialog alertDialog;
    private ViewPager imageViewPager;
    private PagerAdapter adapter;
    private AbortableFuture downloadFuture;

    public static void start(Context context, IMMessage message) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE, message);
        intent.setClass(context, WatchMessagePictureActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, IMMessage message, boolean isShowMenu) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE, message);
        intent.putExtra(INTENT_EXTRA_MENU, isShowMenu);
        intent.setClass(context, WatchMessagePictureActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_watch_picture_activity);

        handleIntent();

        findViews();

        loadMsgAndDisplay();

        handler = new Handler();
        registerObservers(true);
    }

    private void handleIntent() {
        this.message = (IMMessage) getIntent().getSerializableExtra(INTENT_EXTRA_IMAGE);
        mode = ImageUtil.isGif(((ImageAttachment) message.getAttachment()).getExtension()) ? MODE_GIF : MODE_NOMARL;
        isShowMenu = getIntent().getBooleanExtra(INTENT_EXTRA_MENU, true);
    }

    @Override
    protected void onDestroy() {
        registerObservers(false);
        imageViewPager.setAdapter(null);
        if (downloadFuture != null) {
            downloadFuture.abort();
            downloadFuture = null;
        }
        super.onDestroy();
    }

    private void findViews() {
        alertDialog = new CustomAlertDialog(this);
        loadingLayout = findViewById(R.id.loading_layout);

        imageViewPager = (ViewPager) findViewById(R.id.view_pager_image);
        simpleImageView = (ImageView) findViewById(R.id.simple_image_view);

        if (mode == MODE_GIF) {
            simpleImageView.setVisibility(View.VISIBLE);
            simpleImageView.setOnLongClickListener(v -> {
                if (isOriginImageHasDownloaded(message)) {
                    showWatchPictureAction();
                }
                return true;
            });
            imageViewPager.setVisibility(View.GONE);
        } else if (mode == MODE_NOMARL) {
            simpleImageView.setVisibility(View.GONE);
            imageViewPager.setVisibility(View.VISIBLE);
        }
    }

    // 加载并显示
    private void loadMsgAndDisplay() {
        if (mode == MODE_NOMARL) {
            queryImageMessages();
        } else if (mode == MODE_GIF) {
            displaySimpleImage();
        }
    }

    // 显示单个gif图片
    private void displaySimpleImage() {
        String path = ((ImageAttachment) message.getAttachment()).getPath();
        String thumbPath = ((ImageAttachment) message.getAttachment()).getThumbPath();
        if (!TextUtils.isEmpty(path)) {
            Glide.with(this).asGif().load(new File(path)).into(simpleImageView);
            return;
        }
        if (!TextUtils.isEmpty(thumbPath)) {
            Glide.with(this).asGif().load(new File(thumbPath)).into(simpleImageView);
        }

        if (message.getDirect() == MsgDirectionEnum.In) {
            requestOriImage(message);
        }
    }


    // 查询并显示图片，带viewPager
    private void queryImageMessages() {
        IMMessage anchor = MessageBuilder.createEmptyMessage(message.getSessionId(), message.getSessionType(), 0);
        NIMClient.getService(MsgService.class).queryMessageListByType(MsgTypeEnum.image, anchor, Integer.MAX_VALUE).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> param) {
                for (IMMessage imMessage : param) {
                    if (!ImageUtil.isGif(((ImageAttachment) imMessage.getAttachment()).getExtension())) {
                        imageMsgList.add(imMessage);
                    }
                }
                // imageMsgList.addAll(param);
                Collections.reverse(imageMsgList);
                setDisplayIndex();
                setViewPagerAdapter();
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    // 设置第一个选中的图片index
    private void setDisplayIndex() {
        for (int i = 0; i < imageMsgList.size(); i++) {
            IMMessage imageObject = imageMsgList.get(i);
            if (compareObjects(message, imageObject)) {
                firstDisplayImageIndex = i;
                break;
            }
        }
    }

    protected boolean compareObjects(IMMessage t1, IMMessage t2) {
        return (t1.getUuid().equals(t2.getUuid()));
    }

    private void setViewPagerAdapter() {
        adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return imageMsgList == null ? 0 : imageMsgList.size();
            }

            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View layout = (View) object;
                BaseZoomableImageView iv = (BaseZoomableImageView) layout.findViewById(R.id.watch_image_view);
                iv.clear();
                container.removeView(layout);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return (view == object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ViewGroup layout;
                layout = (ViewGroup) LayoutInflater.from(WatchMessagePictureActivity.this).inflate(R.layout.nim_image_layout_multi_touch, null);
                layout.setBackgroundColor(Color.BLACK);

                container.addView(layout);
                layout.setTag(position);

                if (position == firstDisplayImageIndex) {
                    onViewPagerSelected(position);
                }

                return layout;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };

        imageViewPager.setAdapter(adapter);
        imageViewPager.setOffscreenPageLimit(2);
        imageViewPager.setCurrentItem(firstDisplayImageIndex);
        imageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset == 0f && newPageSelected) {
                    newPageSelected = false;
                    onViewPagerSelected(position);
                }
            }

            @Override
            public void onPageSelected(int position) {
                newPageSelected = true;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void onViewPagerSelected(int position) {
        if (downloadFuture != null) {
            downloadFuture.abort();
            downloadFuture = null;
        }
        updateCurrentImageView(position);
        onImageViewFound(image);
    }

    // 初始化每个view的image
    protected void updateCurrentImageView(final int position) {
        View currentLayout = imageViewPager.findViewWithTag(position);
        if (currentLayout == null) {
            ViewCompat.postOnAnimation(imageViewPager, new Runnable() {

                @Override
                public void run() {
                    updateCurrentImageView(position);
                }
            });
            return;
        }
        image = currentLayout.findViewById(R.id.watch_image_view);
        requestOriImage(imageMsgList.get(position));
    }

    // 若图片已下载，直接显示图片；若图片未下载，则下载图片
    private void requestOriImage(IMMessage msg) {
        if (isOriginImageHasDownloaded(msg)) {
            onDownloadSuccess(msg);
            return;
        }

        // async download original image
        onDownloadStart(msg);
        message = msg; // 下载成功之后，判断是否是同一条消息时需要使用
        downloadFuture = NIMClient.getService(MsgService.class).downloadAttachment(msg, false);
    }

    private boolean isOriginImageHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((ImageAttachment) message.getAttachment()).getPath())) {
            return true;
        }

        return false;
    }

    /**
     * ******************************** 设置图片 *********************************
     */

    private void setThumbnail(IMMessage msg) {
        String thumbPath = ((ImageAttachment) msg.getAttachment()).getThumbPath();
        String path = ((ImageAttachment) msg.getAttachment()).getPath();

        Bitmap bitmap = null;
        if (!TextUtils.isEmpty(thumbPath)) {
            bitmap = BitmapDecoder.decodeSampledForDisplay(thumbPath);
            bitmap = ImageUtil.rotateBitmapInNeeded(thumbPath, bitmap);
        } else if (!TextUtils.isEmpty(path)) {
            bitmap = BitmapDecoder.decodeSampledForDisplay(path);
            bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
        }

        if (bitmap != null) {
            image.setImageBitmap(bitmap);
            return;
        }

        image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnLoading()));
    }

    private void setImageView(final IMMessage msg) {
        String path = ((ImageAttachment) msg.getAttachment()).getPath();
        if (TextUtils.isEmpty(path)) {
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnLoading()));
            return;
        }

        Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(path, false);
        bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
        if (bitmap == null) {
            YchatToastUtils.showShort(R.string.picker_image_error);
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        } else {
            image.setImageBitmap(bitmap);
        }
    }

    private int getImageResOnLoading() {
        return R.drawable.nim_image_default;
    }

    private int getImageResOnFailed() {
        return R.drawable.nim_image_download_failed;
    }

    /**
     * ********************************* 下载 ****************************************
     */

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, register);
    }

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            if (!msg.isTheSame(message) || isDestroyedCompatible()) {
                return;
            }

            if (isOriginImageHasDownloaded(msg)) {
                onDownloadSuccess(msg);
            } else if (msg.getAttachStatus() == AttachStatusEnum.fail) {
                onDownloadFailed();
            }
        }
    };

    private void onDownloadStart(final IMMessage msg) {
        if (TextUtils.isEmpty(((ImageAttachment) msg.getAttachment()).getPath())) {
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            loadingLayout.setVisibility(View.GONE);
        }
        if (mode == MODE_NOMARL) {
            setThumbnail(msg);
        }
    }

    private void onDownloadSuccess(final IMMessage msg) {
        loadingLayout.setVisibility(View.GONE);
        if (mode == MODE_NOMARL) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    setImageView(msg);
                }
            });
        } else if (mode == MODE_GIF) {
            displaySimpleImage();
        }
    }

    private void onDownloadFailed() {
        loadingLayout.setVisibility(View.GONE);
        if (mode == MODE_NOMARL) {
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        } else if (mode == MODE_GIF) {
            simpleImageView.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        }
        YchatToastUtils.showShort(R.string.download_picture_fail);
    }

    /**
     * ***********************************图片点击事件*******************************************
     */

    // 设置图片点击事件
    protected void onImageViewFound(BaseZoomableImageView imageView) {
        imageView.setImageGestureListener(new ImageGestureListener() {

            @Override
            public void onImageGestureSingleTapConfirmed() {
                onImageViewTouched();
            }

            @Override
            public void onImageGestureLongPress() {
                showWatchPictureAction();
            }

            @Override
            public void onImageGestureFlingDown() {
                finish();
            }
        });
    }

    // 图片单击
    protected void onImageViewTouched() {
        finish();
    }

    // 图片长按
    protected void showWatchPictureAction() {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
            return;
        }
        alertDialog.clearData();
        String title;
        if (!TextUtils.isEmpty(((ImageAttachment) message.getAttachment()).getPath())) {
            title = getString(R.string.save_to_device);
            alertDialog.addItem(title, new onSeparateItemClickListener() {

                @Override
                public void onClick() {
                    savePicture();
                }
            });
        }
        Bitmap obmp = image.getImageBitmap();
        Observable.create((ObservableEmitter<String> emitter) -> {
            String result = QRCodeDecoder.syncDecodeQRCode(obmp);
            emitter.onNext(TextUtils.isEmpty(result) ? "" : result);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (!TextUtils.isEmpty(s)) {
                            alertDialog.addItem("识别二维码", new onSeparateItemClickListener() {
                                @Override
                                public void onClick() {
                                    if (s.contains(YCHAT_USER)) {
                                        Uri uri = Uri.parse(s);
                                        ContactHttpClient.getInstance().querySearching(Preferences.getWeiranUid(WatchMessagePictureActivity.this), Preferences.getWeiranToken(WatchMessagePictureActivity.this), 3, uri.getQueryParameter("accid"), new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                                            @Override
                                            public void onSuccess(RequestInfo aVoid) {
                                                if (!TextUtils.isEmpty(aVoid.getAccid()) && aVoid.getAccid().length() > 1) {
                                                    NimUIKitImpl.getContactEventListener().onItemClick(WatchMessagePictureActivity.this, aVoid.getAccid());
                                                    finish();
                                                } else {
                                                    YchatToastUtils.showShort("用户可能关闭查找权限");
                                                }
                                            }

                                            @Override
                                            public void onFailed(int code, String errorMsg) {
                                                YchatToastUtils.showShort("用户可能关闭查找权限");
                                            }
                                        });
                                    } else if (s.contains(YCHAT_GROUP)) {
                                        Uri uri = Uri.parse(s);
                                        String groupid = uri.getQueryParameter("groupid");
                                        NIMClient.getService(TeamService.class).searchTeam(groupid).setCallback(new RequestCallback<Team>() {
                                            @Override
                                            public void onSuccess(Team result) {
                                                if (result != null) {
                                                    if (TeamHelper.isTeamMember(groupid, NimUIKit.getAccount())) {
                                                        NimUIKitImpl.startTeamSession(WatchMessagePictureActivity.this, groupid);
                                                        finish();
                                                    } else {
                                                        if (result.getVerifyType() != VerifyTypeEnum.Free) {
                                                            String url = "scheme://ychat/jointeam?EXTRA_ID=" + groupid;
                                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                            startActivity(intent);
                                                        } else {
                                                            final EasyEditDialog requestDialog = new EasyEditDialog(WatchMessagePictureActivity.this);
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
                                                                    String msg = requestDialog.getEditMessage();
                                                                    if (TextUtils.isEmpty(msg)) {
                                                                        msg = String.format("我是%1$s", UserInfoHelper.getUserName(NimUIKit.getAccount()));
                                                                    }
                                                                    NIMClient.getService(TeamService.class).applyJoinTeam(result.getId(), msg).setCallback(new RequestCallback<Team>() {
                                                                        @Override
                                                                        public void onSuccess(Team team) {
                                                                            UpdateMemberChangeService.start(WatchMessagePictureActivity.this, NimUIKit.getAccount(), result.getId(), 1, "qr");
                                                                            NimUIKitImpl.startTeamSession(WatchMessagePictureActivity.this, result.getId());
                                                                            finish();
                                                                        }

                                                                        @Override
                                                                        public void onFailed(int code) {
                                                                            YchatToastUtils.showShort("failed, error code =" + code);
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
                                                } else {
                                                    YchatToastUtils.showShort("群组不存在");
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
                                    } else if (s.toLowerCase().contains(ALIPAY_QRCODE)) {
                                        Uri uri = Uri.parse(s);
                                        List<String> pathSegments = uri.getPathSegments();
                                        String path = pathSegments.get(0);
                                        if (hasInstalledAlipayClient(WatchMessagePictureActivity.this)) {
                                            startAlipayClient(WatchMessagePictureActivity.this, path);
                                        } else {
                                            YchatToastUtils.showShort("未安装支付宝");
                                        }
                                    } else {
                                        if (URLUtil.isNetworkUrl(s)) {
                                            Intent intent = new Intent();
                                            intent.setData(Uri.parse(s));
                                            intent.setAction(Intent.ACTION_VIEW);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            });
                        }
                        alertDialog.show();
                    }
                });
    }

    // 保存图片
    public void savePicture() {
        ImageAttachment attachment = (ImageAttachment) message.getAttachment();
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
            return;
        }

        String srcFilename = attachment.getFileName();
        //默认jpg
        String extension = TextUtils.isEmpty(attachment.getExtension()) ? "jpg" : attachment.getExtension();
        srcFilename += ("." + extension);

        String picPath = StorageUtil.getSystemImagePath();
        String dstPath = picPath + srcFilename;
        if (AttachmentStore.copy(path, dstPath) != -1) {
            try {
                Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                image.draw(canvas);
                File file = new File(dstPath);
                ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG);
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
                YchatToastUtils.showShort("图片保存成功");
            } catch (Exception e) {
                YchatToastUtils.showShort(getString(R.string.picture_save_fail));
            }
        } else {
            YchatToastUtils.showShort(getString(R.string.picture_save_fail));
        }
    }

    public static boolean startAlipayClient(Activity activity, String urlCode) {
        return startIntentUrl(activity, INTENT_URL_FORMAT.replace("{urlCode}", urlCode));
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
}
