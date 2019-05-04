package com.xr.ychat.redpacket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.session.helper.MessageListPanelHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.session.extension.RedPacketAttachment;

public class RedpactDetailActivity extends SwipeBackUI {
    private static final String FROM_ACCOUNT = "FromAccount";
    private static final String FROM_CONTENT = "FromContent";
    private static final String BRIBERY_ID = "BriberyId";
    private static final String UID = "Uid";
    private static final String MY_TOKEN = "MyToken";
    private static final String NICK_NAME = "NickName";
    private static final String MESSAGE = "Message";
    private TextView redpacketRecord;
    private String briberyId;
    private String uid;
    private String mytoken;
    private String fromAccount;
    private String fromContent;
    private IMMessage message;
    //红包发送者头像，名字
    private HeadImageView fromAvatar;
    private TextView fromName;
    //红包附加信息
    private TextView redpacketContent;
    //红包金额以及支付宝账号
    private LinearLayout redpacketAlipayLayout;
    private TextView redpacketAlipayMoney;
    private TextView redpacketAlipayExplain;
    private TextView redpacketAlipayAccount;
    //红包个数或者红包过期提示
    private TextView redpacketRecipientAmount;
    //红包领取者信息
    private ConstraintLayout redpacketRecipientLayout;
    private HeadImageView redpacketRecipientAvatar;
    private TextView redpacketRecipientName;
    private TextView redpacketRecipientTime;
    private TextView redpacketRecipientMoney;
    private TextView redpacketDetailTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket_detail);
        initIntent();
        initToolbar();
        initView();
        queryRedpacketStatus(uid, mytoken, briberyId);
    }

    private void initView() {
        fromAvatar = (HeadImageView) findViewById(R.id.redpacket_detail_avatar);
        fromAvatar.loadBuddyAvatar(fromAccount);
        fromName = (TextView) findViewById(R.id.redpacket_detail_name);
        NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(fromAccount);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(fromAccount, new SimpleCallback<NimUserInfo>() {
                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        fromName.setText(result.getName());
                    }
                }
            });
        } else {
            fromName.setText(userInfo.getName());
        }
        redpacketContent = (TextView) findViewById(R.id.redpacket_detail_content);
        redpacketContent.setText(fromContent);
        redpacketAlipayLayout = (LinearLayout) findViewById(R.id.redpacket_detail_alipay);
        redpacketAlipayMoney = (TextView) findViewById(R.id.redpacket_detail_money);
        redpacketAlipayExplain = (TextView) findViewById(R.id.redpacket_detail_explain);
        redpacketAlipayAccount = (TextView) findViewById(R.id.redpacket_detail_account);
        redpacketDetailTips = (TextView) findViewById(R.id.redpacket_detail_tips);
        redpacketRecipientAmount = (TextView) findViewById(R.id.recipient_amount);
        redpacketRecipientLayout = (ConstraintLayout) findViewById(R.id.recipient);
        redpacketRecipientAvatar = (HeadImageView) findViewById(R.id.recipient_avatar);
        redpacketRecipientName = (TextView) findViewById(R.id.recipient_name);
        redpacketRecipientTime = (TextView) findViewById(R.id.recipient_time);
        redpacketRecipientMoney = (TextView) findViewById(R.id.recipient_money);
    }

    private void initIntent() {
        Intent intent = getIntent();
        fromAccount = intent.getStringExtra(FROM_ACCOUNT);
        fromContent = intent.getStringExtra(FROM_CONTENT);
        briberyId = intent.getStringExtra(BRIBERY_ID);
        uid = intent.getStringExtra(UID);
        mytoken = intent.getStringExtra(MY_TOKEN);
        message = (IMMessage) intent.getSerializableExtra(MESSAGE);
    }

    private void initToolbar() {
        redpacketRecord = (TextView) findViewById(R.id.redpacket_detail_record);
        redpacketRecord.setOnClickListener(v -> {
            if (!NetworkUtil.isNetAvailable(RedpactDetailActivity.this)) {
                YchatToastUtils.showShort(R.string.network_is_not_available);
                return;
            }
            DialogMaker.showProgressDialog(RedpactDetailActivity.this, "", false);
            ContactHttpClient.getInstance().queryAlipayAccount(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo aVoid) {
                    DialogMaker.dismissProgressDialog();
                    if (TextUtils.isEmpty(aVoid.getAliuid())) {
                        BindAlipayActivity.start(RedpactDetailActivity.this);
                    } else {
                        RedpactRecordActivity.start(RedpactDetailActivity.this);
                    }
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    DialogMaker.dismissProgressDialog();
                }
            });
        });
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * 红包状态查询
     */
    private void queryRedpacketStatus(String uid, String mytoken, String orderno) {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.getInstance().queryRedpacketStatus(uid, mytoken, orderno, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                if (aVoid != null) {
                    switch (aVoid.getStatus()) {
                        case 3: {
                            redpacketAlipayLayout.setVisibility(View.GONE);
                            redpacketRecipientLayout.setVisibility(View.GONE);
                            redpacketRecipientAmount.setText(String.format("红包金额%1$.2f元，等待领取", aVoid.getAmount()));
                            redpacketDetailTips.setVisibility(View.VISIBLE);
                        }
                        break;
                        case 4: {
                            if (message != null) {
                                RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                                attachment.setRpType(1);
                                NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                                MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                            }
                            redpacketAlipayLayout.setVisibility(View.VISIBLE);
                            redpacketAlipayExplain.setText("金额已存入你授权的支付宝账户");
                            redpacketAlipayAccount.setText(aVoid.getToAliLogonId());
                            redpacketAlipayMoney.setText(String.valueOf(aVoid.getAmount()));
                            boolean isUser = TextUtils.equals(DemoCache.getAccount(), aVoid.getToUID());
                            redpacketAlipayAccount.setVisibility(isUser ? View.VISIBLE : View.GONE);
                            redpacketAlipayExplain.setVisibility(isUser ? View.VISIBLE : View.GONE);
                            redpacketRecipientLayout.setVisibility(View.VISIBLE);
                            redpacketRecipientAmount.setText(String.format("1个红包共%1$.2f元", aVoid.getAmount()));
                            redpacketRecipientMoney.setText(String.format("%1$.2f元", aVoid.getAmount()));
                            redpacketRecipientTime.setText(TimeUtils.millis2String(aVoid.getnDisTime() * 1000));
                            redpacketRecipientAvatar.loadBuddyAvatar(aVoid.getToUID());
                            NimUIKit.getUserInfoProvider().getUserInfoAsync(aVoid.getToUID(), new SimpleCallback<NimUserInfo>() {

                                @Override
                                public void onResult(boolean success, NimUserInfo result, int code) {
                                    if (success && result != null) {
                                        redpacketRecipientName.setText(result.getName());
                                    }
                                }
                            });
                            redpacketDetailTips.setVisibility(View.GONE);
                        }
                        break;
                        case 5: {
                            RedPacketAttachment attachment = (RedPacketAttachment) message.getAttachment();
                            attachment.setRpType(2);
                            NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                            MessageListPanelHelper.getInstance().notifyUpdateMessage(message);
                            redpacketAlipayLayout.setVisibility(View.GONE);
                            redpacketRecipientLayout.setVisibility(View.GONE);
                            redpacketRecipientAmount.setText(String.format("该红包已过期，红包金额%1$.2f元", aVoid.getAmount()));
                            redpacketDetailTips.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    public static void start(Activity context, String fromAccount, String fromContent, String briberyId, String uid, String mytoken, String nickname, IMMessage message) {
        Intent intent = new Intent(context, RedpactDetailActivity.class);
        intent.putExtra(FROM_ACCOUNT, fromAccount);
        intent.putExtra(FROM_CONTENT, fromContent);
        intent.putExtra(BRIBERY_ID, briberyId);
        intent.putExtra(UID, uid);
        intent.putExtra(MY_TOKEN, mytoken);
        intent.putExtra(NICK_NAME, nickname);
        intent.putExtra(MESSAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

}
