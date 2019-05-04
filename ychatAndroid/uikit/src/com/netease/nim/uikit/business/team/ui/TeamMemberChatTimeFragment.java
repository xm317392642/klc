package com.netease.nim.uikit.business.team.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.ait.selector.AitContactDecoration;
import com.netease.nim.uikit.business.ait.selector.model.ItemType;
import com.netease.nim.uikit.business.team.adapter.TeamMemberChatTimeAdapter;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class TeamMemberChatTimeFragment extends Fragment implements TeamMemberChatTimeAdapter.TeamMemberChatTimeMultiple {
    private static final String RECORD_TYPE = "record_type";
    private static final String EXTRA_TEAM = "extra_team";
    private int type;
    private long time1;
    private long time2;
    private int pageNumber;
    private String uid;
    private String mytoken;
    private String teamId;
    private RecyclerView recyclerView;
    private TeamMemberChatTimeAdapter adapter;
    private List<RedpacketInfo> items;
    private LocalBroadcastManager localBroadcastManager;
    private DateChangeReceiver receiver;
    private LayoutInflater layoutInflater;
    private TeamMemberChatTimeAdapter.TeamMemberChatTimeMultiple chatChange;

    public static TeamMemberChatTimeFragment newInstance(int type, String teamId) {
        TeamMemberChatTimeFragment fragment = new TeamMemberChatTimeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(RECORD_TYPE, type);
        bundle.putString(EXTRA_TEAM, teamId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.layoutInflater = inflater;
        return inflater.inflate(R.layout.nim_team_member_chat_time_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatChange = (TeamMemberChatTimeAdapter.TeamMemberChatTimeMultiple) getActivity();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initAdapter(recyclerView);
    }

    private void initAdapter(RecyclerView recyclerView) {
        items = new ArrayList<>();
        adapter = new TeamMemberChatTimeAdapter(this, recyclerView, items);
        recyclerView.setAdapter(adapter);
        List<Integer> noDividerViewTypes = new ArrayList<>(1);
        noDividerViewTypes.add(ItemType.SIMPLE_LABEL);
        recyclerView.addItemDecoration(new AitContactDecoration(getContext(), LinearLayoutManager.VERTICAL, noDividerViewTypes));
        adapter.setOnLoadMoreListener(() -> {
            fetchChatTimeMemberList(uid, mytoken, teamId, pageNumber++);
        });
        pageNumber = 0;
        fetchChatTimeMemberList(uid, mytoken, teamId, pageNumber);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(RECORD_TYPE);
        teamId = getArguments().getString(EXTRA_TEAM);
        //TODO
        if (type == 1) {
            time1 = 604800L;
            time2 = 1209600L;
//            time1 = 10L;
//            time2 = 19200L;
        } else if (type == 2) {
            time1 = 1209600L;
            time2 = 2592000L;
//            time1 = 19200L;
//            time2 = 2592000L;
        } else {
            time1 = 2592000L;
            time2 = -1L;
        }
        uid = Preferences.getWeiranUid(getActivity());
        mytoken = Preferences.getWeiranToken(getActivity());
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        receiver = new DateChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xr.ychat.TeamMemberChatTimeBroadcastReceiver");
        localBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    private class DateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra("Action", 0);
            int multipleChoiceMode = intent.getIntExtra("MultipleChoice", 0);
            if (multipleChoiceMode == 0) {
                adapter.setType(1);
                for (RedpacketInfo info : items) {
                    info.setCheck(false);
                    chatChange.onCheckedChanged(info.getAccid(), false);
                }
                adapter.notifyDataSetChanged();
            } else {
                if (action == type) {
                    adapter.setType(2);
                    boolean isChecked = (multipleChoiceMode == 2);
                    for (RedpacketInfo info : items) {
                        info.setCheck(isChecked);
                        chatChange.onCheckedChanged(info.getAccid(), isChecked);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.setType(1);
                    for (RedpacketInfo info : items) {
                        info.setCheck(false);
                        chatChange.onCheckedChanged(info.getAccid(), false);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(receiver);
    }

    /**
     * 获取一段时间不发言的玩家列表
     */
    private void fetchChatTimeMemberList(String uid, String mytoken, String qunID, int page) {
        if (!NetworkUtil.isNetAvailable(getContext())) {
            handlerError(page);
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.getInstance().fetchChatTimeMemberList(uid, mytoken, time1, time2, qunID, page, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                if (aVoid.getData() != null && aVoid.getData().size() > 0) {
                    if (page == 0) {
                        items.clear();
                        items.addAll(aVoid.getData());
                        adapter.notifyDataSetChanged();
                    } else {
                        items.addAll(aVoid.getData());
                        adapter.notifyDataSetChanged();
                    }
                    chatChange.updateMemberNumber(type, items.size());
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
                        View emptyView = layoutInflater.inflate(R.layout.empty_team_member_change, null);
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
            View emptyView = layoutInflater.inflate(R.layout.empty_team_member_change, null);
            TextView textView = emptyView.findViewById(R.id.content);
            textView.setText("没有数据");
            adapter.setEmptyView(emptyView);
        } else {
            adapter.loadMoreFail();
        }
    }

    @Override
    public void onCheckedChanged(String accid, boolean isChecked) {
        chatChange.onCheckedChanged(accid, isChecked);
    }

    @Override
    public void updateMemberNumber(int type, int number) {

    }
}
