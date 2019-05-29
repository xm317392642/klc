package com.netease.nim.uikit.business.team.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.team.model.Team;

/**
 * 群主自动领取红包
 * Created by hzxuwen on 2015/3/18.
 */
public class TeamAutoGetRedpacketActivity extends SwipeBackUI {
    private String teamId;
    private Team team;
    private boolean isSelfAdmin;//是否是群主
    private View layoutAutoGetRedpacket;
    private SwitchButton switchButton;
    private String uid;
    private String mytoken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.nim_advanced_team_auto_get_redpacket_activity);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setNavigationOnClickListener(v -> finish());
        teamId = getIntent().getStringExtra("teamId");
        team = NimUIKit.getTeamProvider().getTeamById(teamId);
        String creator = team.getCreator();
        if (creator.equals(NimUIKit.getAccount())) {
            isSelfAdmin = true;
        }
        init();
    }

    /**
     * 群主自动领取红包
     */
    private void init() {
        layoutAutoGetRedpacket = findView(R.id.team_auto_get_redpacket_layout);
        layoutAutoGetRedpacket.findViewById(R.id.line).setVisibility(View.GONE);
        TextView tpotectTx = layoutAutoGetRedpacket.findViewById(R.id.item_title);
        switchButton = layoutAutoGetRedpacket.findViewById(R.id.setting_item_toggle);
        tpotectTx.setText("群主自动领取红包");
        uid = Preferences.getWeiranUid(this);
        mytoken = Preferences.getWeiranToken(this);
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        ContactHttpClient.getInstance().autoGetRedpacketInfoRequest(uid, mytoken, teamId, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo requestInfo) {
                DialogMaker.dismissProgressDialog();
                if (requestInfo.getOpen() == 1) {
                    switchButton.setCheck(true);
                } else {
                    switchButton.setCheck(false);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
            }
        });
        switchButton.setOnChangedListener((v, checkState) -> {
            if (checkState) {
                setSwitch(true, 1);
            } else {
                setSwitch(false, 0);
            }
        });
    }

    /**
     * 设置自动领取红包的开关状态
     *
     * @param checkState
     * @param open
     */
    private void setSwitch(boolean checkState, int open) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        ContactHttpClient.getInstance().autoGetRedpacketSwitchRequest(uid, mytoken, teamId, open, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo requestInfo) {
                DialogMaker.dismissProgressDialog();
                switchButton.setCheck(checkState);
                if (checkState) {
                    YchatToastUtils.showShort("群主自动领取红包开启");
                } else {
                    YchatToastUtils.showShort("群主自动领取红包关闭");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                switchButton.setCheck(!checkState);
                YchatToastUtils.showShort(code + errorMsg);
            }
        });
    }
}
