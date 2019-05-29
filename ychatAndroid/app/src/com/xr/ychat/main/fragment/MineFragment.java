package com.xr.ychat.main.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;
import com.xr.ychat.contact.activity.UserProfileSettingActivity;
import com.xr.ychat.login.ScanCodeActivity;
import com.xr.ychat.main.activity.HelpActivity;
import com.xr.ychat.main.activity.SettingsActivity;
import com.xr.ychat.redpacket.BindAlipayActivity;
import com.xr.ychat.redpacket.RedpactRecordActivity;

public class MineFragment extends Fragment {
    private static final int REQUEST_CODE_DETAIL = 101;
    private HeadImageView avatar;
    private TextView name;
    private TextView account;
    private TextView scan;
    private TextView packet;
    private TextView setting;
    private TextView help;
    private ConstraintLayout infomation;
    private View.OnClickListener listener;
    private String userAccount;
    private String uid;
    private String mytoken;
    private NimUserInfo userInfo;

    public static MineFragment newInstance() {
        MineFragment fragment = new MineFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatar = view.findViewById(R.id.mine_avatar);
        name = view.findViewById(R.id.mine_name);
        account = view.findViewById(R.id.mine_account);
        scan = view.findViewById(R.id.mine_scan);
        packet = view.findViewById(R.id.mine_packet);
        setting = view.findViewById(R.id.mine_setting);
        help = view.findViewById(R.id.mine_help);
        infomation = view.findViewById(R.id.mine_information);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        uid = Preferences.getWeiranUid(getActivity());
        mytoken = Preferences.getWeiranToken(getActivity());
        getUserInfo();
        listener = v -> {
            switch (v.getId()) {
                case R.id.mine_avatar:
                case R.id.mine_information: {
                    UserProfileSettingActivity.start(getActivity(), DemoCache.getAccount());
                }
                break;
                case R.id.mine_help: {
                    HelpActivity.start(getActivity(), "http://ht.yaoliaoim.com/help/", "帮助与反馈");
                }
                break;
                case R.id.mine_setting: {
                    SettingsActivity.start(getActivity());
                }
                break;
                case R.id.mine_packet: {
                    String aliuid = SPUtils.getInstance().getString(CommonUtil.ALIPAYUID);
                    if (TextUtils.isEmpty(aliuid)) {
                        BindAlipayActivity.start(getActivity());
                    } else {
                        RedpactRecordActivity.start(getActivity(), REQUEST_CODE_DETAIL);
                    }
                }
                break;
                case R.id.mine_scan: {
                    ScanCodeActivity.start(getActivity());
                }
                break;
            }
        };
        avatar.setOnClickListener(listener);
        scan.setOnClickListener(listener);
        packet.setOnClickListener(listener);
        setting.setOnClickListener(listener);
        help.setOnClickListener(listener);
        infomation.setOnClickListener(listener);
        queryAlipayAccount();
    }

    private void getUserInfo() {
        userAccount = NimUIKit.getAccount();
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
        name.setText(String.format("%1$s", userInfo.getName()));
        avatar.loadAvatar(userInfo.getAvatar());
        String ychatNo = SPUtils.getInstance().getString(CommonUtil.YCHAT_ACCOUNT);
        if (TextUtils.isEmpty(ychatNo)) {
            ContactHttpClient.getInstance().getYchatAccount(uid, mytoken, userAccount, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
                @Override
                public void onSuccess(RequestInfo aVoid) {
                    SPUtils.getInstance().put(CommonUtil.YCHAT_ACCOUNT, aVoid.getYchatNo());
                    account.setText(String.format("空了吹号: %1$s", aVoid.getYchatNo()));
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    account.setText("");
                }
            });
        } else {
            account.setText(String.format("空了吹号: %1$s", ychatNo));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }

    private void queryAlipayAccount() {
        ContactHttpClient.getInstance().queryAlipayAccount(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                SPUtils.getInstance().put(CommonUtil.ALIPAYUID, aVoid.getAliuid());
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

}
