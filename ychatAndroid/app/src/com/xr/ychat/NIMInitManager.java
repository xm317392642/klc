package com.xr.ychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.extension.TeamAuthAttachment;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.UpdateInfo;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.util.DownloadUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimStrings;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.model.BroadcastMessage;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.IMMessageFilter;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.xr.ychat.event.OnlineStateEventManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by hzchenkang on 2017/9/26.
 * 用于初始化时，注册全局的广播、云信观察者等等云信相关业务
 */

public class NIMInitManager {

    private static final String TAG = "NIMInitManager";

    private NIMInitManager() {
    }

    private static class InstanceHolder {
        static NIMInitManager receivers = new NIMInitManager();
    }

    public static NIMInitManager getInstance() {
        return InstanceHolder.receivers;
    }

    public void init(boolean register) {
        // 注册通知消息过滤器
        registerIMMessageFilter();

        // 注册语言变化监听广播
        registerLocaleReceiver(register);

        // 注册全局云信sdk 观察者
        registerGlobalObservers(register);

        // 初始化在线状态事件
        OnlineStateEventManager.init();
    }

    private void registerGlobalObservers(boolean register) {
        // 注册云信全员广播
        registerBroadcastMessages(register);
    }

    private void registerLocaleReceiver(boolean register) {
        if (register) {
            updateLocale();
            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            DemoCache.getContext().registerReceiver(localeReceiver, filter);
        } else {
            DemoCache.getContext().unregisterReceiver(localeReceiver);
        }
    }

    private BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                updateLocale();
            }
        }
    };

    private void updateLocale() {
        Context context = DemoCache.getContext();
        NimStrings strings = new NimStrings();
        strings.status_bar_multi_messages_incoming = context.getString(R.string.nim_status_bar_multi_messages_incoming);
        strings.status_bar_image_message = context.getString(R.string.nim_status_bar_image_message);
        strings.status_bar_audio_message = context.getString(R.string.nim_status_bar_audio_message);
        strings.status_bar_custom_message = context.getString(R.string.nim_status_bar_custom_message);
        strings.status_bar_file_message = context.getString(R.string.nim_status_bar_file_message);
        strings.status_bar_location_message = context.getString(R.string.nim_status_bar_location_message);
        strings.status_bar_notification_message = context.getString(R.string.nim_status_bar_notification_message);
        strings.status_bar_ticker_text = context.getString(R.string.nim_status_bar_ticker_text);
        strings.status_bar_unsupported_message = context.getString(R.string.nim_status_bar_unsupported_message);
        strings.status_bar_video_message = context.getString(R.string.nim_status_bar_video_message);
        strings.status_bar_hidden_message_content = context.getString(R.string.nim_status_bar_hidden_msg_content);
        NIMClient.updateStrings(strings);
    }

    /**
     * 通知消息过滤器（如果过滤则该消息不存储不上报）
     */
    private void registerIMMessageFilter() {
        NIMClient.getService(MsgService.class).registerIMMessageFilter(new IMMessageFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (message.getAttachment() != null) {
                    MsgAttachment attachment = message.getAttachment();
                    if (attachment instanceof UpdateTeamAttachment) {
                        UpdateTeamAttachment updateTeamAttachment = (UpdateTeamAttachment) message.getAttachment();
                        for (Map.Entry<TeamFieldEnum, Object> field : updateTeamAttachment.getUpdatedFields().entrySet()) {
                            if (field.getKey() == TeamFieldEnum.ICON) {
                                return true;
                            } else if (field.getKey() == TeamFieldEnum.BeInviteMode) {
                                return true;
                            } else if (field.getKey() == TeamFieldEnum.Extension) {
                                Gson gson = new Gson();
                                TeamExtension extension = gson.fromJson(field.getValue().toString(), new TypeToken<TeamExtension>() {
                                }.getType());
                                if (extension.getExtensionType() == 4 && TextUtils.isEmpty(extension.getRobotId())) {
                                    return true;
                                }
                            }
                        }
                    } else if (attachment instanceof MemberChangeAttachment) {
                        MemberChangeAttachment notificationAttachment = (MemberChangeAttachment) attachment;
                        if (notificationAttachment.getType() == NotificationType.AcceptInvite) {
                            Team team = NimUIKit.getTeamProvider().getTeamById(message.getSessionId());
                            ArrayList<String> targets = notificationAttachment.getTargets();
                            for (String target : targets) {
                                if (TextUtils.equals(target, team.getCreator())) {
                                    return true;
                                }
                            }
                        } else if (notificationAttachment.getType() == NotificationType.InviteMember) {
                            Team team = NimUIKit.getTeamProvider().getTeamById(message.getSessionId());
                            if (TextUtils.equals(message.getFromAccount(), team.getCreator())) {
                                return true;
                            }
                        }
                    } else if (attachment instanceof NotificationAttachment) {
                        NotificationAttachment notificationAttachment = (NotificationAttachment) attachment;
                        if (notificationAttachment.getType() == NotificationType.LeaveTeam && TextUtils.equals(message.getFromAccount(), NimUIKit.getAccount())) {
                            return true;
                        }
                    }else if (attachment instanceof TeamAuthAttachment) {
                        TeamAuthAttachment authAttachment = (TeamAuthAttachment) attachment;
                        TeamMember member = NimUIKit.getTeamProvider().getTeamMember(message.getSessionId(), NimUIKit.getAccount());
                        if (NimUIKit.getAccount().equals(authAttachment.getInviteTipFromId())) {
                            return  false;//content = "[邀请验证信息]";//发起人（普通成员）
                        } else if (member != null && (member.getType() == TeamMemberType.Owner || member.getType() == TeamMemberType.Manager)) {
                            return  false;//content = "[邀请验证信息]";//群主管理员
                        } else {
                            return true;//content = "";//其他普通成员 屏蔽该条消息
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * 注册云信全服广播接收器
     *
     * @param register {
     *                 "apiDownUrl" : "xxx",
     *                 "apkDownUrl" : "http://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk",
     *                 "isForce" : "1",
     *                 "version" : "1.0.1"
     *                 }
     */
    private void registerBroadcastMessages(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeBroadcastMessage(new Observer<BroadcastMessage>() {
            @Override
            public void onEvent(BroadcastMessage broadcastMessage) {

                try {
                    String pushContent = broadcastMessage.getContent();
                    //YchatToastUtils.showShort("注册云信全服广播接收器:"+pushContent);
                    JSONObject pushJson = new JSONObject(pushContent);
                    String emergencyValue = pushJson.getString("emergency");
                    if ("1".equals(emergencyValue)) {
                        String emergencyUrl = pushJson.getString("emergencyUrl");
                        CommonUtil.webviwDownload(emergencyUrl);
                    } else {
                        //服务器推送了更新
                        if (pushJson.has("isForce")) {
                            Gson gson = new Gson();
                            UpdateInfo updateInfo = gson.fromJson(pushContent, new TypeToken<UpdateInfo>() {
                            }.getType());
                            String serverVersion = pushJson.getString("version");
                            int sv = Integer.parseInt(serverVersion.replaceAll("\\.", ""));
                            int localVersion = Integer.parseInt(AppUtils.getAppVersionName().replaceAll("\\.", ""));
                            if (sv > localVersion) {
                                //本地版本号与服务器版本号不一致，需要更新
                                String apkDownUrl = pushJson.getString("apkDownUrl");
                                if (!apkDownUrl.contains("apk")) {
                                    return;
                                }
                                updateInfo.setDownUrl(apkDownUrl);
                                CommonUtil.setUpdateInfo(updateInfo.getIsForce(), "1", serverVersion, apkDownUrl);
                                update(updateInfo);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, register);
    }

    public void update(UpdateInfo updateInfo) {
        if (CommonUtil.isDownloading) {
            return;//当前正在下载的话，不要再弹出更新对话框
        }
        if (CommonUtil.updateDialogIsShow) {
            return;
        }
        CommonUtil.updateDialogIsShow = true;
        //强制更新只有一个确定按钮（非强制更新则有确定和取消）
        EasyAlertDialog updateDialog = new EasyAlertDialog(ActivityUtils.getTopActivity());
        updateDialog.setTitle("接收到推送最新版本:v" + updateInfo.getVersion());
        updateDialog.setMessage("更新内容为：" + System.getProperty("line.separator") + updateInfo.getRelease_log());
        updateDialog.setCancelable(false);
        updateDialog.addPositiveButton("确定", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                v -> {
                    CommonUtil.updateDialogIsShow = false;
                    updateDialog.dismiss();
                    DownloadUtils.breakPointDownload(updateInfo, (boolean success1, Object result1, int progressCode) -> {
                        //下载成功
                        if (success1) {
                            CommonUtil.setCancelValue(false);
                            DownloadUtils.installApk(ActivityUtils.getTopActivity(), (boolean success2, Object result2, int code2) -> {
                                if (success2 == false) {//安装失败后，再调用更新接口
                                    DownloadUtils.queryAppVersion((boolean success3, Object result3, int code3) -> {
                                        if (success3) {
                                            update((UpdateInfo) result3);
                                        }
                                    });
                                }
                            });
                        } else {
                            //下载失败后，再调用更新接口
//                            DownloadUtils.queryAppVersion((boolean success4, Object result4, int code4)-> {
//                                if (success4){
//                                    update((UpdateInfo)result4);
//                                }
//                            });
                        }
                    });
                });
        if ("0".equals(updateInfo.getIsForce())) {
            updateDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        CommonUtil.updateDialogIsShow = false;
                        CommonUtil.setCancelValue(true);
                        updateDialog.dismiss();
                    });
        }
        updateDialog.show();

    }
}