package com.xr.ychat.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.xr.ychat.main.viewholder.CustomNotificationViewHolder;
import com.xr.ychat.main.viewholder.SystemMessageViewHolder;

import java.util.List;

public class CustomMessageAdapter extends TAdapter {

    private CustomNotificationViewHolder.CustomNotificationListener customNotificationListener;

    public CustomMessageAdapter(Context context, List<?> items, TAdapterDelegate delegate,
                                CustomNotificationViewHolder.CustomNotificationListener customNotificationListener) {
        super(context, items, delegate);
        this.customNotificationListener = customNotificationListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (customNotificationListener != null) {
            ((CustomNotificationViewHolder) view.getTag()).setListener(customNotificationListener);
        }

        return view;
    }
}
