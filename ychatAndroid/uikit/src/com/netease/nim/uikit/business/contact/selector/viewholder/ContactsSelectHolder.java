package com.netease.nim.uikit.business.contact.selector.viewholder;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.uikit.business.contact.selector.adapter.ContactSelectAdapter;
import com.netease.nim.uikit.common.ui.combinebitmap.helper.BitmapLoader;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ContactsSelectHolder extends AbsContactViewHolder<ContactItem> {
    private final boolean multi;

    private HeadImageView image;

    private TextView nickname;

    private ImageView select;

    private Drawable defaultBackground;

    public ContactsSelectHolder() {
        this(false);
    }

    ContactsSelectHolder(boolean multi) {
        this.multi = multi;
    }

    @Override
    public void refresh(ContactDataAdapter adapter, int position, ContactItem item) {
        if (multi) {
            boolean disabled = !adapter.isEnabled(position);
            boolean selected = adapter instanceof ContactSelectAdapter ? ((ContactSelectAdapter) adapter).isSelected(position) : false;
            this.select.setVisibility(View.VISIBLE);
            if (disabled) {
                this.select.setBackgroundResource(R.drawable.nim_contact_checkbox_checked_grey);
                getView().setBackgroundColor(context.getResources().getColor(R.color.transparent));
            } else if (selected) {
                setBackground(getView(), defaultBackground);
                this.select.setBackgroundResource(R.drawable.nim_contact_checkbox_checked_green);
            } else {
                setBackground(getView(), defaultBackground);
                this.select.setBackgroundResource(R.drawable.nim_contact_checkbox_unchecked);
            }
        } else {
            this.select.setVisibility(View.GONE);
        }

        IContact contact = item.getContact();
        this.nickname.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        this.nickname.setText(contact.getDisplayName());
        if (contact.getContactType() == IContact.Type.Friend || contact.getContactType() == IContact.Type.TeamMember) {
            this.nickname.setText(contact.getDisplayName());
            this.image.loadBuddyAvatar(contact.getContactId());
        } else if (contact.getContactType() == IContact.Type.Team) {
            String teamId = contact.getContactId();
            NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallback<List<TeamMember>>() {
                @Override
                public void onSuccess(List<TeamMember> param) {
                    if (param != null && param.size() > 0) {
                        BitmapLoader.getInstance(context).removeBitmapToMemoryCache("ychat://com.xr.ychat?groupId=" + teamId);
                        image.loadTeamIconByTeam(param, teamId);
                    }
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }
        image.setVisibility(View.VISIBLE);
    }

    private boolean needRefresh(List<TeamMember> teamMembers) {
        if (teamMembers == null) {
            return true;
        }
        if (teamMembers.size() == 0) {
            return true;
        }
        if (teamMembers.size() == 1) {
            TeamMember teamMember = teamMembers.get(0);
            if (teamMember.getType() == TeamMemberType.Owner) {
                return true;
            }
        }
        boolean hasOwner = true;
        for (TeamMember teamMember : teamMembers) {
            if (teamMember.getType() == TeamMemberType.Owner) {
                hasOwner = false;
                break;
            }
        }
        return hasOwner;
    }

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.nim_contacts_select_item, null);
        defaultBackground = view.getBackground();
        this.image = view.findViewById(R.id.img_head);
        this.nickname = view.findViewById(R.id.tv_nickname);
        this.select = view.findViewById(R.id.imgSelect);
        return view;
    }

    private void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
