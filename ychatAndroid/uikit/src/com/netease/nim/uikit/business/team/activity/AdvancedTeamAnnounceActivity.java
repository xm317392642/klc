package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.team.helper.AnnouncementHelper;
import com.netease.nim.uikit.business.team.model.Announcement;
import com.netease.nim.uikit.business.team.viewholder.TeamAnnounceHolder;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * 群公告列表
 * Created by hzxuwen on 2015/3/18.
 */
public class AdvancedTeamAnnounceActivity extends SwipeBackUI implements TAdapterDelegate {
    // constant
    private final static String EXTRA_TID = "EXTRA_TID";
    private final static String EXTRA_AID = "EXTRA_AID";
    private final static String EXTRA_EDITOR = "EXTRA_EDITOR";
    private final static int RES_ANNOUNCE_CREATE_CODE = 0x10;
    public final static String RESULT_ANNOUNCE_DATA = "RESULT_ANNOUNCE_DATA";

    // context
    private Handler uiHandler;

    // data
    private String teamId;
    private String announceId;
    private String announce;
    private boolean canEditor;

    // view
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    //private TextView announceTips;
    private TextView announceContent;
    //private ListView announceListView;
    //private TAdapter mAdapter;
    // List<Announcement> items;

    private boolean isMember = false;

    public static void start(Activity activity, String teamId, boolean canEditor) {
        start(activity, teamId, null, canEditor);
    }

    public static void start(Activity activity, String teamId, String announceId, boolean canEditor) {
        Intent intent = new Intent();
        intent.setClass(activity, AdvancedTeamAnnounceActivity.class);
        intent.putExtra(EXTRA_TID, teamId);
        intent.putExtra(EXTRA_EDITOR, canEditor);
        if (announceId != null) {
            intent.putExtra(EXTRA_AID, announceId);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_announce);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText(R.string.team_annourcement);
        mToolbar.setNavigationOnClickListener(v -> finish());

        uiHandler = new Handler(getMainLooper());

        parseIntentData();
        findViews();
        initActionbar();
        //initAdapter();
        requestTeamData();
        requestMemberData();
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
        return TeamAnnounceHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    /**
     * ******************************初始化*******************************
     */

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_TID);
        announceId = getIntent().getStringExtra(EXTRA_AID);
        canEditor = getIntent().getBooleanExtra(EXTRA_EDITOR, false);
    }

    private void findViews() {
        //announceListView = (ListView) findViewById(R.id.team_announce_listview);
        //announceTips = (TextView) findViewById(R.id.team_announce_tips);

        announceContent = (TextView) findViewById(R.id.team_announce_content);
    }

    private void initActionbar() {
        TextView toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setVisibility(canEditor ? View.VISIBLE : View.GONE);
        toolbarView.setText("新建");
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvancedTeamCreateAnnounceActivity.startActivityForResult(AdvancedTeamAnnounceActivity.this, teamId, RES_ANNOUNCE_CREATE_CODE);
            }
        });
    }

//    private void initAdapter() {
//        items = new ArrayList<>();
//        mAdapter = new TAdapter(this, items, this);
//        announceListView.setAdapter(mAdapter);
//        announceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
//        announceListView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//        });
//    }

    private void requestTeamData() {
        // 请求群信息
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateAnnounceInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateAnnounceInfo(result);
                    }
                }
            });
        }
    }

    private void requestMemberData() {
        TeamMember teamMember = NimUIKit.getTeamProvider().getTeamMember(teamId, NimUIKit.getAccount());
        if (teamMember != null) {
            updateTeamMember(teamMember);
        } else {
            // 请求群成员
            NimUIKit.getTeamProvider().fetchTeamMember(teamId, NimUIKit.getAccount(), new SimpleCallback<TeamMember>() {
                @Override
                public void onResult(boolean success, TeamMember member, int code) {
                    if (success && member != null) {
                        updateTeamMember(member);
                    }
                }
            });
        }
    }

    /**
     * 更新公告信息
     *
     * @param team 群
     */
    private void updateAnnounceInfo(Team team) {
        if (team == null) {
            YchatToastUtils.showShort( getString(R.string.team_not_exist));
            finish();
        } else {
            announce = team.getAnnouncement();
            setAnnounceItem();
        }
    }

    /**
     * 判断是否是普通成员
     *
     * @param teamMember 群成员
     */
    private void updateTeamMember(TeamMember teamMember) {
        if (teamMember.getType() == TeamMemberType.Normal) {
            isMember = true;
        }
    }

    /**
     * 设置公告
     */
    private void setAnnounceItem() {
//        if (TextUtils.isEmpty(announce)) {
//            announceTips.setText(R.string.without_content);
//            announceTips.setVisibility(View.VISIBLE);
//            return;
//        } else {
//            announceTips.setVisibility(View.GONE);
//        }
//
        List<Announcement> list = AnnouncementHelper.getAnnouncements(teamId, announce, isMember ? 5 : Integer.MAX_VALUE);
        if (list == null || list.isEmpty()) {
            return;
        }
//
//        items.clear();
//        items.addAll(list);  这个是全部公告列表，现在要修改为 只留一条最新公告即可。
//        mAdapter.notifyDataSetChanged();
//
//        jumpToIndex(list);


        announceContent.setText(list.get(0).getContent());
    }

    /**
     * 跳转到选中的公告
     *
     * @param list 群公告列表
     */
//    private void jumpToIndex(List<Announcement> list) {
//        if (TextUtils.isEmpty(announceId)) {
//            return;
//        }
//
//        int jumpIndex = -1;
//        for (int i = 0; i < list.size(); i++) {
//            if (list.get(i).getId().equals(announceId)) {
//                jumpIndex = i;
//                break;
//            }
//        }
//
//        if (jumpIndex >= 0) {
//            final int position = jumpIndex;
//            uiHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ListViewUtil.scrollToPosition(announceListView, position, 0);
//                }
//            }, 200);
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RES_ANNOUNCE_CREATE_CODE:
                    announceId = null;
                    //items.clear();
                    requestTeamData();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_ANNOUNCE_DATA, announce);
        setResult(Activity.RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }
}
