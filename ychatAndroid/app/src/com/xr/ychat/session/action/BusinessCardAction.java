package com.xr.ychat.session.action;

import android.content.Intent;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nim.uikit.R;
import com.xr.ychat.session.extension.BussinessCardAttachment;

import java.util.ArrayList;

/**
 * 名片
 */
public class BusinessCardAction extends BaseAction {
    public BusinessCardAction() {
        super(R.drawable.nim_message_plus_business_card_selector, R.string.input_panel_business_card);
    }

    @Override
    public void onClick() {
        if (getContainer().sessionType == SessionTypeEnum.Team) {
            if (TeamHelper.isTeamMember(getContainer().account, NimUIKit.getAccount())) {
                selectUser();
            } else {
                YchatToastUtils.showShort("你已不在本群，无法进行下一步操作");
            }
        } else {
            selectUser();
        }
    }

    private void selectUser() {
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.type = ContactSelectActivity.ContactSelectType.BUDDY;
        option.title = "选择名片";
        option.multi = false;
        ContactSelectActivity.startActivityForResult(getActivity(), option, makeRequestCode(RequestCode.REQUEST_CODE_CONTACT_SELECT));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.REQUEST_CODE_CONTACT_SELECT) {
            ArrayList<String> addSelected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            if (addSelected != null && !addSelected.isEmpty()) {
                UserInfo userInfo = NimUIKitImpl.getUserInfoProvider().getUserInfo(addSelected.get(0));
                BussinessCardAttachment attachment = new BussinessCardAttachment();
                attachment.setPersonCardUserid(userInfo.getAccount());
                attachment.setPersonCardUserName(userInfo.getName());
                attachment.setPersonCardUserImage(userInfo.getAvatar());
                IMMessage message = MessageBuilder.createCustomMessage(
                        getAccount(), getSessionType(), "个人名片", attachment
                );
                sendMessage(message);
            }
        }
    }
}

