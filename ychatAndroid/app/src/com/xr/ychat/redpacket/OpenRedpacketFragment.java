package com.xr.ychat.redpacket;

import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.session.extension.RedPacketAttachment;
import com.netease.nim.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.UnclaimedEnvelope;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;

public class OpenRedpacketFragment extends DialogFragment implements View.OnClickListener {
    private HeadImageView fromAvatar;
    private TextView fromName;
    private TextView redpacketContent;
    private ConstraintLayout openRedpacketLayout;
    private TextView redpacketMoney;
    private TextView redpacketDetail;
    private ImageView openRedpacket;
    private AnimationDrawable animationDrawable;
    private NIMOpenRpCallback callback;
    private IMMessage message;
    private String briberyId;
    private String fromAccount;
    private String fromContent;
    private String sessionId;
    private String uid;
    private String mytoken;
    private String nickname;
    private int type;
    private int status;
    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 400;  // 快速点击间隔

    public void show(FragmentManager fragmentManager, NIMOpenRpCallback callback, IMMessage message) {
        try {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(this, "OpenRedpacketFragment");
            ft.commit();
        } catch (IllegalStateException ignore) {

        }
        Bundle bundle = getArguments();
        this.sessionId = bundle.getString("SessionId");
        this.fromAccount = bundle.getString("FromAccount");
        this.fromContent = bundle.getString("FromContent");
        this.briberyId = bundle.getString("BriberyId");
        this.type = bundle.getInt("Type", 1);
        this.status = bundle.getInt("Status", 0);
        this.callback = callback;
        this.message = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.activity_open_redpacket, null);
        fromAvatar = view.findViewById(R.id.open_redpacket_avatar);
        fromName = view.findViewById(R.id.open_redpacket_name);
        redpacketContent = view.findViewById(R.id.open_redpacket_content);
        redpacketContent.setVisibility(View.VISIBLE);
        openRedpacketLayout = view.findViewById(R.id.open_redpacket_layout);
        openRedpacketLayout.setVisibility(View.GONE);
        redpacketMoney = view.findViewById(R.id.open_redpacket_money);
        openRedpacket = view.findViewById(R.id.open_redpacket);
        redpacketDetail = view.findViewById(R.id.open_redpacket_detail);
        redpacketDetail.setOnClickListener(this);
        redpacketDetail.setVisibility(View.INVISIBLE);
        openRedpacket.setOnClickListener(this);
        openRedpacket.setVisibility(View.GONE);
        view.findViewById(R.id.open_redpacket_close).setOnClickListener(this);
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(colorDrawable);
        return dialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mytoken = Preferences.getWeiranToken(getActivity());
        uid = Preferences.getWeiranUid(getActivity());
        NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(NimUIKit.getAccount());
        nickname = userInfo.getName();
        NimUserInfo fromUserInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(fromAccount);
        if (fromUserInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(fromAccount, new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        fromName.setText(result.getName());
                        fromAvatar.loadAvatar(result.getAvatar());
                    }
                }
            });
        } else {
            fromName.setText(fromUserInfo.getName());
            fromAvatar.loadAvatar(fromUserInfo.getAvatar());
        }
        if (status == 3) {
            redpacketContent.setVisibility(View.VISIBLE);
            redpacketContent.setText("你无法领取该红包");
            openRedpacketLayout.setVisibility(View.GONE);
            openRedpacket.setVisibility(View.GONE);
            redpacketDetail.setVisibility(View.VISIBLE);
        } else {
            if (!TextUtils.isEmpty(briberyId)) {
                queryRedpacketStatus(uid, mytoken, briberyId);
            } else {
                dismiss();
            }
        }
    }

    private void queryRedpacketStatus(String uid, String mytoken, String orderno) {
        if (!NetworkUtil.isNetAvailable(getActivity())) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.getInstance().queryRedpacketStatus(uid, mytoken, orderno, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                if (aVoid != null) {
                    switch (aVoid.getStatus()) {
                        case 0:
                        case 3: {
                            redpacketContent.setText(fromContent);
                            openRedpacket.setVisibility(View.VISIBLE);
                            if (TextUtils.equals(aVoid.getFromUID(), NimUIKit.getAccount())) {
                                redpacketDetail.setVisibility(View.VISIBLE);
                            } else {
                                redpacketDetail.setVisibility(View.INVISIBLE);
                            }
                        }
                        break;
                        case 4: {
                            redpacketDetail.setVisibility(View.VISIBLE);
                            int redpacketStatus;
                            if (!TextUtils.equals(DemoCache.getAccount(), aVoid.getToUID())) {
                                redpacketContent.setText("你的手慢了，红包已被领取");
                                redpacketStatus = 4;
                            } else {
                                redpacketContent.setVisibility(View.GONE);
                                openRedpacketLayout.setVisibility(View.VISIBLE);
                                redpacketMoney.setText(String.valueOf(aVoid.getAmount()));
                                redpacketStatus = 1;
                            }
                            if (callback != null) {
                                callback.sendUnclaimedEnvelope(buildUnclaimedEnvelope(redpacketStatus));
                            }
                            if (message != null) {
                                RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                                attachment.setRpType(redpacketStatus);
                                NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                                MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                            }
                        }
                        break;
                        case 5: {
                            redpacketDetail.setVisibility(View.VISIBLE);
                            redpacketContent.setText("红包超过48小时，无法领取");
                            if (callback != null) {
                                callback.sendUnclaimedEnvelope(buildUnclaimedEnvelope(2));
                            }
                            if (message != null) {
                                RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                                attachment.setRpType(2);
                                NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                                MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                YchatToastUtils.showShort("网络繁忙，请重试");
            }
        });
    }

    private void receiveRedpacket(String uid, String mytoken, String orderno, String nickname) {
        if (!NetworkUtil.isNetAvailable(getActivity())) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.ContactHttpCallback contactHttpCallback = new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                resetAnimation();
                if (aVoid.getCode() == 0 || aVoid.getCode() == 100039) {
                    redpacketDetail.setVisibility(View.VISIBLE);
                    redpacketContent.setVisibility(View.GONE);
                    openRedpacketLayout.setVisibility(View.VISIBLE);
                    redpacketMoney.setText(String.valueOf(aVoid.getAmount()));
                    openRedpacket.setVisibility(View.GONE);
                    if (callback != null && aVoid.getCode() == 0) {
                        callback.sendMessage(DemoCache.getAccount(), briberyId, true);
                        callback.sendUnclaimedEnvelope(buildUnclaimedEnvelope(1));
                    }
                    if (message != null) {
                        RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                        attachment.setRpType(1);
                        NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                        MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                    }
                } else if (aVoid.getCode() == 100020) {
                    redpacketDetail.setVisibility(View.VISIBLE);
                    redpacketContent.setVisibility(View.VISIBLE);
                    redpacketContent.setText("你的手慢了，红包已被领取");
                    openRedpacketLayout.setVisibility(View.GONE);
                    openRedpacket.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.sendUnclaimedEnvelope(buildUnclaimedEnvelope(4));
                    }
                    if (message != null) {
                        RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                        attachment.setRpType(4);
                        NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                        MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                    }
                } else {
                    redpacketDetail.setVisibility(View.VISIBLE);
                    redpacketContent.setVisibility(View.VISIBLE);
                    redpacketContent.setText("你无法领取该红包");
                    openRedpacketLayout.setVisibility(View.GONE);
                    openRedpacket.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.sendUnclaimedEnvelope(buildUnclaimedEnvelope(3));
                    }
                    if (message != null) {
                        RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                        attachment.setRpType(3);
                        NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                        MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                    }
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                resetAnimation();
                redpacketDetail.setVisibility(View.VISIBLE);
                redpacketContent.setVisibility(View.VISIBLE);
                openRedpacketLayout.setVisibility(View.GONE);
                openRedpacket.setVisibility(View.GONE);
                if (callback != null) {
                    callback.sendUnclaimedEnvelope(buildUnclaimedEnvelope(4));
                }
                if (code == 100028) {
                    redpacketContent.setText("本群不允许您收红包");
                } else if (code == 100018) {
                    redpacketContent.setText("红包领取错误");
                } else if (code == 100021) {
                    redpacketContent.setText("你无法领取该红包");
                    if (message != null) {
                        RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                        attachment.setRpType(3);
                        NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                        MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                    }
                } else {
                    redpacketContent.setText("");
                    YchatToastUtils.showShort("网络繁忙，领取红包失败");
                    dismiss();
                }
            }
        };
        if (type == 1) {
            ContactHttpClient.getInstance().receiveRedpacket(uid, mytoken, orderno, nickname, contactHttpCallback);
        } else if (type == 2) {
            ContactHttpClient.getInstance().receiveRedpacket(uid, mytoken, orderno, nickname, sessionId, TeamHelper.getTeamName(sessionId), contactHttpCallback);
        }
    }

    private void resetAnimation() {
        openRedpacket.setEnabled(true);
        if (animationDrawable != null) {
            animationDrawable.stop();
            animationDrawable = null;
        }
        openRedpacket.setImageResource(R.drawable.kai_0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_redpacket: {
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
                    return;
                }
                lastClickTime = System.currentTimeMillis();
                openRedpacket.setImageResource(R.drawable.open_redpacket_amimation);
                animationDrawable = (AnimationDrawable) openRedpacket.getDrawable();
                animationDrawable.start();
                openRedpacket.setEnabled(false);
                receiveRedpacket(uid, mytoken, briberyId, nickname);
            }
            break;
            case R.id.open_redpacket_close: {
                dismiss();
            }
            break;
            case R.id.open_redpacket_detail: {
                dismiss();
                RedpactDetailActivity.start(getActivity(), fromAccount, fromContent, briberyId, uid, mytoken, nickname, message);
            }
            break;
        }
    }

    private UnclaimedEnvelope buildUnclaimedEnvelope(int type) {
        UnclaimedEnvelope unclaimedEnvelope = new UnclaimedEnvelope();
        unclaimedEnvelope.setFromid(fromAccount);
        unclaimedEnvelope.setContent(fromContent);
        unclaimedEnvelope.setOrderno(briberyId);
        unclaimedEnvelope.setType(type);
        return unclaimedEnvelope;
    }
}
