package com.xr.ychat.main.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.uikit.impl.preference.UserPreferences;
import com.xr.ychat.R;
import com.xr.ychat.common.util.WxShareUtils;
import com.xr.ychat.contact.activity.AddFriendActivity;
import com.xr.ychat.contact.activity.BlackListActivity;
import com.xr.ychat.main.activity.SystemNotificationActivity;
import com.xr.ychat.main.activity.TeamListActivity;
import com.xr.ychat.main.helper.SystemMessageUnreadManager;
import com.xr.ychat.main.reminder.ReminderId;
import com.xr.ychat.main.reminder.ReminderItem;
import com.xr.ychat.main.reminder.ReminderManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FuncViewHolder extends AbsContactViewHolder<FuncViewHolder.FuncItem> implements ReminderManager.UnreadNumChangedCallback {

    private static ArrayList<WeakReference<ReminderManager.UnreadNumChangedCallback>> sUnreadCallbackRefs = new ArrayList<>();

    private ImageView image;
    private TextView funcName;
    private TextView unreadNum;
    private View line;
    private Set<ReminderManager.UnreadNumChangedCallback> callbacks = new HashSet<>();

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.func_contacts_item, null);
        this.image = view.findViewById(R.id.img_head);
        this.funcName = view.findViewById(R.id.tv_func_name);
        this.unreadNum = view.findViewById(R.id.tab_new_msg_label);
        this.line = view.findViewById(R.id.item_line);
        return view;
    }

    @Override
    public void refresh(ContactDataAdapter contactAdapter, int position, FuncItem item) {
        if (item == FuncItem.VERIFY) {
            funcName.setText("新的朋友");
            image.setImageResource(R.drawable.icon_verify_remind);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            int unreadCount = SystemMessageUnreadManager.getInstance().getSysMsgUnreadCount();
            updateUnreadNum(unreadCount);
            //ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
            sUnreadCallbackRefs.add(new WeakReference<ReminderManager.UnreadNumChangedCallback>(this));
        } else if (item == FuncItem.ADVANCED_TEAM) {
            funcName.setText("已保存的群聊");
            image.setImageResource(R.drawable.ic_advanced_team);
        } else if (item == FuncItem.INVITE_WECHAT_FRIEND) {
            funcName.setText("邀请微信好友");
            image.setImageResource(R.mipmap.wechat);
        }
//        else if (item == FuncItem.BLACK_LIST) {
//            funcName.setText("黑名单");
//            image.setImageResource(R.drawable.ic_black_list);
//        }
        else if (item == FuncItem.ADD_FRIEND) {
            funcName.setText("添加好友");
            image.setImageResource(R.drawable.ic_new_friend);
        }
        if (item != FuncItem.VERIFY) {
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            unreadNum.setVisibility(View.GONE);
        }
        line.setVisibility(item != FuncItem.INVITE_WECHAT_FRIEND ? View.VISIBLE : View.INVISIBLE);
    }


    private void updateUnreadNum(int unreadCount) {
        // 2.*版本viewholder复用问题
        //      if (unreadCount > 0 && funcName.getText().toString().equals("验证提醒")) {
        if (unreadCount > 0) {
            unreadNum.setVisibility(View.VISIBLE);
            unreadNum.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        } else {
            unreadNum.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        if (item.getId() != ReminderId.CONTACT) {
            return;
        }
        updateUnreadNum(item.getUnread());
    }

    public static void unRegisterUnreadNumChangedCallback() {
        Iterator<WeakReference<ReminderManager.UnreadNumChangedCallback>> iter = sUnreadCallbackRefs.iterator();
        while (iter.hasNext()) {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(iter.next().get());
            iter.remove();
        }
    }


    public final static class FuncItem extends AbsContactItem {
        static final FuncItem ADD_FRIEND = new FuncItem();
        static final FuncItem VERIFY = new FuncItem();
        static final FuncItem ADVANCED_TEAM = new FuncItem();
        static final FuncItem BLACK_LIST = new FuncItem();
        static final FuncItem INVITE_WECHAT_FRIEND = new FuncItem();

        @Override
        public int getItemType() {
            return ItemTypes.FUNC;
        }

        @Override
        public String belongsGroup() {
            return null;
        }

        public static List<AbsContactItem> provide() {

            if (UserPreferences.getShare()) {
                return null;
            } else {
                List<AbsContactItem> items = new ArrayList<>();
                items.add(VERIFY);
                items.add(ADD_FRIEND);
                items.add(ADVANCED_TEAM);
                items.add(INVITE_WECHAT_FRIEND);
                //items.add(BLACK_LIST);
                return items;
            }

        }


        public static void handle(Context context, AbsContactItem item) {
            if (item == VERIFY) {
                //SystemMessageActivity.start(context);
                SystemNotificationActivity.start(context);
            } else if (item == ADVANCED_TEAM) {
                TeamListActivity.start(context, ItemTypes.TEAMS.ADVANCED_TEAM);
            } else if (item == BLACK_LIST) {
                BlackListActivity.start(context);
            } else if (item == ADD_FRIEND) {
                AddFriendActivity.start(context);
            } else if (item == INVITE_WECHAT_FRIEND) {
                WxShareUtils.share(context);
            }
        }
    }
}
