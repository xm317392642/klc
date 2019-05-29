package com.xr.ychat.redpacket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.blankj.utilcode.util.LogUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.UnsentRedPacket;
import com.netease.nim.uikit.common.UnsentRedPacketCache;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.string.StringTextWatcher;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class SendFlockRedpactActivity extends SwipeBackUI {
    private static final int SDK_PAY_FLAG = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;
    private static final int REQUEST_CODE_DETAIL = 101;
    private static final String TARGETID = "target_id";
    private static final String TARGETNAME = "target_name";
    private static final String TARGETROBOT = "target_robot";
    private static final String TARGETMASTER = "target_master";
    private static final String ORDERINFO = "order_info";
    private TextView moneyTips;
    private EditText inputMoney;
    private EditText inputMessage;
    private TextView redpacketType;
    private TextView redpacketChange;
    private ConstraintLayout appointLayoput;
    private TextView redpacketAppoint;
    private Button send;
    private String uid;
    private String mytoken;
    private String nickname;
    private String envelopeMessage;
    private float sendAmount;
    private int type = 3;
    private String appoint;
    private String teamId;
    private String teamName;
    private String robotId;
    private String masterUID;
    private ArrayList<String> alreadyList;
    private boolean isClearBlank = true;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    Bundle bundle = msg.getData();
                    String orderno = bundle.getString(ORDERINFO);
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    String resultStatus = payResult.getResultStatus();
                    LogUtils.file("支付宝回调结果:" + resultStatus);
                    if (TextUtils.equals(resultStatus, "9000")) {
                        sendRepacketMessage(orderno);
                    } else if (TextUtils.equals(resultStatus, "8000")) {
                        sendRepacketMessage(orderno);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setActivityView(R.layout.activity_send_flock_redpactet);
        alreadyList = new ArrayList<>();
        uid = Preferences.getWeiranUid(this);
        mytoken = Preferences.getWeiranToken(this);
        teamId = getIntent().getStringExtra(TARGETID);
        teamName = getIntent().getStringExtra(TARGETNAME);
        robotId = getIntent().getStringExtra(TARGETROBOT);
        masterUID = getIntent().getStringExtra(TARGETMASTER);
        NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount());
        nickname = userInfo.getName();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> {
            showKeyboard(false);
            finish();
        });
        TextView record = (TextView) findViewById(R.id.single_red_packet_record);
        record.setOnClickListener(v -> {
            RedpactRecordActivity.start(this, REQUEST_CODE_DETAIL);
        });
        send = (Button) findViewById(R.id.single_red_packet_send_money);
        send.setOnClickListener(v -> {
            sendRedpacket();
        });
        moneyTips = (TextView) findViewById(R.id.single_red_packet_input_money);
        inputMessage = (EditText) findViewById(R.id.input_brief);
        inputMessage.addTextChangedListener(new StringTextWatcher(20, inputMessage, new SimpleCallback<String>() {
            @Override
            public void onResult(boolean success, String result, int code) {
                if (TextUtils.isEmpty(result)) {
                    isClearBlank = true;
                } else if (moneyTips.getText().toString().equals(result)) {
                    isClearBlank = true;
                } else {
                    isClearBlank = false;
                }
            }
        }));
        inputMoney = (EditText) findViewById(R.id.single_red_packet_input);
        inputMoney.setFilters(new InputFilter[]{
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 4, afterDecimal = 2;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = inputMoney.getText() + source.toString();

                        if (temp.equals(".")) {
                            return "0.";
                        } else if (temp.toString().indexOf(".") == -1) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                if (source.length() == 1) {
                                    return "";
                                }
                            }
                        }
                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });
        inputMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && !TextUtils.equals(s, ".")) {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    float sendAmount = Float.valueOf(s.toString());
                    moneyTips.setText("¥" + decimalFormat.format(sendAmount));
                    if (isClearBlank) {
                        inputMessage.setText("¥" + decimalFormat.format(sendAmount));
                    }
                    send.setEnabled(sendAmount > 0);
                } else {
                    moneyTips.setText("¥0.00");
                    if (isClearBlank) {
                        inputMessage.setText("");
                    }
                    send.setEnabled(false);
                }
            }
        });
        redpacketType = (TextView) findViewById(R.id.single_red_packet_current);
        redpacketType.setText("当前为指定人领取，");
        redpacketType.setVisibility(View.GONE);
        redpacketChange = (TextView) findViewById(R.id.single_red_packet_change);
        redpacketChange.setText("改为普通红包");
        redpacketChange.setVisibility(View.GONE);
        redpacketChange.setOnClickListener(v -> {
            if (type == 3) {
                type = 2;
                redpacketType.setText("当前为指定人领取，");
                redpacketChange.setText("改为普通红包");
                appointLayoput.setVisibility(View.VISIBLE);
                appoint = null;
                redpacketAppoint.setText("请选择领取人");
            } else {
                type = 3;
                redpacketType.setText("当前为普通红包，");
                redpacketChange.setText("改为指定人领取");
                appointLayoput.setVisibility(View.GONE);
                appoint = DemoCache.getAccount();
            }
        });
        appointLayoput = (ConstraintLayout) findViewById(R.id.single_red_packet_appoint_layout);
        appointLayoput.setVisibility(View.GONE);
        appoint = null;
        redpacketAppoint = (TextView) findViewById(R.id.single_red_packet_appoint);
        redpacketAppoint.setText("请选择领取人");
        redpacketAppoint.setOnClickListener(v -> {
            ContactSelectActivity.Option option = new ContactSelectActivity.Option();
            option.title = "选择领取人";
            option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
            option.teamId = teamId;
            option.multi = true;
            option.alreadySelectedAccounts = alreadyList;
            ArrayList<String> includeAccounts = new ArrayList<>();
            if (!TextUtils.isEmpty(robotId)) {
                includeAccounts.add(robotId);
            }
            option.itemFilter = new ContactIdFilter(includeAccounts, true);
            NimUIKit.startContactSelector(this, option, REQUEST_CODE_ADVANCED);
        });
    }

    private void sendRedpacket() {
        int sendType;
        String money = inputMoney.getText().toString().trim();
        if (TextUtils.isEmpty(money)) {
            YchatToastUtils.showShort("请输入发送金额");
            return;
        }
        sendAmount = Float.valueOf(money);
        if (sendAmount <= 0) {
            YchatToastUtils.showShort("金额不能为0");
            return;
        }
        if (sendAmount > 200) {
            YchatToastUtils.showShort("红包金额不能超过200");
            return;
        }
        if (type == 2 && TextUtils.isEmpty(appoint)) {
            sendType = 3;
            appoint = DemoCache.getAccount();
        } else {
            sendType = type;
        }
        String message = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            envelopeMessage = "恭喜发财，大吉大利！";
        } else {
            envelopeMessage = message;
        }
        if (!NetworkUtil.isNetAvailable(SendFlockRedpactActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().querySendRedpacket(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (aVoid.getHb_onoff() == 1) {
                    sendFlockRedpacket(sendType);
                } else {
                    YchatToastUtils.showShort("此服务暂时关闭");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("网络繁忙，请重试");
            }
        });
    }

    private void sendFlockRedpacket(int sendType) {
        DialogMaker.showProgressDialog(this, "", false);
        LogUtils.file(uid + "开始发送红包" + teamId);
        ContactHttpClient.getInstance().sendRedpacket(uid, mytoken, appoint, nickname, sendAmount, sendType, teamId, teamName, masterUID, envelopeMessage, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                UnsentRedPacket unsentRedPacket = new UnsentRedPacket(teamId, aVoid.getOrderno(), envelopeMessage, SessionTypeEnum.Team);
                UnsentRedPacketCache.addUnsentRedPacket(unsentRedPacket);
                LogUtils.file(unsentRedPacket);
                if (aVoid != null && !TextUtils.isEmpty(aVoid.getPara())) {
                    final Runnable payRunnable = new Runnable() {

                        @Override
                        public void run() {
                            PayTask alipay = new PayTask(SendFlockRedpactActivity.this);
                            Map<String, String> result = alipay.payV2(aVoid.getPara(), true);
                            Message msg = new Message();
                            msg.what = SDK_PAY_FLAG;
                            msg.obj = result;
                            Bundle bundle = new Bundle();
                            bundle.putString(ORDERINFO, aVoid.getOrderno());
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                    };
                    // 必须异步调用
                    Thread payThread = new Thread(payRunnable);
                    payThread.start();
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                if (code == 100027) {
                    YchatToastUtils.showShort("本群不允许您发红包");
                } else {
                    YchatToastUtils.showShort("网络繁忙，请重试");
                }
            }
        });
    }

    private void sendRepacketMessage(String orderno) {
        EnvelopeBean envelopeBean = new EnvelopeBean();
        envelopeBean.setEnvelopesID(orderno);
        envelopeBean.setEnvelopeMessage(envelopeMessage);
        envelopeBean.setEnvelopeType(5);
        Intent intent = getIntent();
        intent.putExtra("Envelope", envelopeBean);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_ADVANCED) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            alreadyList.clear();
            alreadyList.addAll(selected);
            StringBuilder builder = new StringBuilder();
            int selectedNumber = selected.size();
            for (int i = 0; i < selectedNumber; i++) {
                builder.append(selected.get(i));
                if (i != selectedNumber - 1) {
                    builder.append(",");
                }
            }
            appoint = builder.toString();
            redpacketAppoint.setText(String.format("已选择%1$s人 ", selectedNumber));
        } else if (requestCode == REQUEST_CODE_DETAIL) {
            //finish();
        }
    }

    public static void start(Activity context, String teamId, String teamName, String masterUID, String robotId, int requestCode) {
        Intent intent = new Intent(context, SendFlockRedpactActivity.class);
        intent.putExtra(TARGETID, teamId);
        intent.putExtra(TARGETNAME, teamName);
        intent.putExtra(TARGETROBOT, robotId);
        intent.putExtra(TARGETMASTER, masterUID);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

}
