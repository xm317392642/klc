package com.xr.ychat.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.RegexUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.string.StringTextWatcher;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.common.ui.XEditText;
import com.xr.ychat.main.activity.MainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindPasswordFragment extends Fragment {
    public static final String NEED_LOGIN = "need_login";
    private XEditText inputPhone;
    private XEditText inputCode;
    private TextView sendCode;
    private Button next;
    private TimeCount timeCount;
    private XEditText inputFrist;
    private XEditText inputSecond;
    private boolean needLogin;
    private NimUserInfo userInfo;
    public static FindPasswordFragment newInstance(boolean needLogin) {
        FindPasswordFragment fragment = new FindPasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(NEED_LOGIN, needLogin);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
         userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(DemoCache.getAccount());
        return inflater.inflate(R.layout.fragment_find_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inputPhone = view.findViewById(R.id.find_password_account);
        if(userInfo!=null && !TextUtils.isEmpty(userInfo.getMobile())){
            inputPhone.setText(userInfo.getMobile());
            inputPhone.setEnabled(false);
        }else{
            inputPhone.setEnabled(true);
        }
        inputPhone.addTextChangedListener(new StringTextWatcher(11, inputPhone));
        inputCode = view.findViewById(R.id.find_password_input_code);
        inputCode.addTextChangedListener(new StringTextWatcher(6, inputCode));
        sendCode = view.findViewById(R.id.find_password_send_code);
        next = view.findViewById(R.id.rest_password_confirm);
        inputFrist = view.findViewById(R.id.rest_password_first);
        inputFrist.addTextChangedListener(new StringTextWatcher(18, inputPhone));
        inputSecond = view.findViewById(R.id.rest_password_repeat);
        inputSecond.addTextChangedListener(new StringTextWatcher(18, inputPhone));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        needLogin = bundle.getBoolean(NEED_LOGIN);
        View.OnClickListener listener = v -> {
            switch (v.getId()) {
                case R.id.find_password_send_code: {
                    sendVerfyCode();
                }
                break;
                case R.id.rest_password_confirm: {
                    String account = inputPhone.getText().toString().trim();
                    if (TextUtils.isEmpty(account)) {
                        YchatToastUtils.showShort("请输入手机号码");
                        return;
                    }
                    if (!RegexUtils.isMobileExact(account)) {
                        YchatToastUtils.showShort("请输入正确的手机号码");
                        return;
                    }
                    String code = inputCode.getText().toString().trim();
                    if (TextUtils.isEmpty(code)) {
                        YchatToastUtils.showShort("请输入验证码");
                        return;
                    }
                    String fristPassword = inputFrist.getText().toString().trim();
                    if (TextUtils.isEmpty(fristPassword)) {
                        YchatToastUtils.showShort("请输入新密码");
                        return;
                    }
                    String repeatPassword = inputSecond.getText().toString().trim();
                    if (TextUtils.isEmpty(repeatPassword)) {
                        YchatToastUtils.showShort("请确认新密码");
                        return;
                    }
                    if (!TextUtils.equals(fristPassword, repeatPassword)) {
                        YchatToastUtils.showShort("两次密码输入不一致");
                        return;
                    }
                    if (fristPassword.length() < 6) {
                        YchatToastUtils.showShort("密码为6-18位字母、数字组合");
                        return;
                    }
                    if (!isPassword(fristPassword)) {
                        YchatToastUtils.showShort("密码为6-18位字母、数字组合");
                        return;
                    }
                    if (!NetworkUtil.isNetAvailable(getActivity())) {
                        YchatToastUtils.showShort(R.string.network_is_not_available);
                        return;
                    }
                    DialogMaker.showProgressDialog(getActivity(), "正在重置密码", false);
                    ContactHttpClient.getInstance().changePassword(account, MD5.getStringMD5(fristPassword), code, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                        @Override
                        public void onSuccess(RequestInfo aVoid) {
                            DialogMaker.dismissProgressDialog();
                            YchatToastUtils.showShort("重置密码成功");
                            getActivity().finish();
                            if (needLogin) {

                                MainActivity.logout(getActivity(), true, userInfo.getAvatar());
                                NIMClient.getService(AuthService.class).logout();
                            }
                        }

                        @Override
                        public void onFailed(int code, String errorMsg) {
                            DialogMaker.dismissProgressDialog();
                            if (code == 100001) {
                                YchatToastUtils.showShort("请求缺少参数");
                            } else if (code == 100006) {
                                YchatToastUtils.showShort("验证码错误");
                            } else if (code == 100009) {
                                YchatToastUtils.showShort("手机号不对");
                            } else if (code == 100002) {
                                YchatToastUtils.showShort("您未注册，请使用验证码登录注册后再重置密码");
                            } else {
                                YchatToastUtils.showShort("重置密码失败");
                            }
                        }
                    });
                }
                break;
            }
        };
        sendCode.setOnClickListener(listener);
        next.setOnClickListener(listener);
    }

    /**
     * 获取验证码
     */
    private void sendVerfyCode() {
        String account = inputPhone.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            YchatToastUtils.showShort("请输入手机号码");
            return;
        }
        if (!RegexUtils.isMobileExact(account)) {
            YchatToastUtils.showShort("请输入正确的手机号码");
            return;
        }
        if (!NetworkUtil.isNetAvailable(getActivity())) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(getActivity(), "正在获取验证码", false);
        startCountdown();
        ContactHttpClient.getInstance().sendVerifyCode(account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("验证码获取成功");
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                if (code == 100008) {
                    YchatToastUtils.showShort("手机号没有在平台绑定账户");
                    DownloadTipsActivity.start(getActivity());
                } else {
                    YchatToastUtils.showShort("验证码获取失败");
                }
            }
        });
    }

    public void startCountdown() {
        if (timeCount != null) {
            timeCount.cancel();
            timeCount = null;
        }
        timeCount = new TimeCount(60000, 1000);
        timeCount.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timeCount != null) {
            timeCount.cancel();
            timeCount = null;
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            getActivity().runOnUiThread(() -> {
                sendCode.setEnabled(true);
                sendCode.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_be6913));
                sendCode.setText("获取验证码");
            });
        }

        @Override
        public void onTick(long millisUntilFinished) {
            getActivity().runOnUiThread(() -> {
                sendCode.setEnabled(false);
                sendCode.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_a9a9a9));
                sendCode.setText("重新获取" + (millisUntilFinished + 1000) / 1000 + "s");
            });
        }
    }

    private boolean isPassword(String password) {
        Pattern p = Pattern.compile("^[a-zA-Z].*[0-9]|.*[0-9].*[a-zA-Z]");
        Matcher m = p.matcher(password);
        return m.matches();
    }

}
