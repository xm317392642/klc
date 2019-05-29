package com.xr.ychat.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.common.ui.XEditText;
import com.xr.ychat.login.MyCodeActivity;

/**
 * 添加好友页面
 * Created by huangjun on 2015/8/11.
 */
public class AddFriendActivity extends SwipeBackUI {

    private XEditText searchEdit;
    private TextView search;
    private TextView qrcode;
    private NimUserInfo userInfo;
    private String userAccount;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, AddFriendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.add_friend_activity);
        findViews();
        initActionbar();
        getUserInfo();
    }

    private void findViews() {
        searchEdit = findView(R.id.search_friend_edit);
        search = findView(R.id.search_friend_add);
        qrcode = findView(R.id.search_friend_icon);
        qrcode.setOnClickListener(v -> {
            MyCodeActivity.start(AddFriendActivity.this);
        });
    }

    private void initActionbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        search.setOnClickListener(v -> {
            if (TextUtils.isEmpty(searchEdit.getTextTrimmed())) {
                YchatToastUtils.showShort(R.string.not_allow_empty);
            } else if (TextUtils.equals(searchEdit.getTextTrimmed(), DemoCache.getAccount())) {
                YchatToastUtils.showShort(R.string.add_friend_self_tip);
            } else {
                query();
            }
        });
    }

    private boolean isPassword(String password) {
        return password.length() > 5;
    }

    private void query() {
        final String account = searchEdit.getTextTrimmed();
        if (!isPassword(account)) {
            YchatToastUtils.showShort("请输入正确的空了吹号");
            return;
        }
        DialogMaker.showProgressDialog(AddFriendActivity.this, "", false);
        ContactHttpClient.getInstance().querySearching(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), 1, account, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (!TextUtils.isEmpty(aVoid.getAccid()) && aVoid.getAccid().length() > 1) {
                    if (TextUtils.equals(aVoid.getAccid(), DemoCache.getAccount())) {
                        YchatToastUtils.showShort("不能添加自己为好友");
                    } else {
                        NimUIKit.getUserInfoProvider().getUserInfoAsync(aVoid.getAccid(), new SimpleCallback<NimUserInfo>() {
                            @Override
                            public void onResult(boolean success, NimUserInfo result, int code) {
                                if (success) {
                                    if (result == null) {
                                        EasyAlertDialogHelper.showOneButtonDiolag(AddFriendActivity.this, R.string.user_not_exsit, R.string.user_tips, R.string.ok, false, null);
                                    } else {
                                        UserProfileActivity.start(AddFriendActivity.this, aVoid.getAccid());
                                    }
                                } else if (code == 408) {
                                    YchatToastUtils.showShort(R.string.network_is_not_available);
                                } else if (code == ResponseCode.RES_EXCEPTION) {
                                    YchatToastUtils.showShort("on exception");
                                } else {
                                    YchatToastUtils.showShort("on failed:" + code);
                                }
                            }
                        });
                    }
                } else {
                    YchatToastUtils.showShort("未找到该用户");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("该用户可能关闭了查找权限");
            }
        });
    }

    private void getUserInfo() {
        userAccount = DemoCache.getAccount();
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(userAccount);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(userAccount, new SimpleCallback<NimUserInfo>() {

                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        userInfo = result;
                        updateUI();
                    }
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        String ychatNo = SPUtils.getInstance().getString(CommonUtil.YCHAT_ACCOUNT);
        if (TextUtils.isEmpty(ychatNo)) {
            ContactHttpClient.getInstance().getYchatAccount(Preferences.getWeiranUid(this), Preferences.getWeiranToken(this), userAccount, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo aVoid) {
                    SPUtils.getInstance().put(CommonUtil.YCHAT_ACCOUNT, aVoid.getYchatNo());
                    qrcode.setText(String.format("我的空了吹号: %1$s", aVoid.getYchatNo()));
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    qrcode.setText("");
                }
            });
        } else {
            qrcode.setText(String.format("我的空了吹号: %1$s", ychatNo));
        }
    }

}
