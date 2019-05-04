package com.xr.ychat.session.viewholder;

import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.xr.ychat.session.extension.ScreenCaptureAttachment;

/**
 * Created by huangjun on 2015/11/25.
 * 屏幕截屏Tip类型消息ViewHolder
 */
public class MsgViewHolderScreenCaptureTip extends MsgViewHolderBase {

    protected TextView notificationTextView;

    public MsgViewHolderScreenCaptureTip(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return com.netease.nim.uikit.R.layout.nim_message_item_notification;
    }

    @Override
    protected void inflateContentView() {
        notificationTextView =  view.findViewById(com.netease.nim.uikit.R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        ScreenCaptureAttachment attachment = (ScreenCaptureAttachment) message.getAttachment();
        String tipId=attachment.getCustomTipId();
        String tipContent=attachment.getCustomTipContent();
        if(NimUIKit.getAccount().equals(tipId)){
            handleTextNotification("你"+tipContent);
        }else{
            handleTextNotification(TeamHelper.getTeamMemberDisplayName(message.getSessionId(),tipId)+tipContent);
        }
    }
    private void handleTextNotification(String text) {
        MoonUtil.identifyFaceExpressionAndATags(context, notificationTextView, text, ImageSpan.ALIGN_BOTTOM);
        notificationTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}
