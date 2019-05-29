package com.xr.ychat.login;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.main.activity.MainActivity;

/**
 * 绑定手机号
 * 只是第一次登录成功时弹出绑定手机号。
 */
public class BindPhoneNumActivity extends SwipeBackUI implements OnKeyListener {
    private final int BASIC_PERMISSION_REQUEST_CODE = 110;
    private TextView txSendCode;
    private TimeCount timeCount;
    private EditText edit_login_account, edit_code;
    private String flag;

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent(context, BindPhoneNumActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setActivityView(R.layout.activity_bind_phone_num);
        flag = getIntent().getStringExtra("flag");
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setNavigationOnClickListener(v -> exitLogic(false));
        txSendCode = findView(R.id.edit_login_verification_code);
        edit_login_account = findView(R.id.edit_login_account);
        edit_code = findView(R.id.edit_code);
        txSendCode.setOnClickListener(v -> {
            sendVerfyCode();
        });
        findView(R.id.bind_phone).setOnClickListener(v -> {
            if (!judge()) {
                return;
            }
            String uid = Preferences.getWeiranUid(this);
            String mytoken = Preferences.getWeiranToken(this);
            String accid = DemoCache.getAccount();
            DialogMaker.showProgressDialog(this, "");
            String phone = edit_login_account.getText().toString();
            ContactHttpClient.getInstance().bindPhoe(uid, mytoken, accid, phone, edit_code.getText().toString(), new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo requestInfo) {
                    Preferences.saveUserPhone(BindPhoneNumActivity.this, phone);
                    YchatToastUtils.showShort("绑定成功");
                    DialogMaker.dismissProgressDialog();
                    exitLogic(true);
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    if (code == 100006) {
                        YchatToastUtils.showShort("验证码错误");
                    } else if (code == 100035) {
                        YchatToastUtils.showShort("该手机号已经绑定其他账号，可使用手机号直接登录");
                    } else if (code == 100036) {
                        YchatToastUtils.showShort("该手机号未注册，微信登录绑定手机号即可");
                    } else {
                        YchatToastUtils.showShort("绑定失败，请重试" + code);//"code" : 100004
                    }

                    DialogMaker.dismissProgressDialog();
                }
            });
        });

        requestBasicPermission();
    }

    @Override
    public void onBackPressed() {
        exitLogic(false);
    }

    /**
     * 退出逻辑，共两种情况
     * @param isBind 是绑定操作，还是按返回键操作
     */
    private void exitLogic(boolean isBind) {
        if ("AccountSecretActivity".equals(flag)) {
            //从账号隐私页面跳转过来的话，关闭当前页面;如果是进行绑定操作，则账号隐私页面刷新为‘设置登录密码’
            if(isBind){
                setResult(Activity.RESULT_OK);
            }
            finish();
        } else {
            //第一次微信登录成功，提示用户绑定手机号，后续绑定完跳转到主界面
            MainActivity.start(this);
            finish();
            ActivityUtils.finishActivity(LoginAuthorizeActivity.class);
        }

    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private void requestBasicPermission() {
        MPermission.with(BindPhoneNumActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {

    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {

    }


    private boolean judge() {
        String account = edit_login_account.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            YchatToastUtils.showShort("请输入手机号码");
            return false;
        }
        if (!RegexUtils.isMobileExact(account)) {
            YchatToastUtils.showShort("请输入正确的手机号码");
            return false;
        }
        if (TextUtils.isEmpty(edit_code.getText().toString().trim())) {
            YchatToastUtils.showShort("验证码不能为空");
            return false;
        }
        return true;
    }

    /**
     * 获取验证码
     */
    private void sendVerfyCode() {

        String account = edit_login_account.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            YchatToastUtils.showShort("请输入手机号码");
            return;
        }
        if (!RegexUtils.isMobileExact(account)) {
            YchatToastUtils.showShort("请输入正确的手机号码");
            return;
        }
        if (!NetworkUtil.isNetAvailable(BindPhoneNumActivity.this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "正在获取验证码", false);
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
                if (code == 100001) {
                    YchatToastUtils.showShort("请求缺少参数");
                } else if (code == 100009) {
                    YchatToastUtils.showShort("手机号不对");
                } else if (code == 100008) {
                    YchatToastUtils.showShort("手机号没有在平台绑定账户");
                }  else {
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
    protected void onDestroy() {
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
            runOnUiThread(() -> {
                txSendCode.setEnabled(true);
                txSendCode.setTextColor(ContextCompat.getColor(BindPhoneNumActivity.this, R.color.color_be6913));
                txSendCode.setText("获取验证码");
            });
        }

        @Override
        public void onTick(long millisUntilFinished) {
            runOnUiThread(() -> {
                txSendCode.setEnabled(false);
                txSendCode.setTextColor(ContextCompat.getColor(BindPhoneNumActivity.this, R.color.color_a9a9a9));
                txSendCode.setText("重新获取" + (millisUntilFinished + 1000) / 1000 + "s");
            });
        }
    }


}
