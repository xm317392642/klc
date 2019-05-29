package com.xr.ychat.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.xr.ychat.R;
import com.xr.ychat.common.ui.XEditText;

public class SettingYchatActivity extends SwipeBackUI {
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private TextView toolbarAction;
    private XEditText inputYchat;
    private String uid;
    private String myToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_setting_ychat);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("空了吹号");
        mToolbar.setNavigationOnClickListener(v -> {
            showKeyboard(false);
            finish();
        });
        findViews();
        uid = Preferences.getWeiranUid(this);
        myToken = Preferences.getWeiranToken(this);
    }

    private void findViews() {
        inputYchat = (XEditText) findViewById(R.id.discussion_name);
        toolbarAction = (TextView) findViewById(R.id.action_bar_right_clickable_textview);
        toolbarAction.setOnClickListener(v -> {
            String account = inputYchat.getTextTrimmed();
            if (TextUtils.isEmpty(account)) {
                YchatToastUtils.showShort("请输入要修改的空了吹号");
                return;
            }
            if (!isPassword(account)) {
                YchatToastUtils.showShort("请输入正确的空了吹号");
                return;
            }
            if (!NetworkUtil.isNetAvailable(SettingYchatActivity.this)) {
                YchatToastUtils.showShort(R.string.network_is_not_available);
                return;
            }
            DialogMaker.showProgressDialog(SettingYchatActivity.this, "正在修改空了吹号", false);
            ContactHttpClient.getInstance().changeYchatAccount(uid, myToken, account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo aVoid) {
                    DialogMaker.dismissProgressDialog();
                    SPUtils.getInstance().put(CommonUtil.YCHAT_ACCOUNT, aVoid.getYchatNo());
                    YchatToastUtils.showShort("空了吹号修改成功");
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    DialogMaker.dismissProgressDialog();
                    if (code == 100024) {
                        YchatToastUtils.showShort("空了吹号不合法");
                    } else if (code == 100025) {
                        YchatToastUtils.showShort("空了吹号重复");
                    } else if (code == 100026) {
                        YchatToastUtils.showShort("空了吹号已经修改");
                    } else {
                        YchatToastUtils.showShort("修改空了吹号失败");
                    }
                }
            });
        });
    }

    private boolean isPassword(String password) {
        return password.length() > 5;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingYchatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
