package com.netease.nim.uikit.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.CacheMemoryUtils;
import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.business.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.extension.GameShareAttachment;
import com.netease.nim.uikit.business.session.extension.TeamAuthAttachment;
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
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class CommonUtil {
    public static boolean updateDialogIsShow = false;
    public static boolean localDialogIsShow = false;
    public static boolean isDownloading = false;//如果当前正在下载中的话，就不会重新调用查询接口
    public static final int SHARE_SUCCESS = 1;//分享成功
    public static final int SHARE_FAIL = 2;//分享失败
    public static final int SHARE_CANCEL = 3;//分享取消
    public static final String ASSISTANT = "ASSISTANT_ACCOUNT";
    public static final String AUTO_ADD_TYPE = "AUTO_ADD_TYPE";
    public static final String CANCEL = "update_cancel";
    public static final String AUTO_ADD_VALUE = "AUTO_ADD_VALUE";
    public static final String BACKGROUND = "SESSION_BACKGROUND";
    public static final String BACKGROUND_URL = "SESSION_BACKGROUND_URL";
    public static final String ALIPAYUID = "aliuid";
    public static final String YCHAT_ACCOUNT = "YCHAT_ACCOUNT";
    public static final String[] strings = {"local_background_one", "local_background_two", "local_background_three", "local_background_four", "local_background_five", "local_background_six", "local_background"};

    public static String getCacheDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "konglechui_download";
    }

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
            String uriString = UserPreferences.getString(UserPreferences.KEY_SHARE_URI, null);
            Uri uri = Uri.parse(uriString);
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

        String uriString = UserPreferences.getString(UserPreferences.KEY_SHARE_URI, null);
        if (TextUtils.isEmpty(uriString)) {
            return;
        }
        Activity activity = (Activity) context;
        Uri data = Uri.parse(uriString);
        final EasyEditDialog2 shareDialog = new EasyEditDialog2(activity);
        shareDialog.setCancelable(false);
        shareDialog.setCanceledOnTouchOutside(false);
        shareDialog.setTitle("发送给");
        if (sessionType == SessionTypeEnum.P2P) {
            if (id.equals(SPUtils.getInstance().getString(CommonUtil.ASSISTANT))) {
                shareDialog.setSubTitle("空了吹小助手");
                shareDialog.setP2pAddr(SPUtils.getInstance().getString(CommonUtil.ASSISTANT));
            } else {
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
                NIMClient.getService(MsgService.class).sendMessage(gameMessage, false).setCallback(new RequestCallback<Void>() {
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
                        YchatToastUtils.showShort("分享失败：" + code);
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
        String uriString = UserPreferences.getString(UserPreferences.KEY_SHARE_URI, null);
        if (TextUtils.isEmpty(uriString)) {
            return;
        }
        Activity activity = (Activity) context;
        Uri data = Uri.parse(uriString);
        final EasyEditDialog2 shareDialog = new EasyEditDialog2(activity);
        shareDialog.setCancelable(false);
        shareDialog.setCanceledOnTouchOutside(false);
        shareDialog.setTitle("发送给");
        if (sessionType == SessionTypeEnum.P2P) {
            if (id.equals(SPUtils.getInstance().getString(CommonUtil.ASSISTANT))) {
                shareDialog.setSubTitle("空了吹小助手");
                shareDialog.setP2pAddr(SPUtils.getInstance().getString(CommonUtil.ASSISTANT));
            } else {
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
                NIMClient.getService(MsgService.class).sendMessage(imgMsg, false).setCallback(new RequestCallback<Void>() {
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


    /**
     * 清除更新相关信息
     */
    public static void clearUpdateInfo() {
        SPUtils instance = SPUtils.getInstance();
        instance.put("localApkPath", "");
        instance.put("apkName", "");
        instance.put("server_version", "");
        instance.put("updateUrl", "");
        instance.put("isForce", -1);
        instance.put("update", -1);
    }

    /**
     * 设置更新相关信息
     */
    public static void setUpdateInfo(String isForce, String update, String serverVersion, String downUrl) {
        SPUtils instance = SPUtils.getInstance();
        instance.put("isForce", isForce);
        instance.put("update", update);
        instance.put("apkName", "konglechui" + serverVersion + ".apk");
        instance.put("server_version", serverVersion);
        instance.put("updateUrl", downUrl);
    }

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {       //500毫秒内按钮无效，这样可以控制快速点击，自己调整频率
            return true;
        }
        lastClickTime = time;
        return false;
    }


    public static String getInviteTipContent(String sessionId, TeamAuthAttachment attachment) {
        //邀请通知只有群主和管理员看到，其他成员不可见
        String fromId = attachment.getInviteTipFromId();
        String[] toIdArray = attachment.getInviteTipToId().split(",");
        String content = "";
        switch (attachment.getInviteTipType()) {
            case "-1"://本地临时改变状态为-1表示已确认
                content = "\"" + TeamHelper.getTeamMemberDisplayNameYou(sessionId, fromId) + "\"" + "想邀请" + toIdArray.length + "位朋友加入群聊  已确认";
                break;
            case TeamAuthAttachment.APPLY1:
                TeamMember member = NimUIKit.getTeamProvider().getTeamMember(sessionId, NimUIKit.getAccount());
                if (NimUIKit.getAccount().equals(fromId)) {
                    content = "[邀请验证信息]";//发起人（普通成员）
                } else if (member != null && (member.getType() == TeamMemberType.Owner || member.getType() == TeamMemberType.Manager)) {
                    content = "[邀请验证信息]";//群主管理员
                } else {
                    content = "";//其他普通成员
                }
                break;
            case TeamAuthAttachment.AGREE2:
                String connectNickname = "";
                for (int i = 0, len = toIdArray.length; i < len; i++) {
                    String toNickname = TeamHelper.getTeamMemberDisplayNameYou(sessionId, toIdArray[i]);
                    if (i == len - 1) {
                        connectNickname = connectNickname + toNickname;
                    } else {
                        connectNickname = connectNickname + toNickname + ",";
                    }
                }
                content = TeamHelper.getTeamMemberDisplayNameYou(sessionId, fromId) + "邀请" + connectNickname + "加入了群 ";
                break;
            case TeamAuthAttachment.ACCEPT3:
                String fromName = TeamHelper.getTeamMemberDisplayNameYou(sessionId, fromId);
                String toName = "";
                if (toIdArray != null && toIdArray.length > 0) {
                    for (int i = 0, len = toIdArray.length; i < len; i++) {
                        String name = TeamHelper.getTeamMemberDisplayNameYou(sessionId, toIdArray[i]);
                        if (i == len - 1) {
                            toName = toName + name;
                        } else {
                            toName = toName + name + ",";
                        }
                    }
                }
                content = toName + "接受" + fromName + "的邀请进群 ";
                break;

            case TeamAuthAttachment.CREATE_TEAM4:
                content = TeamHelper.getTeamMemberDisplayNameYou(sessionId, fromId) + "创建了群聊 ";
                break;
        }
        return content;

    }

    public static void setCancelValue(boolean isCancle) {
        if (isCancle) {
            CacheMemoryUtils.getInstance().put("cancel", "1");
        } else {
            CacheMemoryUtils.getInstance().put("cancel", "0");
        }
    }

    /**
     * 紧急更新
     * @param emergencyUrl
     */
    public static void webviwDownload(String emergencyUrl) {
        Uri uri = Uri.parse(emergencyUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
