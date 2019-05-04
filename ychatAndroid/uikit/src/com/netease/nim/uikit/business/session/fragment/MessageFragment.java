package com.netease.nim.uikit.business.session.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.blankj.utilcode.util.CacheMemoryUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.main.CustomPushContentProvider;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.ait.AitManager;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.actions.ImageAction;
import com.netease.nim.uikit.business.session.actions.LocationAction;
import com.netease.nim.uikit.business.session.actions.VideoAction;
import com.netease.nim.uikit.business.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.business.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.helper.ScreenShotListenManager;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.business.session.module.input.InputPanel;
import com.netease.nim.uikit.business.session.module.list.MessageListPanelEx;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.UnclaimedEnvelope;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.robot.model.RobotMsgType;
import com.netease.nimlib.sdk.team.model.Team;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 聊天界面基类
 * <p/>
 * Created by huangjun on 2015/2/1.
 */
public class MessageFragment extends TFragment implements ModuleProxy {

    private View rootView;

    private SessionCustomization customization;

    public static final String NEW_MESSAGE = "NewMessage";

    // p2p对方Account或者群id
    protected String sessionId;

    protected SessionTypeEnum sessionType;

    // modules
    public InputPanel inputPanel;
    protected MessageListPanelEx messageListPanel;

    protected AitManager aitManager;
    public TextView txAit;
    private int unReadCount = 0;
    private ScreenShotListenManager manager;

    private LocalBroadcastManager localBroadcastManager;
    private NewMessageBroadcastReceiver receiver;

    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(NEW_MESSAGE)) {
                IMMessage message = (IMMessage) intent.getSerializableExtra(NEW_MESSAGE);
                sendMessage(message);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseIntent();
        manager = ScreenShotListenManager.newInstance(getActivity());
        manager.setListener(imagePath -> {
            Team team = NimUIKit.getTeamProvider().getTeamById(sessionId);
            if (!TextUtils.isEmpty(team.getExtension())) {
                try {
                    Gson gson = new Gson();
                    TeamExtension extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                    }.getType());
                    if (TeamExtras.OPEN.equals(extension.getScreenshotNotify())) {
                        Object ins = null;
                        try {
                            Class tchClass = Class.forName("com.xr.ychat.session.extension.ScreenCaptureAttachment");
                            Method method1 = tchClass.getMethod("setCustomTipId", String.class);//得到方法对象
                            Method method2 = tchClass.getMethod("setCustomTipContent", String.class);//得到方法对象
                            ins = tchClass.newInstance();
                            method1.invoke(ins, NimUIKit.getAccount());//调用创建高级群方法
                            method2.invoke(ins, "在聊天中截屏了");//调用创建高级群方法
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        IMMessage msg = MessageBuilder.createCustomMessage(sessionId, sessionType, "截屏", (MsgAttachment) ins);
                        CustomMessageConfig config = new CustomMessageConfig();
                        config.enablePush = false; // 不推送
                        msg.setConfig(config);
                        sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        manager.startListen();
        if (sessionType == SessionTypeEnum.Team) {
            localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
            receiver = new NewMessageBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.xr.ychat.NewMessageBroadcastReceiver");
            localBroadcastManager.registerReceiver(receiver, intentFilter);
        }
    }

    public AitManager getAitManager() {
        return aitManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.nim_message_fragment, container, false);
        txAit = rootView.findViewById(R.id.tx_ait);

        int w1 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h1 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        txAit.measure(w1, h1);
        //有人@你了或者是查看未读新消息 两者共用一个。
        txAit.setOnClickListener(v -> {
            leftToRightAnim();
            int currentAdapterSize = messageListPanel.adapter.getDataSize();//adapter默认加载数据20条
            if (atMeIndex != -1) {
                //用户在外面会话列表，有人@你的情况
                messageListPanel.messageListView.smoothScrollToPosition(currentAdapterSize - atMeIndex);
                atMeIndex = -1;
                CacheMemoryUtils.getInstance().put(sessionId, "");//把@缓存数据清空
            } else if (unReadCount >= 10) {
                //未读新消息，往上滚动，完成
                messageListPanel.messageListView.smoothScrollToPosition(currentAdapterSize - unReadCount);
                unReadCount = 0;
            }

        });
        return rootView;
    }

    /**
     * ***************************** life cycle *******************************
     */

    @Override
    public void onPause() {
        super.onPause();

        NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        inputPanel.onPause();
        messageListPanel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        messageListPanel.onResume();
        NIMClient.getService(MsgService.class).setChattingAccount(sessionId, sessionType);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sessionType == SessionTypeEnum.Team) {
            localBroadcastManager.unregisterReceiver(receiver);
        }
        messageListPanel.onDestroy();
        registerObservers(false);
        if (inputPanel != null) {
            inputPanel.onDestroy();
        }
        if (aitManager != null) {
            aitManager.reset();
        }
        manager.stopListen();
    }

    public boolean onBackPressed() {
        return inputPanel.collapse(true) || messageListPanel.onBackPressed();
    }

    public void refreshMessageList() {
        messageListPanel.refreshMessageList();
    }


    /**
     * 从右到左的动画（出现：从屏幕外滑动进来）
     *
     */
    public void rightToLeftAnim() {
        ViewPropertyAnimator viewPropertyAnimator = txAit.animate();
        viewPropertyAnimator.setInterpolator(new AccelerateInterpolator());
        viewPropertyAnimator.setDuration(600);
        viewPropertyAnimator.translationX(0);
    }

    /**
     * 从左到右的动画(消失：滑出屏幕外面)
     *
     */
    public void leftToRightAnim() {

        ViewPropertyAnimator viewPropertyAnimator = txAit.animate();
        viewPropertyAnimator.setDuration(600);
        viewPropertyAnimator.translationX(txAit.getMeasuredWidth());


    }
    private void displayUnreadView(){
        if (unReadCount >= 10) {
            rightToLeftAnim();
            if (unReadCount > 99) {
                txAit.setText("99+" + "条新消息");
            } else {
                txAit.setText(unReadCount + "条新消息");
            }
        }
    }
    private void parseIntent() {
        Bundle arguments = getArguments();
        sessionId = arguments.getString(Extras.EXTRA_ACCOUNT);
        sessionType = (SessionTypeEnum) arguments.getSerializable(Extras.EXTRA_TYPE);
        if (sessionType == SessionTypeEnum.P2P) {
            RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.P2P);
            unReadCount = (recentContact != null ? recentContact.getUnreadCount() : 0);
        } else if (sessionType == SessionTypeEnum.Team) {
            RecentContact recentContact2 = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.Team);
            unReadCount = (recentContact2 != null ? recentContact2.getUnreadCount() : 0);
        }
        //设置默认加载多少条消息记录
        if (unReadCount > 20) {
            NimUIKitImpl.getOptions().messageCountLoadOnce = unReadCount;
        } else {
            NimUIKitImpl.getOptions().messageCountLoadOnce = 20;
        }

        txAit.setX(txAit.getMeasuredWidth());
        //@的逻辑
        if (sessionType == SessionTypeEnum.Team) {
            RecentContact recentContact2 = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.Team);
            if (recentContact2 != null) {
                //群聊界面点击进来为Null
                unReadCount = recentContact2.getUnreadCount();
                //在首页缓存的@我的消息
                String cachedAccountIdWithContent = CacheMemoryUtils.getInstance().get(recentContact2.getContactId());//取出缓存有@我的消息块，key:teamId  value:发送者账号+消息内容
                // Log.e("xx","cached atme="+cachedAccountIdWithContent);//cached atme=10000049@一路向北 吃饭了
                if (!TextUtils.isEmpty(cachedAccountIdWithContent)) {
                    if (unReadCount <= 20) {
                        NimUIKitImpl.getOptions().messageCountLoadOnce = 20;
                    } else {
                        NimUIKitImpl.getOptions().messageCountLoadOnce = unReadCount;//@消息的优先级要高一点
                    }

                    //说明有@我的消息
                    // List<IMMessage> curentMsgList = messageListPanel.adapter.getData();
                    NIMClient.getService(MsgService.class).queryMessageList(sessionId, sessionType, 0, unReadCount).setCallback(new RequestCallback<List<IMMessage>>() {
                        @Override
                        public void onSuccess(List<IMMessage> curentMsgList) {

                            for (IMMessage imMessage : curentMsgList) {
                                atMeIndex++;
                                String dbContent = imMessage.getFromAccount() + imMessage.getContent();
                                if (cachedAccountIdWithContent.equals(dbContent)) {
                                    break;
                                }
                            }
                            if (atMeIndex != -1) {
                                txAit.setText(getString(R.string.at_you));//有人@你了
                                rightToLeftAnim();//有人@你了滑出来
                            }
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });

                } else {
                   displayUnreadView();
                }
            }

        }else{
            //A给B发送多条消息,显示x条新消息
            displayUnreadView();
        }


        IMMessage anchor = (IMMessage) arguments.getSerializable(Extras.EXTRA_ANCHOR);

        customization = (SessionCustomization) arguments.getSerializable(Extras.EXTRA_CUSTOMIZATION);
        Container container = new Container(getActivity(), sessionId, sessionType, this);

        if (messageListPanel == null) {
            messageListPanel = new MessageListPanelEx(container, rootView, anchor, false, false);
        } else {
            messageListPanel.reload(container, anchor);
        }
        messageListPanel.setSimpleCallback(new SimpleCallback() {
            @Override
            public void onResult(boolean success, Object result, int code) {
                String className = getActivity().getClass().getSimpleName();
                switch (result.toString()) {
                    case "切换成听筒播放":
                        Drawable drawable = getResources().getDrawable(R.drawable.ear_small);
                        if ("P2PMessageActivity".equals(className)) {
                            ((P2PMessageActivity) getActivity()).toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                        } else {
                            ((TeamMessageActivity) getActivity()).toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                        }
                        break;
                    case "切换成扬声器播放":
                        if ("P2PMessageActivity".equals(className)) {
                            ((P2PMessageActivity) getActivity()).toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        } else {
                            ((TeamMessageActivity) getActivity()).toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        }
                        break;
                }
            }
        });

        if (inputPanel == null) {
            inputPanel = new InputPanel(container, rootView, getActionList());
            inputPanel.setCustomization(customization);
        } else {
            inputPanel.reload(container, customization);
        }

        initAitManager();

        inputPanel.switchRobotMode(NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(sessionId) != null);

        registerObservers(true);

        if (customization != null) {
            messageListPanel.setChattingBackground(customization.backgroundUri, customization.backgroundColor);
        }


    }

    private int atMeIndex = -1;

    private void initAitManager() {
        UIKitOptions options = NimUIKitImpl.getOptions();
        if (options.aitEnable) {
            aitManager = new AitManager(getContext(), options.aitTeamMember && sessionType == SessionTypeEnum.Team ? sessionId : null, options.aitIMRobot);
            inputPanel.addAitTextWatcher(aitManager);
            aitManager.setTextChangeListener(inputPanel);
        }

    }

    /**
     * ************************* 消息收发 **********************************
     */
    // 是否允许发送消息
    protected boolean isAllowSendMessage(final IMMessage message) {
        return customization.isAllowSendMessage(message);
    }


    private void registerObservers(boolean register) {
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        service.observeReceiveMessage(incomingMessageObserver, register);
        // 已读回执监听
        if (NimUIKitImpl.getOptions().shouldHandleReceipt) {
            service.observeMessageReceipt(messageReceiptObserver, register);
        }
    }

    /**
     * 消息接收观察者
     */
    Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            onMessageIncoming(messages);
        }
    };

    private void onMessageIncoming(List<IMMessage> messages) {
        if (CommonUtil.isEmpty(messages)) {
            return;
        }
        messageListPanel.onIncomingMessage(messages);
        // 发送已读回执
        messageListPanel.sendReceipt();
    }

    /**
     * 已读回执观察者
     */
    private Observer<List<MessageReceipt>> messageReceiptObserver = new Observer<List<MessageReceipt>>() {
        @Override
        public void onEvent(List<MessageReceipt> messageReceipts) {
            messageListPanel.receiveReceipt();
        }
    };


    /**
     * ********************** implements ModuleProxy *********************
     */
    @Override
    public boolean sendMessage(IMMessage message) {
        if (isAllowSendMessage(message)) {
            appendTeamMemberPush(message);
            message = changeToRobotMsg(message);
            final IMMessage msg = message;
            appendPushConfig(message);
            // send message to server and save to db
            NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {

                }

                @Override
                public void onFailed(int code) {
                    sendFailWithBlackList(code, msg);
                }

                @Override
                public void onException(Throwable exception) {

                }
            });
            messageListPanel.onMsgSend(message);
        } else {
            // 替换成tip
//            ToastUtils.showShort("该消息无法发送");
//            message = MessageBuilder.createTipMessage(message.getSessionId(), message.getSessionType());
//            message.setContent("该消息无法发送");
//            message.setStatus(MsgStatusEnum.success);
//            NIMClient.getService(MsgService.class).saveMessageToLocal(message, false);
        }

        if (aitManager != null) {
            aitManager.reset();
        }
        return true;
    }

    // 被对方拉入黑名单后，发消息失败的交互处理
    private void sendFailWithBlackList(int code, IMMessage msg) {
        if (code == ResponseCode.RES_IN_BLACK_LIST) {
            // 如果被对方拉入黑名单，发送的消息前不显示重发红点
            msg.setStatus(MsgStatusEnum.success);
            NIMClient.getService(MsgService.class).updateIMMessageStatus(msg);
            messageListPanel.refreshMessageList();
            // 同时，本地插入被对方拒收的tip消息
            IMMessage tip = MessageBuilder.createTipMessage(msg.getSessionId(), msg.getSessionType());
            tip.setContent(getActivity().getString(R.string.black_list_send_tip));
            tip.setStatus(MsgStatusEnum.success);
            CustomMessageConfig config = new CustomMessageConfig();
            config.enableUnreadCount = false;
            tip.setConfig(config);
            NIMClient.getService(MsgService.class).saveMessageToLocal(tip, true);
        }
    }

    private void appendTeamMemberPush(IMMessage message) {
        if (aitManager == null) {
            return;
        }
        if (sessionType == SessionTypeEnum.Team) {
            List<String> pushList = aitManager.getAitTeamMember();
            if (pushList == null || pushList.isEmpty()) {
                return;
            }
            MemberPushOption memberPushOption = new MemberPushOption();
            memberPushOption.setForcePush(true);
            memberPushOption.setForcePushContent(message.getContent());
            memberPushOption.setForcePushList(pushList);
            message.setMemberPushOption(memberPushOption);
        }
    }

    private IMMessage changeToRobotMsg(IMMessage message) {
        if (aitManager == null) {
            return message;
        }
        if (message.getMsgType() == MsgTypeEnum.robot) {
            return message;
        }
        if (isChatWithRobot()) {
            if (message.getMsgType() == MsgTypeEnum.text && message.getContent() != null) {
                String content = message.getContent().equals("") ? " " : message.getContent();
                message = MessageBuilder.createRobotMessage(message.getSessionId(), message.getSessionType(), message.getSessionId(), content, RobotMsgType.TEXT, content, null, null);
            }
        } else {
            String robotAccount = aitManager.getAitRobot();
            if (TextUtils.isEmpty(robotAccount)) {
                return message;
            }
            String text = message.getContent();
            String content = aitManager.removeRobotAitString(text, robotAccount);
            content = content.equals("") ? " " : content;
            message = MessageBuilder.createRobotMessage(message.getSessionId(), message.getSessionType(), robotAccount, text, RobotMsgType.TEXT, content, null, null);

        }
        return message;
    }

    private boolean isChatWithRobot() {
        return NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(sessionId) != null;
    }

    private void appendPushConfig(IMMessage message) {
        CustomPushContentProvider customConfig = NimUIKitImpl.getCustomPushContentProvider();
        if (customConfig == null) {
            return;
        }
        String content = customConfig.getPushContent(message);
        Map<String, Object> payload = customConfig.getPushPayload(message);
        if (!TextUtils.isEmpty(content)) {
            message.setPushContent(content);
        }
        if (payload != null) {
            message.setPushPayload(payload);
        }

    }

    @Override
    public void onInputPanelExpand() {
        messageListPanel.scrollToBottom();
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
    }

    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    @Override
    public void onItemFooterClick(IMMessage message) {
        if (aitManager == null) {
            return;
        }
        if (messageListPanel.isSessionMode()) {
            RobotAttachment attachment = (RobotAttachment) message.getAttachment();
            NimRobotInfo robot = NimUIKitImpl.getRobotInfoProvider().getRobotByAccount(attachment.getFromRobotAccount());
            aitManager.insertAitRobot(robot.getAccount(), robot.getName(), inputPanel.getEditSelectionStart());
        }
    }

    @Override
    public void onUnclaimedEnvelopeClick(UnclaimedEnvelope message) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (aitManager != null) {
            aitManager.onActivityResult(requestCode, resultCode, data);
        }
        inputPanel.onActivityResult(requestCode, resultCode, data);
        messageListPanel.onActivityResult(requestCode, resultCode, data);
    }

    // 操作面板集合
    protected List<BaseAction> getActionList() {
        List<BaseAction> actions = new ArrayList<>();
        actions.add(new ImageAction());
        actions.add(new VideoAction());
        actions.add(new LocationAction());


        if (customization != null && customization.actions != null) {
            actions.addAll(customization.actions);
        }
        return actions;
    }

    public void reload(IMMessage anchor) {
        if (messageListPanel != null) {
            messageListPanel.scrollToPosition(anchor);
        }
    }
}
