package com.xr.ychat.session.viewholder;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.EncodeUtils;
import com.netease.nim.avchatkit.common.util.ScreenUtil;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.xr.ychat.R;
import com.xr.ychat.session.extension.GameShareAttachment;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class MsgViewHolderGameShare extends MsgViewHolderBase {
    private HeadImageView img_shareLinkImage, img_shareLinkSourceImage;
    private TextView tx_shareLinkTitle, tx_shareLinkDes, tx_shareLinkSourceName;
    private View line;

    public MsgViewHolderGameShare(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.game_share_card_item;
    }

    @Override
    protected void inflateContentView() {
        img_shareLinkImage = findViewById(R.id.img_shareLinkImage);
        img_shareLinkSourceImage = findViewById(R.id.img_shareLinkSourceImage);
        tx_shareLinkTitle = findViewById(R.id.tx_shareLinkTitle);
        tx_shareLinkDes = findViewById(R.id.tx_shareLinkDes);
        tx_shareLinkSourceName = findViewById(R.id.tx_shareLinkSourceName);
        line = findViewById(R.id.line);
    }

    @Override
    protected void bindContentView() {
        GameShareAttachment attachment = (GameShareAttachment) message.getAttachment();
        tx_shareLinkTitle.setText(attachment.getShareLinkTitle());
        tx_shareLinkDes.setText(attachment.getShareLinkDes());
        tx_shareLinkSourceName.setText(attachment.getShareLinkSourceName());
        img_shareLinkImage.loadAvatar(attachment.getShareLinkImage());
        img_shareLinkSourceImage.loadAvatar(attachment.getShareLinkSourceImage());
        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) tx_shareLinkTitle.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) tx_shareLinkDes.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) img_shareLinkImage.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) line.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) img_shareLinkSourceImage.getLayoutParams();


        if (message.getDirect() == MsgDirectionEnum.In) {
            layoutParams1.leftMargin = ScreenUtil.dip2px(15f);
            layoutParams2.leftMargin = ScreenUtil.dip2px(15f);
            layoutParams4.leftMargin = ScreenUtil.dip2px(15f);
            layoutParams5.leftMargin = ScreenUtil.dip2px(15f);

            layoutParams1.rightMargin = ScreenUtil.dip2px(10f);
            layoutParams2.rightMargin = ScreenUtil.dip2px(10f);
            layoutParams3.rightMargin = ScreenUtil.dip2px(10f);
            layoutParams4.rightMargin = ScreenUtil.dip2px(10f);
            layoutParams5.rightMargin = ScreenUtil.dip2px(3f);
        } else {
            layoutParams1.leftMargin = ScreenUtil.dip2px(10f);
            layoutParams2.leftMargin = ScreenUtil.dip2px(10f);
            layoutParams4.leftMargin = ScreenUtil.dip2px(10f);
            layoutParams5.leftMargin = ScreenUtil.dip2px(10f);

            layoutParams1.rightMargin = ScreenUtil.dip2px(15f);
            layoutParams2.rightMargin = ScreenUtil.dip2px(15f);
            layoutParams3.rightMargin = ScreenUtil.dip2px(15f);
            layoutParams4.rightMargin = ScreenUtil.dip2px(15f);
            layoutParams5.rightMargin = ScreenUtil.dip2px(3f);
        }
    }

    @Override
    protected int leftBackground() {

        return R.drawable.nim_message_item_left_bc;
    }

    @Override
    protected int rightBackground() {
        return R.drawable.nim_message_item_right_bc;
    }

    @Override
    protected void onItemClick() {
        String url = ((GameShareAttachment) message.getAttachment()).getShareLinkUrl();
        Uri uri=Uri.parse(url);
        String codeValue= CommonUtil.getValueWithKey(uri,"code");
        //如果uri包含code，则把code的值保存到剪切板中
        if(!TextUtils.isEmpty(codeValue)){
            ClipboardManager mClipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);// 1.获取剪贴板服务
            ClipData clip = ClipData.newPlainText("code",codeValue);// 2.然后把数据放在ClipData对象中(第一个参数，是描述复制的内容)
            mClipboardManager.setPrimaryClip(clip);//3.把clip对象放在剪贴板中
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}
