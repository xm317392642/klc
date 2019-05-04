package com.xr.ychat.redpacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.team.TeamProvider;
import com.netease.nim.uikit.business.ait.selector.AitContactDecoration;
import com.netease.nim.uikit.business.ait.selector.model.ItemType;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.core.provider.TeamDataProvider;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.xr.ychat.R;

import java.util.ArrayList;
import java.util.List;

public class PreventRedpacketActivity extends SwipeBackUI implements PreventRedpacketAdapter.PreventRedpacketInteface {
    private static final int REQUEST_CODE_PREVENT_REDPACKET = 101;//转让群
    public static final String EXTRA_TEAM_ID = "team_id";
    private ImageView toolbarAdd;
    private String uid;
    private String myToken;
    private RecyclerView recyclerView;
    private PreventRedpacketAdapter adapter;
    private List<RedpacketInfo> items;
    private int pageNumber;
    private View emptyView;
    private TextView content;
    private String teamId;
    private Team team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prevent_redpacket);
        initActionbar();
        initAdapter();
        Uri data = getIntent().getData();
        teamId = data.getQueryParameter(EXTRA_TEAM_ID);
        team = NimUIKit.getTeamProvider().getTeamById(teamId);
        uid = Preferences.getWeiranUid(this);
        myToken = Preferences.getWeiranToken(this);
        pageNumber = 0;
        fetchPreventRedpacketMemberList(uid, myToken, teamId, pageNumber);
    }

    private void initActionbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        toolbarAdd = (ImageView) findViewById(R.id.toolbar_add);
        toolbarAdd.setOnClickListener(v -> {
            ContactSelectActivity.Option option = new ContactSelectActivity.Option();
            option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
            option.title = "添加群成员";
            option.teamId = teamId;
            option.multi = true;
            option.searchVisible = true;
            String userAccount = NimUIKit.getAccount();
            ArrayList<String> includeAccounts = new ArrayList<>();
            String robotId = getRobotId(team);
            if (!TextUtils.isEmpty(robotId)) {
                includeAccounts.add(robotId);
            }
            includeAccounts.add(userAccount);
            List<TeamMember> teamMembers = NimUIKit.getTeamProvider().getTeamMemberList(teamId);
            for (TeamMember teamMember : teamMembers) {
                if (teamMember.getType() != TeamMemberType.Normal && !includeAccounts.contains(teamMember.getAccount())) {
                    includeAccounts.add(teamMember.getAccount());
                }
            }
            option.itemFilter = new ContactIdFilter(includeAccounts, true);
            NimUIKit.startContactSelector(PreventRedpacketActivity.this, option, REQUEST_CODE_PREVENT_REDPACKET);
        });
    }

    private void initAdapter() {
        emptyView = LayoutInflater.from(PreventRedpacketActivity.this).inflate(com.netease.nim.uikit.R.layout.empty_team_member_change, null);
        content = emptyView.findViewById(com.netease.nim.uikit.R.id.content);
        content.setText("没有群成员被禁止收发红包");
        items = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreventRedpacketAdapter(recyclerView, items, this::removePreventRedpacket);
        recyclerView.setAdapter(adapter);
        List<Integer> noDividerViewTypes = new ArrayList<>(1);
        noDividerViewTypes.add(ItemType.SIMPLE_LABEL);
        recyclerView.addItemDecoration(new AitContactDecoration(PreventRedpacketActivity.this, LinearLayoutManager.VERTICAL, noDividerViewTypes));
        adapter.setOnLoadMoreListener(() -> {
            fetchPreventRedpacketMemberList(uid, myToken, teamId, pageNumber++);
        });
    }

    /**
     * 获取红包操作禁止列表
     */
    private void fetchPreventRedpacketMemberList(String uid, String mytoken, String qunID, int page) {
        if (!NetworkUtil.isNetAvailable(PreventRedpacketActivity.this)) {
            YchatToastUtils.showShort(com.netease.nim.uikit.R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.getInstance().fetchPreventRedpacketMemberList(uid, mytoken, qunID, page, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                if (aVoid.getData() != null && aVoid.getData().size() > 0) {
                    if (page == 0) {
                        adapter.setNewData(aVoid.getData());
                    } else {
                        adapter.addData(aVoid.getData());
                    }
                    boolean hasNextPage = aVoid.getData().size() >= 10;
                    adapter.setEnableLoadMore(hasNextPage);
                    if (hasNextPage) {
                        adapter.loadMoreComplete();
                    } else {
                        adapter.loadMoreEnd();
                    }
                } else {
                    if (page == 0) {
                        adapter.setNewData(null);
                        adapter.setEmptyView(emptyView);
                    } else {
                        adapter.loadMoreEnd();
                    }
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                handlerError(page);
            }
        });
    }

    private void handlerError(int page) {
        if (page == 0) {
            adapter.setNewData(null);
            adapter.setEmptyView(emptyView);
        } else {
            adapter.loadMoreFail();
        }
    }

    @Override
    public void removePreventRedpacket(String accid) {
        TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
        if (teamMember.getType() == TeamMemberType.Owner || teamMember.getType() == TeamMemberType.Manager) {
            updatePreventRedpacketMemberList(uid, myToken, teamId, 2, accid);
        } else {
            YchatToastUtils.showShort("没有权限移出成员禁止收发红包");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_PREVENT_REDPACKET:
                final ArrayList<String> addSelected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (addSelected != null && !addSelected.isEmpty()) {
                    TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
                    if (teamMember.getType() == TeamMemberType.Owner || teamMember.getType() == TeamMemberType.Manager) {
                        updatePreventRedpacketMemberList(uid, myToken, teamId, 1, createMembersString(addSelected));
                    } else {
                        YchatToastUtils.showShort("没有权限添加成员禁止收发红包");
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置能否发送或者能否接收红包
     */
    private void updatePreventRedpacketMemberList(String uid, String mytoken, String qunID, int IsAdd, String DoAccID) {
        if (!NetworkUtil.isNetAvailable(PreventRedpacketActivity.this)) {
            YchatToastUtils.showShort(com.netease.nim.uikit.R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().updatePreventRedpacketMemberList(uid, mytoken, qunID, IsAdd, DoAccID, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                pageNumber = 0;
                fetchPreventRedpacketMemberList(uid, myToken, teamId, pageNumber);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    public static void start(Context context, String teamId) {
        Intent intent = new Intent(context, PreventRedpacketActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    private String createMembersString(ArrayList<String> accounts) {
        if (accounts == null || accounts.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int selectedNumber = accounts.size();
        for (int i = 0; i < selectedNumber; i++) {
            builder.append(accounts.get(i));
            if (i != selectedNumber - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

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
