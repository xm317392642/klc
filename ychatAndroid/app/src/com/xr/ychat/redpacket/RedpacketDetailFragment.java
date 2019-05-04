package com.xr.ychat.redpacket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.business.ait.selector.AitContactDecoration;
import com.netease.nim.uikit.business.ait.selector.model.ItemType;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RedpacketInfo;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.xr.ychat.R;

import java.util.ArrayList;
import java.util.List;

public class RedpacketDetailFragment extends Fragment {
    private static final String RECORD_TYPE = "record_type";
    private static final int REFRESH_REDPACKET = 12;
    public static final String TIME = "time";
    private String date;
    private int type;
    private int pageNumber;
    private String uid;
    private String mytoken;
    private RecyclerView recyclerView;
    private RedpacketDetailAdapter adapter;
    private List<RedpacketInfo> items;
    private LocalBroadcastManager localBroadcastManager;
    private DateChangeReceiver receiver;
    private LayoutInflater layoutInflater;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_REDPACKET:
                    queryRedpacketRecord(uid, mytoken, type, date, pageNumber);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static RedpacketDetailFragment newInstance(int type) {
        RedpacketDetailFragment fragment = new RedpacketDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(RECORD_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.layoutInflater = inflater;
        return inflater.inflate(R.layout.fragment_redpacket_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initAdapter(recyclerView);
    }

    private void initAdapter(RecyclerView recyclerView) {
        items = new ArrayList<>();
        adapter = new RedpacketDetailAdapter(recyclerView, items, type);
        recyclerView.setAdapter(adapter);
        List<Integer> noDividerViewTypes = new ArrayList<>(1);
        noDividerViewTypes.add(ItemType.SIMPLE_LABEL);
        recyclerView.addItemDecoration(new AitContactDecoration(getContext(), LinearLayoutManager.VERTICAL, noDividerViewTypes));
        adapter.setOnLoadMoreListener(() -> {
            queryRedpacketRecord(uid, mytoken, type, date, pageNumber++);
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt(RECORD_TYPE);
        uid = Preferences.getWeiranUid(getActivity());
        mytoken = Preferences.getWeiranToken(getActivity());
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        receiver = new DateChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xr.ychat.DateChangeBroadcastReceiver");
        localBroadcastManager.registerReceiver(receiver, intentFilter);
    }

    private class DateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String changeDate = intent.getStringExtra(TIME);
            if (!TextUtils.equals(date, changeDate)) {
                date = changeDate;
                pageNumber = 0;
                handler.sendEmptyMessage(REFRESH_REDPACKET);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeMessages(REFRESH_REDPACKET);
        localBroadcastManager.unregisterReceiver(receiver);
    }

    /**
     * 红包记录查询
     */
    private void queryRedpacketRecord(String uid, String mytoken, int hisType, String date, int page) {
        if (!NetworkUtil.isNetAvailable(getContext())) {
            handlerError(page);
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.getInstance().queryRedpacketRecord(uid, mytoken, hisType, date, page, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
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
                        View emptyView = layoutInflater.inflate(R.layout.empty_redpacket_record, null);
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
            View emptyView = layoutInflater.inflate(R.layout.empty_redpacket_record, null);
            adapter.setEmptyView(emptyView);
        } else {
            adapter.loadMoreFail();
        }
    }
}
