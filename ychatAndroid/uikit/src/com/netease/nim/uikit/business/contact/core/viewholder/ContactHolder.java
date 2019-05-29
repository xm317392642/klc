package com.netease.nim.uikit.business.contact.core.viewholder;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ui.combinebitmap.helper.BitmapLoader;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class ContactHolder extends AbsContactViewHolder<ContactItem> {

    protected HeadImageView head;

    protected TextView name;

    protected TextView desc;

    protected View line;

    @Override
    public void refresh(ContactDataAdapter adapter, int position, final ContactItem item) {
        // contact info
        final IContact contact = item.getContact();
        if (position == adapter.getCount() - 1) {
            line.setVisibility(View.VISIBLE);
        } else {
            AbsContactItem nextContact = (AbsContactItem) adapter.getItem(position + 1);
            if (nextContact.getItemType() == item.getItemType()) {
                line.setVisibility(View.VISIBLE);
            } else {
                line.setVisibility(View.GONE);
            }
        }
        if (contact.getContactType() == IContact.Type.Friend) {
            if (TextUtils.equals(contact.getContactId(), SPUtils.getInstance().getString(CommonUtil.ASSISTANT))) {
                head.setImageResource(R.drawable.nim_avatar_assistant);
            } else {
                head.loadBuddyAvatar(contact.getContactId());
            }
            head.setOnClickListener(v -> {
                if (NimUIKitImpl.getContactEventListener() != null) {
                    NimUIKitImpl.getContactEventListener().onAvatarClick(context, item.getContact().getContactId());
                }
            });
        } else {
            String teamId = contact.getContactId();
            head.loadTeamIconByTeam(NimUIKit.getTeamProvider().getTeamById(teamId));
        }
        TextQuery textQuery = adapter.getQuery();
        if (textQuery != null && !TextUtils.isEmpty(textQuery.text)) {
            name.setText(matcherSearchText(R.color.color_be6913, contact.getDisplayName(), textQuery.text));
        } else {
            name.setText(contact.getDisplayName());
        }

        // query result
        desc.setVisibility(View.GONE);
        /*
        TextQuery query = adapter.getQuery();
        HitInfo hitInfo = query != null ? ContactSearch.hitInfo(contact, query) : null;
        if (hitInfo != null && !hitInfo.text.equals(contact.getDisplayName())) {
            desc.setVisibility(View.VISIBLE);
        } else {
            desc.setVisibility(View.GONE);
        }
        */
    }

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.nim_contacts_item, null);

        head = view.findViewById(R.id.contacts_item_head);
        name = view.findViewById(R.id.contacts_item_name);
        desc = view.findViewById(R.id.contacts_item_desc);
        line = view.findViewById(R.id.contacts_item_line);

        return view;
    }

    private CharSequence matcherSearchText(int color, String string, String keyWord) {
        if (!TextUtils.isEmpty(string)) {
            SpannableStringBuilder builder = new SpannableStringBuilder(string);
            int indexOf = string.indexOf(keyWord);
            if (indexOf != -1) {
                builder.setSpan(new ForegroundColorSpan(context.getResources().getColor(color)), indexOf, indexOf + keyWord.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return builder;
        } else {
            return string;
        }
    }
}
