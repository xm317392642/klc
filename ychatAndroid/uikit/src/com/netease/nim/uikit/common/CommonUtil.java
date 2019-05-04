package com.netease.nim.uikit.common;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.business.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.extension.GameShareAttachment;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog2;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.impl.preference.UserPreferences;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class CommonUtil {
    public static final int SHARE_SUCCESS = 1;//分享成功
    public static final int SHARE_FAIL = 2;//分享失败
    public static final int SHARE_CANCEL = 3;//分享取消
    public static final String ASSISTANT_ACCOUNT = "10010005";
    public static final String AUTO_ADD_TYPE = "AUTO_ADD_TYPE";
    public static final String AUTO_ADD_VALUE = "AUTO_ADD_VALUE";
    public static final String BACKGROUND = "SESSION_BACKGROUND";
    public static final String BACKGROUND_URL = "SESSION_BACKGROUND_URL";
    public static final String[] strings = {"local_background_one", "local_background_two", "local_background_three", "local_background_four", "local_background_five", "local_background_six", "local_background"};

    public static void addTag(RecentContact recent, long tag) {
        tag = recent.getTag() | tag;
        recent.setTag(tag);
    }

    public static void removeTag(RecentContact recent, long tag) {
        tag = recent.getTag() & ~tag;
        recent.setTag(tag);
    }

    public static boolean isTagSet(RecentContact recent, long tag) {
        return (recent.getTag() & tag) == tag;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }


    public static void shareResultDialg(Activity activity, SimpleCallback simpleCallback) {
        if (UserPreferences.getShare()) {
            EasyAlertDialog alertDialog = new EasyAlertDialog(activity);
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            String uriString=UserPreferences.getString(UserPreferences.KEY_SHARE_URI,null);
            Uri uri=Uri.parse(uriString);
            String sourceName = uri.getQueryParameter(Extras.EXTRA_GAME_SOURCE_NAME);//游戏名字
            alertDialog.setMessage("分享成功，留在空了吹，还是返回" + sourceName + "？");
            alertDialog.addNegativeButton("留在空了吹", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        alertDialog.dismiss();
                        //UserPreferences.setShare(false);
                        simpleCallback.onResult(true, 200, 200);

                    }
            );
            alertDialog.addPositiveButton("返回" + sourceName, EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        alertDialog.dismiss();
                        simpleCallback.onResult(false, 200, 200);
                        CommonUtil.backYaoyao(activity, CommonUtil.SHARE_SUCCESS);

                    });
            alertDialog.show();
        }
    }

    /**
     * 返回到吆吆app
     *
     * @param activity
     * @param shareResult 1成功，2失败，3取消分享
     */
    public static void backYaoyao(Activity activity, int shareResult) {
        String value = UserPreferences.getShareValue();
        String schema = UserPreferences.getString(UserPreferences.KEY_SCHEMA, "");
        Uri uri;
        if (UserPreferences.SHARE_IMG.equals(value)) {
            uri = Uri.parse(schema + "://yaoliao/image_share?share_result=" + shareResult);
        } else if (UserPreferences.SHARE_URL.equals(value)) {
            uri = Uri.parse(schema + "://yaoliao/url_share?share_result=" + shareResult);
        } else {
            uri = Uri.parse(schema + "://yaoliao/open?share_result=" + shareResult);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        List<ResolveInfo> activities = activity.getPackageManager().queryIntentActivities(intent, 0);
        if (activities.size() > 0) {
            activity.startActivity(intent);
            activity.finish();
        } else {
            YchatToastUtils.showShort("uri格式有误：" + uri.toString());
        }
    }

    public static String getValueWithKey(Uri data, String key) {
        String result = null;
        String query = data.getQuery();
        if (!TextUtils.isEmpty(query)) {
            String[] queries = query.split("&");
            if (queries != null && queries.length > 0) {
                for (String string : queries) {
                    String start = key + "=";
                    if (string.startsWith(start)) {
                        result = string.substring(start.length());
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 游戏分享
     *
     * @param context
     * @param id
     * @param sessionType
     * @param customization
     * @param anchor
     */
    public static void shareGameDialog(Context context, String id, SessionTypeEnum sessionType, SessionCustomization
            customization, IMMessage anchor) {
        /**
         * host = yyyp
         * path = /url_share
         * query =
         * source_name=吆吆约牌
         * &title=血战到底 计分 1分 8局3番(小旭茶社)血战到底 计分 1分 8局3番(小旭茶社)
         * &description=自摸加底、金构钓、海底捞、点杠花(点炮)
         * &url=https://3w.huanqiu.com/a/c36dc8/7LxuZDTmGpG
         * &icon_url=https://t1.huanqiu.cn/0a330953b6a0442d1d99b9d02e959321.jpg
         * &source_icon=http://icon.mobanwang.com/UploadFiles_8971/200910/20091011134333685.png
         */

        String uriString=UserPreferences.getString(UserPreferences.KEY_SHARE_URI,null);
        if(TextUtils.isEmpty(uriString)){
            return;
        }
        Activity activity = (Activity) context;
        Uri data = Uri.parse(uriString);
        final EasyEditDialog2 shareDialog = new EasyEditDialog2(activity);
        shareDialog.setCancelable(false);
        shareDialog.setCanceledOnTouchOutside(false);
        shareDialog.setTitle("发送给");
        if (sessionType == SessionTypeEnum.P2P) {
            if(id.equals(ASSISTANT_ACCOUNT)){
                shareDialog.setSubTitle("空了吹小助手");
                shareDialog.setP2pAddr(ASSISTANT_ACCOUNT);
            }else{
                shareDialog.setSubTitle(UserInfoHelper.getUserDisplayName(id));
                shareDialog.setP2pAddr(id);
            }

        } else {
            shareDialog.setSubTitle(TeamHelper.getTeamName(id));
            shareDialog.setTeamAddr(NimUIKit.getTeamProvider().getTeamById(id));
        }


        String source_scheme = data.getHost();
        String title = data.getQueryParameter(Extras.EXTRA_GAME_TTILE);//title
        String des = data.getQueryParameter(Extras.EXTRA_GAME_DES);//description
        String sourceName = data.getQueryParameter(Extras.EXTRA_GAME_SOURCE_NAME);//source_name
        String result = getValueWithKey(data, Extras.EXTRA_GAME_LINKURL);
        String linkUrl = !TextUtils.isEmpty(result) ? result : data.getQueryParameter(Extras.EXTRA_GAME_LINKURL);
        String linkIcon = data.getQueryParameter(Extras.EXTRA_GAME_LINK_ICON);//icon_url
        String linkSource = data.getQueryParameter(Extras.EXTRA_GAME_LINK_SOURCE);//source_icon

        shareDialog.setmDesc(des);
        shareDialog.addNegativeButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDialog.dismiss();
            }
        });
        shareDialog.addPositiveButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    GameShareAttachment attachment = new GameShareAttachment();
                    attachment.setShareLinkTitle(title);
                    attachment.setShareLinkDes(des);
                    attachment.setShareLinkSourceName(sourceName);
                    attachment.setShareLinkImage(linkIcon);
                    attachment.setShareLinkSourceImage(linkSource);
                    attachment.setShareLinkUrl(linkUrl);

                    IMMessage gameMessage = MessageBuilder.createCustomMessage(
                            id, sessionType, "游戏分享", attachment
                    );
                    DialogMaker.showProgressDialog(context, "分享中...", false);
                    NIMClient.getService(MsgService.class).sendMessage(gameMessage,false).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            DialogMaker.dismissProgressDialog();
                            if (sessionType == SessionTypeEnum.P2P) {
                                P2PMessageActivity.start(context, id, sourceName, source_scheme, customization, anchor);
                            } else if (sessionType == SessionTypeEnum.Team) {
                                TeamMessageActivity.start(context, id, sourceName, source_scheme, customization, null, anchor);
                            }
                            activity.finish();
                        }

                        @Override
                        public void onFailed(int code) {
                            DialogMaker.dismissProgressDialog();
                            YchatToastUtils.showShort( "分享失败：" + code);
                        }

                        @Override
                        public void onException(Throwable exception) {
                            DialogMaker.dismissProgressDialog();
                        }
                    });
                    shareDialog.dismiss();

            }
        });
        shareDialog.show();
    }

    /**
     * 分享图片对话框
     *
     * @param context
     * @param id
     * @param sessionType
     * @param customization
     * @param anchor
     */
    public static void sharePicDialog(Context context, String id, SessionTypeEnum sessionType, SessionCustomization
            customization, IMMessage anchor) {
        String uriString=UserPreferences.getString(UserPreferences.KEY_SHARE_URI,null);
        if(TextUtils.isEmpty(uriString)){
            return;
        }
        Activity activity = (Activity) context;
        Uri data=Uri.parse(uriString);
        final EasyEditDialog2 shareDialog = new EasyEditDialog2(activity);
        shareDialog.setCancelable(false);
        shareDialog.setCanceledOnTouchOutside(false);
        shareDialog.setTitle("发送给");
        if (sessionType == SessionTypeEnum.P2P) {
            if(id.equals(ASSISTANT_ACCOUNT)){
                shareDialog.setSubTitle("空了吹小助手");
                shareDialog.setP2pAddr(ASSISTANT_ACCOUNT);
            }else{
                shareDialog.setSubTitle(UserInfoHelper.getUserDisplayName(id));
                shareDialog.setP2pAddr(id);
            }


        } else {
            shareDialog.setSubTitle(TeamHelper.getTeamName(id));
            shareDialog.setTeamAddr(NimUIKit.getTeamProvider().getTeamById(id));
        }

        String picPath = data.getQueryParameter(Extras.EXTRA_PIC_PATH);
        String source_name = data.getQueryParameter(Extras.EXTRA_SOURCE_NAME);
        String source_scheme = data.getHost();
        Log.e("xx", "scheme=" + data.getScheme() + " host = " + data.getHost() + " path = " + data.getPath() + " params query = " + data.getQuery());
        Log.e("xx", "pic_path=" + picPath);
        Log.e("xx", "@@@@@@@@@@source_scheme@@@@@@@@@@@@@=" + source_scheme);
        Log.e("xx", "source_name=" + source_name);

        shareDialog.setShareImgFilepath(picPath);
        shareDialog.addNegativeButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDialog.dismiss();
            }
        });
        shareDialog.addPositiveButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogMaker.showProgressDialog(context, "分享中...", false);
                IMMessage imgMsg = MessageBuilder.createImageMessage(id, sessionType, new File(picPath));
                NIMClient.getService(MsgService.class).sendMessage(imgMsg,false).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        DialogMaker.dismissProgressDialog();
                        if (sessionType == SessionTypeEnum.P2P) {
                            P2PMessageActivity.start(context, id, source_name, source_scheme, customization, anchor);
                        } else if (sessionType == SessionTypeEnum.Team) {
                            TeamMessageActivity.start(context, id, source_name, source_scheme, customization, null, anchor);
                        }
                        activity.finish();
                    }

                    @Override
                    public void onFailed(int code) {
                        YchatToastUtils.showShort("分享失败：" + code);
                        DialogMaker.dismissProgressDialog();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                });
                shareDialog.dismiss();

            }
        });
        shareDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                shareDialog.dismiss();
            }
        });
        shareDialog.show();

    }

    public static String getBackgroundUrl() {
        String[] strings = {"local_background_one", "local_background_two", "local_background_three", "local_background_four", "local_background_five", "local_background_six", "local_background"};
        int type = SPUtils.getInstance().getInt(BACKGROUND, -1);
        if (type == 0) {
            String path = SPUtils.getInstance().getString(BACKGROUND_URL);
            String head = "file://";
            String newpath = path.substring(head.length());
            File file = new File(newpath);
            if (file.exists()) {
                return path;
            } else {
                String backgroundUri = "android.resource://com.xr.ychat/drawable/" + strings[0];
                SPUtils.getInstance().put(BACKGROUND, 1);
                SPUtils.getInstance().put(BACKGROUND_URL, backgroundUri);
                return backgroundUri;
            }
        } else if (type == -1) {
            String backgroundUri = "android.resource://com.xr.ychat/drawable/" + strings[6];
            SPUtils.getInstance().put(BACKGROUND, 7);
            SPUtils.getInstance().put(BACKGROUND_URL, backgroundUri);
            return backgroundUri;
        } else {
            return "android.resource://com.xr.ychat/drawable/" + strings[type - 1];
        }
    }

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1200) {       //500毫秒内按钮无效，这样可以控制快速点击，自己调整频率
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
