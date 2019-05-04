package com.xr.ychat.redpacket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.string.StringTextWatcher;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;

import java.text.DecimalFormat;
import java.util.Map;

public class SendSingleRedpactActivity extends SwipeBackUI {
    private static final int SDK_PAY_FLAG = 1;
    private static final String TARGETID = "target_id";
    private static final String ORDERINFO = "order_info";
    private TextView moneyTips;
    private EditText inputMoney;
    private EditText inputMessage;
    private Button send;
    private String uid;
    private String mytoken;
    private String nickname;
    private String targetAccount;
    private String envelopeMessage;
    private boolean isClearBlank = true;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Bundle bundle = msg.getData();
                        String orderno = bundle.getString(ORDERINFO);
                        attestationPay(uid, mytoken, payResult.getResult(), orderno);
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
        setContentView(R.layout.activity_send_single_redpactet);
        uid = Preferences.getWeiranUid(this);
        mytoken = Preferences.getWeiranToken(this);
        targetAccount = getIntent().getStringExtra(TARGETID);
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
            RedpactRecordActivity.start(SendSingleRedpactActivity.this);
        });
        send = (Button) findViewById(R.id.single_red_packet_send_money);
        send.setOnClickListener(v -> {
            sendRedpacket();
        });
        moneyTips = (TextView) findViewById(R.id.single_red_packet_input_money);
        inputMessage = (EditText) findViewById(R.id.input_brief);
        inputMessage.addTextChangedListener(new StringTextWatcher(25, inputMessage, new SimpleCallback<String>() {
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
                if (!TextUtils.isEmpty(s)) {

                    float sendAmount = Float.valueOf(s.toString());
                    moneyTips.setText("¥" + decimalFormat.format(sendAmount));
                    send.setEnabled(sendAmount > 0);
                    if (isClearBlank) {
                        inputMessage.setText("¥" + decimalFormat.format(sendAmount));
                    }
                } else {
                    moneyTips.setText("¥0.00");
                    if (isClearBlank) {
                        inputMessage.setText("");
                    }
                    send.setEnabled(false);
                }
            }
        });

    }

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    /**
     * 发单人红包
     */
    private void sendRedpacket() {
        String money = inputMoney.getText().toString().trim();
        if (TextUtils.isEmpty(money)) {
            YchatToastUtils.showShort("请输入发送金额");
            return;
        }
        float sendAmount = Float.valueOf(money);
        if (sendAmount <= 0) {
            YchatToastUtils.showShort("金额不能为0");
            return;
        }
        if (sendAmount > 200) {
            YchatToastUtils.showShort("红包金额不能超过200");
            return;
        }
        String message = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            envelopeMessage = "恭喜发财，大吉大利！";
        } else {
            envelopeMessage = message;
        }
        if (!NetworkUtil.isNetAvailable(SendSingleRedpactActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().querySendRedpacket(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (aVoid.getHb_onoff() == 1) {
                    sendSingleRedpacket(sendAmount, envelopeMessage);
                } else {
                    YchatToastUtils.showShort("此服务暂时关闭");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("此服务暂时关闭");
            }
        });
    }

    private void sendSingleRedpacket(float sendAmount, String envelopeMessage) {
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().sendRedpacket(uid, mytoken, targetAccount, nickname, sendAmount, 1, envelopeMessage, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (aVoid != null && !TextUtils.isEmpty(aVoid.getPara())) {
                    final Runnable payRunnable = new Runnable() {

                        @Override
                        public void run() {
                            PayTask alipay = new PayTask(SendSingleRedpactActivity.this);
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
            }

        });
    }

    /**
     * 验签支付
     */
    private void attestationPay(String uid, String mytoken, String response, String orderno) {
        ContactHttpClient.getInstance().attestationPay(uid, mytoken, response, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                EnvelopeBean envelopeBean = new EnvelopeBean();
                envelopeBean.setEnvelopesID(orderno);
                envelopeBean.setEnvelopeMessage(envelopeMessage);
                Intent intent = getIntent();
                intent.putExtra("Envelope", envelopeBean);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    public static void start(Activity context, String targetAccount, int requestCode) {
        Intent intent = new Intent(context, SendSingleRedpactActivity.class);
        intent.putExtra(TARGETID, targetAccount);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }

}
