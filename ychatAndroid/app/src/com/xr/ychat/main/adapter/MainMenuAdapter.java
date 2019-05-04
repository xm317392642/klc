package com.xr.ychat.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xr.ychat.R;
import com.xr.ychat.main.model.MainMenu;

import java.util.List;

public class MainMenuAdapter extends BaseAdapter {
    protected Context context;
    private List<MainMenu> items;

    public MainMenuAdapter(Context context, List<MainMenu> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_main_menu, parent, false);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.main_menu_icon);
            viewHolder.name = convertView.findViewById(R.id.main_menu_name);
            viewHolder.line = convertView.findViewById(R.id.main_menu_line);
            convertView.setTag(viewHolder);
        }
        MainMenu item = items.get(position);
        viewHolder.name.setText(item.getName());
        viewHolder.icon.setImageResource(item.getIcon());
        viewHolder.line.setVisibility((position == items.size() - 1) ? View.GONE : View.VISIBLE);
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        private ImageView icon;
        private TextView name;
        private View line;
    }

}
