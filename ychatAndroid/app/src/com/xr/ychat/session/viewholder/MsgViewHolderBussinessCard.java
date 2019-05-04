package com.xr.ychat.session.viewholder;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.avchatkit.common.util.ScreenUtil;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.xr.ychat.R;
import com.xr.ychat.contact.activity.UserProfileActivity;
import com.xr.ychat.session.extension.BussinessCardAttachment;

public class MsgViewHolderBussinessCard extends MsgViewHolderBase {
    private HeadImageView avatar;
    private TextView txName,txBottom;
    private View line;

    public MsgViewHolderBussinessCard(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.bussiness_card_item;
    }

    @Override
    protected void inflateContentView() {
        avatar = findViewById(R.id.avatar);
        txName = findViewById(R.id.tx_name);
        line = findViewById(R.id.line);
        txBottom = findViewById(R.id.tx_bottom);
    }

    @Override
    protected void bindContentView() {
        BussinessCardAttachment attachment = (BussinessCardAttachment) message.getAttachment();
        txName.setText(attachment.getPersonCardUserName());
        avatar.loadAvatar(attachment.getPersonCardUserImage());

        ConstraintLayout.LayoutParams layoutParams1= (ConstraintLayout.LayoutParams) avatar.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParams2= (ConstraintLayout.LayoutParams) line.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParams3= (ConstraintLayout.LayoutParams) txBottom.getLayoutParams();


        if(message.getDirect()== MsgDirectionEnum.In){
            layoutParams1.leftMargin= ScreenUtil.dip2px(15f);
            layoutParams2.leftMargin= ScreenUtil.dip2px(15f);
            layoutParams3.leftMargin= ScreenUtil.dip2px(15f);

            layoutParams1.rightMargin= ScreenUtil.dip2px(10f);
            layoutParams2.rightMargin= ScreenUtil.dip2px(10f);
            layoutParams3.rightMargin= ScreenUtil.dip2px(10f);
        }else{
            layoutParams1.leftMargin= ScreenUtil.dip2px(10f);
            layoutParams2.leftMargin= ScreenUtil.dip2px(10f);
            layoutParams3.leftMargin= ScreenUtil.dip2px(10f);

            layoutParams1.rightMargin= ScreenUtil.dip2px(15f);
            layoutParams2.rightMargin= ScreenUtil.dip2px(15f);
            layoutParams3.rightMargin= ScreenUtil.dip2px(15f);
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
        UserProfileActivity.start(context,((BussinessCardAttachment) message.getAttachment()).getPersonCardUserid());
    }
}
