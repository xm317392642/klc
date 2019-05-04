package com.xr.ychat.session.action;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.xr.ychat.R;
import com.xr.ychat.redpacket.BindAlipayActivity;
import com.xr.ychat.redpacket.EnvelopeBean;
import com.xr.ychat.redpacket.NIMRedPacketClient;
import com.xr.ychat.session.extension.RedPacketAttachment;

public class RedPacketAction extends BaseAction {

    public RedPacketAction() {
        super(R.drawable.message_plus_rp_selector, R.string.red_packet);
    }

    private static final int CREATE_GROUP_RED_PACKET = 51;
    private static final int CREATE_SINGLE_RED_PACKET = 10;

    @Override
    public void onClick() {
        if (getContainer().sessionType == SessionTypeEnum.Team) {
            if (TeamHelper.isTeamMember(getContainer().account, NimUIKit.getAccount())) {
                openSendPacket();
            } else {
                YchatToastUtils.showShort("你已不在本群，无法进行下一步操作");
            }
        } else {
            openSendPacket();
        }
    }

    private void openSendPacket() {
        int requestCode;
        if (getContainer().sessionType == SessionTypeEnum.Team) {
            requestCode = makeRequestCode(CREATE_GROUP_RED_PACKET);
        } else if (getContainer().sessionType == SessionTypeEnum.P2P) {
            requestCode = makeRequestCode(CREATE_SINGLE_RED_PACKET);
        } else {
            return;
        }
        if (!NetworkUtil.isNetAvailable(getActivity())) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(getActivity(), "", false);
        String mytoken = Preferences.getWeiranToken(getActivity());
        String uid = Preferences.getWeiranUid(getActivity());
        ContactHttpClient.getInstance().queryAlipayAccount(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (TextUtils.isEmpty(aVoid.getAliuid())) {
                    BindAlipayActivity.start(getActivity());
                } else {
                    NIMRedPacketClient.startSendRpActivity(getActivity(), getContainer().sessionType, getAccount(), requestCode);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
                YchatToastUtils.showShort("网络繁忙");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        sendRpMessage(data);
    }

    private void sendRpMessage(Intent data) {
        EnvelopeBean groupRpBean = (EnvelopeBean) data.getSerializableExtra("Envelope");
        if (groupRpBean == null) {
            return;
        }
        RedPacketAttachment attachment = new RedPacketAttachment();
        // 红包id，红包信息，红包名称
        attachment.setRpId(groupRpBean.getEnvelopesID());
        attachment.setRpContent(groupRpBean.getEnvelopeMessage());
        attachment.setRpTitle(groupRpBean.getEnvelopeName());
        attachment.setRpType(groupRpBean.getEnvelopeType());
        String content = getActivity().getString(R.string.rp_push_content);
        // 不存云消息历史记录
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = false;
        IMMessage message = MessageBuilder.createCustomMessage(getAccount(), getSessionType(), content, attachment, config);
        sendMessage(message);
    }
}
