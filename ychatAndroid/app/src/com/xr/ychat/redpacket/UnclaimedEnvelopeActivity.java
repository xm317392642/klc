package com.xr.ychat.redpacket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.business.session.fragment.MessageFragment;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.TeamMethodInfo;
import com.netease.nim.uikit.common.UnclaimedEnvelope;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.xr.ychat.R;

import java.util.ArrayList;
import java.util.List;

public class UnclaimedEnvelopeActivity extends SwipeBackUI implements ModuleProxy {
    private static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";
    //TODO
    private static final int TIME_OUT = 10;
    private RecyclerView recyclerView;
    private UnclaimedEnvelopeAdapter adapter;
    private String sessionId;
    private LocalBroadcastManager localBroadcastManager;
    private String uid;
    private String myToken;
    private List<UnclaimedEnvelope> items;
    private int pageNumber;
    private View emptyView;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.nim_team_unclaimed_envelope_activity);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(UnclaimedEnvelopeActivity.this));
        initAdapter(recyclerView);
        localBroadcastManager = LocalBroadcastManager.getInstance(UnclaimedEnvelopeActivity.this);
        Uri uri = getIntent().getData();
        sessionId = uri.getQueryParameter(EXTRA_SESSION_ID);
        uid = Preferences.getWeiranUid(this);
        myToken = Preferences.getWeiranToken(this);
        pageNumber = 0;
        queryUnclaimedRedpacket(uid, myToken, sessionId, pageNumber);
    }

    private void initAdapter(RecyclerView recyclerView) {
        items = new ArrayList<>();
        Container container = new Container(UnclaimedEnvelopeActivity.this, sessionId, SessionTypeEnum.Team, this);
        adapter = new UnclaimedEnvelopeAdapter(recyclerView, items, container, sessionId);
        emptyView = LayoutInflater.from(UnclaimedEnvelopeActivity.this).inflate(R.layout.empty_redpacket_record, null);
        content = emptyView.findViewById(R.id.content);
        content.setText("没有长时间未领取的红包");
        adapter.setEmptyView(emptyView);
        adapter.setOnLoadMoreListener(() -> {
            queryUnclaimedRedpacket(uid, myToken, sessionId, pageNumber++);
        });
        recyclerView.addItemDecoration(new SimplePaddingDecoration(UnclaimedEnvelopeActivity.this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean sendMessage(IMMessage msg) {
        Intent intent = new Intent("com.xr.ychat.NewMessageBroadcastReceiver");
        intent.putExtra(MessageFragment.NEW_MESSAGE, msg);
        intent.putExtra(MessageFragment.NEW_MESSAGE_TYPE, 1);
        localBroadcastManager.sendBroadcast(intent);
        return true;
    }

    @Override
    public void saveMessageToLocal(IMMessage msg) {

    }

    @Override
    public void onInputPanelExpand() {

    }

    @Override
    public void shouldCollapseInputPanel() {

    }

    @Override
    public boolean isLongClickEnabled() {
        return false;
    }

    @Override
    public void onItemFooterClick(IMMessage message) {

    }

    @Override
    public void onUnclaimedEnvelopeClick(UnclaimedEnvelope message) {
        for (UnclaimedEnvelope envelope : items) {
            if (TextUtils.equals(envelope.getOrderno(), message.getOrderno())) {
                envelope.setType(message.getType());
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void queryUnclaimedRedpacket(String uid, String mytoken, String qunID, int page) {
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().queryUnclaimedRedpacket(uid, mytoken, qunID, TIME_OUT, page, new ContactHttpClient.ContactHttpCallback<TeamMethodInfo>() {
            @Override
            public void onSuccess(TeamMethodInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (aVoid.getList() != null && aVoid.getList().size() > 0) {
                    if (page == 0) {
                        items.clear();
                        items.addAll(aVoid.getList());
                    } else {
                        items.addAll(aVoid.getList());
                    }
                    adapter.notifyDataSetChanged();
                    boolean hasNextPage = aVoid.getList().size() >= 10;
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
                DialogMaker.dismissProgressDialog();
                handlerError();
            }
        });
    }

    private void handlerError() {
        adapter.setNewData(null);
        adapter.setEmptyView(emptyView);
    }

    public class SimplePaddingDecoration extends RecyclerView.ItemDecoration {

        private int dividerHeight;


        public SimplePaddingDecoration(Context context) {
            dividerHeight = context.getResources().getDimensionPixelSize(R.dimen.bubble_layout_margin_side);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int childIndex = parent.getChildAdapterPosition(view);
            int mItemCount = parent.getAdapter().getItemCount();
            if (childIndex == 0) {
                outRect.top = dividerHeight;
                outRect.bottom = dividerHeight;
            } else {
                outRect.top = 0;
                outRect.bottom = dividerHeight;
            }
        }
    }
}
