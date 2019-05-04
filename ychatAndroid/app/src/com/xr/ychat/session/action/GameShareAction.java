package com.xr.ychat.session.action;

import android.content.Intent;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.xr.ychat.session.extension.BussinessCardAttachment;
import com.xr.ychat.session.extension.GameShareAttachment;

import java.util.ArrayList;

/**
 * 游戏分享
 */
public class GameShareAction extends BaseAction {
    public GameShareAction() {
        super(R.drawable.nim_message_plus_business_card_selector, R.string.input_panel_game_share);
    }
    public  void sendMsg(String title,String des,String sourceName,String linkIcon,String linkSource,String linkUrl){
        GameShareAttachment attachment = new GameShareAttachment();
        attachment.setShareLinkTitle(title);
        attachment.setShareLinkDes(des);
        attachment.setShareLinkSourceName(sourceName);
        attachment.setShareLinkImage(linkIcon);
        attachment.setShareLinkSourceImage(linkSource);
        attachment.setShareLinkUrl(linkUrl);
        IMMessage message = MessageBuilder.createCustomMessage(
                getAccount(), getSessionType(), "游戏分享", attachment
        );
        sendMessage(message);
    }
    @Override
    public void onClick() {
        String title="血战到底 计分 1分 8局3番(小旭茶社)血战到底 计分 1分 8局3番(小旭茶社)";
        String des="自摸加底、金构钓、海底捞、点杠花(点炮)";
        String sourceName="吆吆约牌";
        String linkUrl="https://3w.huanqiu.com/a/c36dc8/7LxuZDTmGpG";
        String linkIcon ="https://t1.huanqiu.cn/0a330953b6a0442d1d99b9d02e959321.jpg";
        String linkSource ="http://icon.mobanwang.com/UploadFiles_8971/200910/20091011134333685.png";

        GameShareAttachment attachment = new GameShareAttachment();
        attachment.setShareLinkTitle(title);
        attachment.setShareLinkDes(des);
        attachment.setShareLinkSourceName(sourceName);
        attachment.setShareLinkImage(linkIcon);
        attachment.setShareLinkSourceImage(linkSource);
        attachment.setShareLinkUrl(linkUrl);
        IMMessage message = MessageBuilder.createCustomMessage(
                getAccount(), getSessionType(), "游戏分享", attachment
        );
        sendMessage(message);
    }

}

