package com.xr.ychat.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.xr.ychat.R;

public class AddMineMethodActivity extends SwipeBackUI implements SwitchButton.OnChangedListener {
    public static final int PHONE = 1;
    public static final int YCHAT = 1 << 1;
    public static final int GROUP = 1 << 2;
    public static final int QRCODE = 1 << 3;
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private View phoneLayout;
    private SwitchButton phoneSwitchButton;
    private View ychatLayout;
    private SwitchButton ychatSwitchButton;
    private View groupChatLayout;
    private SwitchButton groupChatSwitchButton;
    private View qrcodeLayout;
    private SwitchButton qrcodeSwitchButton;
    private String uid;
    private String myToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mine_method);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("添加我的方式");
        mToolbar.setNavigationOnClickListener(v -> finish());
        findViews();
        uid = Preferences.getWeiranUid(this);
        myToken = Preferences.getWeiranToken(this);
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().querySearchingSwitch(uid, myToken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                int flag = aVoid.getFlag();
                phoneSwitchButton.setCheck((flag & PHONE) > 0);
                ychatSwitchButton.setCheck((flag & YCHAT) > 0);
                groupChatSwitchButton.setCheck((flag & GROUP) > 0);
                qrcodeSwitchButton.setCheck((flag & QRCODE) > 0);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    private void findViews() {
        phoneLayout = findViewById(R.id.addmine_method_phone);
        phoneSwitchButton = phoneLayout.findViewById(R.id.user_profile_toggle);
        phoneSwitchButton.setOnChangedListener(this::OnChanged);
        ((TextView) phoneLayout.findViewById(R.id.user_profile_title)).setText("手机号");
        ychatLayout = findViewById(R.id.addmine_method_ychat);
        ychatSwitchButton = ychatLayout.findViewById(R.id.user_profile_toggle);
        ychatSwitchButton.setOnChangedListener(this::OnChanged);
        ((TextView) ychatLayout.findViewById(R.id.user_profile_title)).setText("空了吹号");
        ychatLayout.findViewById(R.id.line).setVisibility(View.GONE);
        groupChatLayout = findViewById(R.id.addmine_method_group_chat);
        groupChatSwitchButton = groupChatLayout.findViewById(R.id.user_profile_toggle);
        groupChatSwitchButton.setOnChangedListener(this::OnChanged);
        ((TextView) groupChatLayout.findViewById(R.id.user_profile_title)).setText("群聊");
        qrcodeLayout = findViewById(R.id.addmine_method_qrcode);
        qrcodeSwitchButton = qrcodeLayout.findViewById(R.id.user_profile_toggle);
        qrcodeSwitchButton.setOnChangedListener(this::OnChanged);
        ((TextView) qrcodeLayout.findViewById(R.id.user_profile_title)).setText("二维码");
        qrcodeLayout.findViewById(R.id.line).setVisibility(View.GONE);
    }

    @Override
    public void OnChanged(View v, boolean checkState) {
        int phoneState = phoneSwitchButton.isChoose() ? PHONE : 0;
        int ychatState = ychatSwitchButton.isChoose() ? YCHAT : 0;
        int groupChatState = groupChatSwitchButton.isChoose() ? GROUP : 0;
        int qrcodeState = qrcodeSwitchButton.isChoose() ? QRCODE : 0;
        int state = phoneState + ychatState + groupChatState + qrcodeState;
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().setSearchingSwitch(uid, myToken, state, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                ((SwitchButton) v).setCheck(!checkState);
                YchatToastUtils.showShort("设置失败");
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, AddMineMethodActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
