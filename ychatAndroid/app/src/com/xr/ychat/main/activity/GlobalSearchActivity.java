package com.xr.ychat.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.nim.uikit.business.contact.core.model.TeamMemberContact;
import com.netease.nim.uikit.business.contact.core.provider.ContactDataProvider;
import com.netease.nim.uikit.business.contact.core.query.IContactDataProvider;
import com.netease.nim.uikit.business.contact.core.viewholder.ContactHolder;
import com.netease.nim.uikit.business.contact.core.viewholder.ContactTeamMemberHolder;
import com.netease.nim.uikit.business.contact.core.viewholder.LabelHolder;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.impl.preference.UserPreferences;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.xr.ychat.R;
import com.xr.ychat.session.SessionHelper;

/**
 * 全局搜索页面
 * 支持通讯录搜索、消息全文检索
 * <p/>
 * Created by huangjun on 2015/4/13.
 */
public class GlobalSearchActivity extends UI implements OnItemClickListener {

    private ContactDataAdapter adapter;

    private ListView lvContacts;

    private SearchView searchView;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, GlobalSearchActivity.class);
        context.startActivity(intent);
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.global_search_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_search);

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                MenuItemCompat.expandActionView(item);
            }
        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                finish();

                return false;
            }
        });

        searchView = (SearchView) MenuItemCompat.getActionView(item);

        //set color to searchview
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.gray7));
        searchAutoComplete.setHint("搜索");
        searchAutoComplete.setTextColor(getResources().getColor(android.R.color.white));

        ImageView closeViewIcon = searchView.findViewById(R.id.search_close_btn);
        closeViewIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_close));
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                showKeyboard(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (StringUtil.isEmpty(query)) {
                    lvContacts.setVisibility(View.GONE);
                } else {
                    lvContacts.setVisibility(View.VISIBLE);
                }
                adapter.query(query);
                return true;
            }
        });
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.global_search_result);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(mToolbar);

        lvContacts = (ListView) findViewById(R.id.searchResultList);
        lvContacts.setVisibility(View.GONE);
        SearchGroupStrategy searchGroupStrategy = new SearchGroupStrategy();
        IContactDataProvider dataProvider = new ContactDataProvider(ItemTypes.FRIEND, ItemTypes.TEAM, ItemTypes.TEAM_MEMBER);

        adapter = new ContactDataAdapter(this, searchGroupStrategy, dataProvider);
        adapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        adapter.addViewHolder(ItemTypes.FRIEND, ContactHolder.class);
        adapter.addViewHolder(ItemTypes.TEAM, ContactHolder.class);
        adapter.addViewHolder(ItemTypes.TEAM_MEMBER, ContactTeamMemberHolder.class);

        lvContacts.setAdapter(adapter);
        lvContacts.setOnItemClickListener(this);
        lvContacts.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                showKeyboard(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        findViewById(R.id.global_search_root).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    private static class SearchGroupStrategy extends ContactGroupStrategy {
        public static final String GROUP_FRIEND = "FRIEND";
        public static final String GROUP_TEAM = "TEAM";
        public static final String GROUP_MEMBERS = "MEMBERS";

        SearchGroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, 0, "");
            add(GROUP_TEAM, 1, "群聊");
            add(GROUP_FRIEND, 2, "联系人");
            add(GROUP_MEMBERS, 3, "群聊");
        }

        @Override
        public String belongs(AbsContactItem item) {
            switch (item.getItemType()) {
                case ItemTypes.FRIEND:
                    return GROUP_FRIEND;
                case ItemTypes.TEAM:
                    return GROUP_TEAM;
                case ItemTypes.TEAM_MEMBER:
                    return GROUP_MEMBERS;
                default:
                    return null;
            }
        }
    }
//    private  void jump(int itemType,String accountId){
//        if(itemType==ItemTypes.FRIEND){
//            if(UserPreferences.getShare()){//分享的话，就弹出对话框
//                String value=UserPreferences.getShareValue();
//                if(UserPreferences.SHARE_IMG.equals(value)){
//                    CommonUtil.sharePicDialog(this,accountId, SessionTypeEnum.P2P,null,null);
//                }else if(UserPreferences.SHARE_URL.equals(value)){
//                    CommonUtil.shareGameDialog(this,accountId, SessionTypeEnum.P2P,null,null);
//                }
//            }else{
//                NimUIKit.startP2PSession(this, accountId);
//            }
//        }else{
//            if(UserPreferences.getShare()){//分享的话，就弹出对话框
//                String value=UserPreferences.getShareValue();
//                if(UserPreferences.SHARE_IMG.equals(value)){
//                    CommonUtil.sharePicDialog(this,accountId, SessionTypeEnum.Team,null,null);
//                }else if(UserPreferences.SHARE_URL.equals(value)){
//                    CommonUtil.shareGameDialog(this,accountId, SessionTypeEnum.Team,null,null);
//                }
//            }else{
//                SessionHelper.startTeamSession(this,accountId);
//            }
//        }
//    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AbsContactItem item = (AbsContactItem) adapter.getItem(position);
        switch (item.getItemType()) {
            case ItemTypes.TEAM: {
                SessionHelper.startTeamSession(this, ((ContactItem) item).getContact().getContactId());
                break;
            }
            case ItemTypes.FRIEND: {

                NimUIKit.startP2PSession(this, ((ContactItem) item).getContact().getContactId());
                break;
            }
            case ItemTypes.TEAM_MEMBER: {
                SessionHelper.startTeamSession(this, ((TeamMemberContact) ((ContactItem) item).getContact()).teamMember.getTid());
                break;
            }
//            case ItemTypes.MSG: {
//                MsgIndexRecord msgIndexRecord = ((MsgItem) item).getRecord();
//                if (msgIndexRecord.getCount() > 1) {
//                    GlobalSearchDetailActivity2.start(this, msgIndexRecord);
//                } else {
//                    DisplayMessageActivity.start(this, msgIndexRecord.getMessage());
//                }
//                break;
//            }
            default:
                break;
        }
    }

}
