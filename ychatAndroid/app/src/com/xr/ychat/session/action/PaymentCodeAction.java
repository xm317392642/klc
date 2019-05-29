package com.xr.ychat.session.action;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.xr.ychat.R;
import com.xr.ychat.redpacket.PaymentCodeActivity;

import java.io.File;

/**
 * 支付宝收款码
 */
public class PaymentCodeAction extends BaseAction {
    public PaymentCodeAction() {
        super(R.drawable.message_plus_payment_code_selector, R.string.input_panel_paymentcode);
    }

    @Override
    public void onClick() {
        int requestCode = makeRequestCode(RequestCode.PICK_IMAGE);
        if (getContainer().sessionType == SessionTypeEnum.Team) {
            if (TeamHelper.isTeamMember(getContainer().account, NimUIKit.getAccount())) {
                PaymentCodeActivity.start(getActivity(), requestCode);
            } else {
                YchatToastUtils.showShort("你已不在本群，无法进行下一步操作");
            }
        } else {
            PaymentCodeActivity.start(getActivity(), requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == RequestCode.PICK_IMAGE) {
            Uri photoPath = data.getParcelableExtra(Extras.EXTRA_FILE_PATH);
            File imageFile = new File(photoPath.getPath());
            if (!imageFile.exists()) {
                return;
            }
            IMMessage message = MessageBuilder.createImageMessage(getAccount(), getSessionType(), imageFile, imageFile.getName());
            sendMessage(message);
        }
    }
}

