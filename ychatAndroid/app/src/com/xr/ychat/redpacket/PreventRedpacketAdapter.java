package com.xr.ychat.redpacket;

import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.R;

import java.util.List;

public class PreventRedpacketAdapter extends BaseQuickAdapter<RedpacketInfo, BaseViewHolder> {
    private NimUserInfo userInfo;
    private HeadImageView headImageView;
    private TextView accountText;
    private Button removeBtn;
    private PreventRedpacketInteface inteface;

    public PreventRedpacketAdapter(RecyclerView recyclerView, List<RedpacketInfo> data, PreventRedpacketInteface inteface) {
        super(recyclerView, R.layout.black_list_item, data);
        this.inteface = inteface;
    }

    @Override
    protected void convert(BaseViewHolder helper, RedpacketInfo item, int position, boolean isScrolling) {
        headImageView = helper.getView(R.id.head_image);
        headImageView.loadBuddyAvatar(item.getAccid());
        removeBtn = helper.getView(R.id.remove);
        removeBtn.setOnClickListener(v -> {
            inteface.removePreventRedpacket(item.getAccid());
        });
        accountText = helper.getView(R.id.account);
        userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(item.getAccid());
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(item.getAccid(), new SimpleCallback<NimUserInfo>() {

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
        accountText.setText(String.format("%1$s", userInfo.getName()));
    }

    public interface PreventRedpacketInteface {
        void removePreventRedpacket(String accid);
    }

}
