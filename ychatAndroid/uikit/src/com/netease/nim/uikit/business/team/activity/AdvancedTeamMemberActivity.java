package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.team.TeamMemberDataChangedObserver;
import com.netease.nim.uikit.api.model.team.TeamProvider;
import com.netease.nim.uikit.api.model.user.UserInfoObserver;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter.TeamMemberItem;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.impl.cache.TeamDataCache;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 群成员列表界面
 * Created by hzxuwen on 2015/3/17.
 */
public class AdvancedTeamMemberActivity extends SwipeBackUI implements TAdapterDelegate,
        TeamMemberAdapter.RemoveMemberCallback, TeamMemberAdapter.AddMemberCallback, TeamMemberHolder.TeamMemberHolderEventListener {
    private static final int REQUEST_CODE_CONTACT_SELECT = 103;
    private static final int REQUEST_CODE_CONTACT_SELECT_DELETE = 105;
    private Toolbar mToolbar;
    private TextView toolbarTitle;

    // constant
    private static final String EXTRA_ID = "EXTRA_ID";
    public static final String EXTRA_DATA = "EXTRA_DATA";

    // data source
    private String teamId;
    private List<TeamMember> members;
    private TeamMemberAdapter adapter;
    private List<String> memberAccounts;
    private List<TeamMemberAdapter.TeamMemberItem> dataSource;
    private String creator;
    private List<String> managerList;

    // state
    private boolean isSelfAdmin = false;
    private boolean isSelfManager = false;
    private boolean isMemberChange = false;
    private UserInfoObserver userInfoObserver;
    private Team team;
    private String robotId;

    public static void startActivityForResult(Activity context, String tid, int resCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, AdvancedTeamMemberActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, resCode);
    }

    /**
     * *********************************lifeCycle*******************************************
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_team_member_grid_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText(R.string.team_member);
        mToolbar.setNavigationOnClickListener(v -> finish());

        parseIntentData();
        loadTeamInfo();
        initAdapter();
        findViews();
        registerUserInfoChangedObserver(true);
        requestData();
        registerObservers(true);
    }

    @Override
    protected void onDestroy() {
        registerUserInfoChangedObserver(false);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA, isMemberChange);
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void loadTeamInfo() {
        this.team = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (team != null) {
            creator = team.getCreator();
            robotId = getRobotId(team);
        }
    }

    private void findViews() {
        TeamInfoGridView teamInfoGridView = (TeamInfoGridView) findViewById(R.id.team_member_grid);
        teamInfoGridView.setSelector(R.color.transparent);
        teamInfoGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        teamInfoGridView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && adapter.getMode() == TeamMemberAdapter.Mode.DELETE) {
                    adapter.setMode(TeamMemberAdapter.Mode.NORMAL);
                    adapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
        teamInfoGridView.setAdapter(adapter);
    }

    private void initAdapter() {
        memberAccounts = new ArrayList<>();
        members = new ArrayList<>();
        dataSource = new ArrayList<>();
        managerList = new ArrayList<>();
        adapter = new TeamMemberAdapter(this, dataSource, this, this, this);
        adapter.setEventListener(this);
    }

    private void updateTeamMember(final List<TeamMember> members) {
        if (members != null && members.isEmpty()) {
            return;
        }

        addTeamMembers(members, true);
    }

    private void addTeamMembers(final List<TeamMember> m, boolean clear) {
        if (m == null || m.isEmpty()) {
            return;
        }

        if (clear) {
            this.members.clear();
            this.memberAccounts.clear();
        }

        // add
        if (this.members.isEmpty()) {
            this.members.addAll(m);
        } else {
            for (TeamMember tm : m) {
                if (!this.memberAccounts.contains(tm.getAccount())) {
                    this.members.add(tm);
                }
            }
        }

        // sort
        Collections.sort(this.members, TeamHelper.teamMemberComparator);

        // accounts, manager, creator
        this.memberAccounts.clear();
        this.managerList.clear();
        for (TeamMember tm : members) {
            initManagerList(tm);
            if (tm.getAccount().equals(NimUIKit.getAccount())) {
                if (tm.getType() == TeamMemberType.Manager) {
                    isSelfManager = true;
                } else if (tm.getType() == TeamMemberType.Owner) {
                    isSelfAdmin = true;
                    creator = NimUIKit.getAccount();
                }
            }
            if (!TextUtils.equals(tm.getAccount(), robotId)) {
                this.memberAccounts.add(tm.getAccount());
            }
        }

        updateTeamMemberDataSource();
    }

    /**
     * 初始化管理员列表
     *
     * @param tm 群成员
     */
    private void initManagerList(TeamMember tm) {
        if (tm.getType() == TeamMemberType.Manager) {
            managerList.add(tm.getAccount());
        }
    }

    private void updateTeamMemberDataSource() {
        if (members.size() <= 0) {
            return;
        }

        dataSource.clear();

        // member item
        for (String account : memberAccounts) {
            dataSource.add(new TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                    .NORMAL, teamId, account, initMemberIdentity(account)));
        }


        // add item
        if (team.getTeamInviteMode() == TeamInviteModeEnum.All || isSelfAdmin || isSelfManager) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null,
                    null));
        }
        // remove item
        //只有群主和管理员才有移除人的权限  并且  有群成员的情况下才添加删除图标
        if ((isSelfAdmin || isSelfManager) && memberAccounts.size() > 1) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.DELETE, null, null,
                    null));
        }

        // refresh
        adapter.notifyDataSetChanged();
    }

    /**
     * 初始化成员身份
     *
     * @param account 帐号
     * @return String
     */
    private String initMemberIdentity(String account) {
        String identity;
        if (creator.equals(account)) {
            identity = TeamMemberHolder.OWNER;
        } else if (managerList.contains(account)) {
            identity = TeamMemberHolder.ADMIN;
        } else {
            identity = null;
        }
        return identity;
    }


    /**
     * 从联系人选择器发起邀请成员
     */
    @Override
    public void onAddMember() {
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        option.title = "邀请成员";
        NimUIKit.startContactSelector(this, option, REQUEST_CODE_CONTACT_SELECT);
    }

    /**
     * 选择删除的群成员
     *
     * @param account
     */
    @Override
    public void onRemoveMember(String account) {
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        option.title = "删除成员";
        option.teamId = teamId;
        option.multi = true;
        option.itemDisableFilter = null;
        //需要过滤（不显示）的联系人项
        ArrayList<String> includeAccounts = new ArrayList<>();
        memberAccounts.remove(NimUIKit.getAccount());
        includeAccounts.addAll(memberAccounts);
        option.itemFilter = new ContactIdFilter(includeAccounts, false);
        NimUIKit.startContactSelector(this, option, REQUEST_CODE_CONTACT_SELECT_DELETE);
    }

    /**
     * ******************************加载数据*******************************
     */
    private void requestData() {
        NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> members, int code) {
                if (success && members != null && !members.isEmpty()) {
                    updateTeamMember(members);
                }
            }
        });
    }

    /**
     * ************************ TAdapterDelegate **************************
     */

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return TeamMemberHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public void onHeadImageViewClick(String account) {
        AdvancedTeamMemberInfoActivity.startActivityForResult(AdvancedTeamMemberActivity.this, account, teamId, TextUtils.equals(account, creator));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case AdvancedTeamMemberInfoActivity.REQ_CODE_REMOVE_MEMBER:
                    boolean isSetAdmin = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISADMIN, false);
                    boolean isRemoveMember = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISREMOVE, false);
                    String account = data.getStringExtra(EXTRA_ID);
                    refreshAdmin(isSetAdmin, account);
                    if (isRemoveMember) {
                        removeMember(account);
                    }
                    break;
                case REQUEST_CODE_CONTACT_SELECT:
                    final ArrayList<String> addSelected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                    if (addSelected != null && !addSelected.isEmpty()) {
                        inviteMembers(addSelected);
                    }
                    break;
                case REQUEST_CODE_CONTACT_SELECT_DELETE:
                    final ArrayList<String> removeSelected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                    if (removeSelected != null && !removeSelected.isEmpty()) {
                        removeMembers(removeSelected);
                    }
                    break;
            }
        }
    }

    /**
     * 删除选中的群成员
     *
     * @param accounts
     */
    private void removeMembers(ArrayList<String> accounts) {
        NIMClient.getService(TeamService.class).removeMembers(teamId, accounts).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                YchatToastUtils.showShort("删除群成员成功");
            }

            @Override
            public void onFailed(int code) {
                YchatToastUtils.showShort("delete members failed,code=" + code);
            }

            @Override
            public void onException(Throwable exception) {
                YchatToastUtils.showShort("exception=" + exception.getMessage());

            }
        });
    }

    /**
     * 邀请群成员
     *
     * @param accounts 邀请帐号
     */
    private void inviteMembers(ArrayList<String> accounts) {
        NIMClient.getService(TeamService.class).addMembers(teamId, accounts).setCallback(new RequestCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> failedAccounts) {
                if (failedAccounts == null || failedAccounts.isEmpty()) {
                    YchatToastUtils.showShort("添加群成员成功");
                } else {
                    TeamHelper.onMemberTeamNumOverrun(failedAccounts, AdvancedTeamMemberActivity.this);
                }
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_TEAM_INVITE_SUCCESS) {
                    YchatToastUtils.showShort(R.string.team_invite_members_success);
                } else {
                    YchatToastUtils.showShort("invite members failed, code=" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    /**
     * 移除群成员成功后，删除列表中的群成员
     */
    private void removeMember(String account) {


        if (TextUtils.isEmpty(account)) {
            return;
        }

        memberAccounts.remove(account);

        for (TeamMember m : members) {
            if (m.getAccount().equals(account)) {
                members.remove(m);
                break;
            }
        }


        for (TeamMemberItem item : dataSource) {
            if (item.getAccount() != null && item.getAccount().equals(account)) {
                dataSource.remove(item);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 是否设置了管理员刷新界面
     *
     * @param isSetAdmin 是否设置为管理员
     * @param account    帐号
     */
    private void refreshAdmin(boolean isSetAdmin, String account) {
        if (isSetAdmin) {
            if (managerList.contains(account)) {
                return;
            }
            managerList.add(account);
            isMemberChange = true;
            updateTeamMemberDataSource();
        } else {
            if (managerList.contains(account)) {
                managerList.remove(account);
                isMemberChange = true;
                updateTeamMemberDataSource();
            }
        }
    }

    private void registerUserInfoChangedObserver(boolean register) {
        if (register) {
            if (userInfoObserver == null) {
                userInfoObserver = new UserInfoObserver() {
                    @Override
                    public void onUserInfoChanged(List<String> accounts) {
                        adapter.notifyDataSetChanged();
                    }
                };
            }
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true);
        } else {
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false);
        }
    }


    private void registerObservers(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberObserver, register);
        //NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
        registerUserInfoChangedObserver(register);
    }

    TeamMemberDataChangedObserver teamMemberObserver = new TeamMemberDataChangedObserver() {

        @Override
        public void onUpdateTeamMember(List<TeamMember> m) {
            for (TeamMember mm : m) {
                for (TeamMember member : members) {
                    if (mm.getAccount().equals(member.getAccount())) {
                        members.set(members.indexOf(member), mm);
                        break;
                    }
                }
            }
            addTeamMembers(m, false);
        }

        @Override
        public void onRemoveTeamMember(List<TeamMember> members) {
            for (TeamMember member : members) {
                removeMember(member.getAccount());
            }
        }
    };

    private String getRobotId(Team team) {
        if (team == null) {
            return null;
        }
        TeamExtension extension;
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                Gson gson = new Gson();
                extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
            } catch (Exception exception) {
                extension = new TeamExtension();
            }
        } else {
            extension = new TeamExtension();
        }
        return extension.getRobotId();
    }
}
