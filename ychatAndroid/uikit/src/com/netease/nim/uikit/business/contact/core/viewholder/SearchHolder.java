package com.netease.nim.uikit.business.contact.core.viewholder;

import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.SearchItem;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.common.CommonUtil;

public class SearchHolder extends AbsContactViewHolder<SearchItem> {
    private View assistantLayout;

    public void refresh(ContactDataAdapter contactAdapter, int position, SearchItem item) {
        assistantLayout.setOnClickListener(v -> {
            NimUIKit.startP2PSession(context, SPUtils.getInstance().getString(CommonUtil.ASSISTANT));
        });
    }

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.nim_contact_assistant_item, null);
        assistantLayout = view.findViewById(R.id.assistant_layout);
        return view;
    }
}
