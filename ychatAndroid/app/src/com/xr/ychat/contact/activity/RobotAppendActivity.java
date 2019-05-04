package com.xr.ychat.contact.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.RobotInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.xr.ychat.R;

import java.util.ArrayList;
import java.util.List;

import static com.xr.ychat.contact.activity.RobotActivity.EXTRA_ACCID;

public class RobotAppendActivity extends SwipeBackUI implements RobotAppendAdapter.AppendRobotInteface {
    private String uid;
    private String mytoken;
    private RecyclerView recyclerView;
    private RobotAppendAdapter adapter;
    private List<RobotInfo> robotInfos;
    private LayoutInflater layoutInflater;

    public static void start(Activity activity, int reqcode) {
        Intent intent = new Intent(activity, RobotAppendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, reqcode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_append_robot);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("添加机器人");
        layoutInflater = LayoutInflater.from(RobotAppendActivity.this);
        uid = Preferences.getWeiranUid(this);
        mytoken = Preferences.getWeiranToken(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        initAdapter(recyclerView);
        queryAllRobot(uid, mytoken);
    }

    private void initAdapter(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(RobotAppendActivity.this));
        robotInfos = new ArrayList<>();
        adapter = new RobotAppendAdapter(recyclerView, robotInfos, this);
        recyclerView.setAdapter(adapter);
    }

    private void queryAllRobot(String uid, String mytoken) {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        DialogMaker.showProgressDialog(this, "", false);
        ContactHttpClient.getInstance().queryAllRobot(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                DialogMaker.dismissProgressDialog();
                if (aVoid.getList() != null && aVoid.getList().size() > 0) {
                    adapter.setNewData(aVoid.getList());
                    adapter.setEnableLoadMore(false);
                    adapter.loadMoreEnd();
                } else {
                    handlerError();
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
        View emptyView = layoutInflater.inflate(R.layout.empty_redpacket_record, null);
        TextView textView = emptyView.findViewById(R.id.content);
        textView.setText("没有第三方机器人");
        adapter.setEmptyView(emptyView);
    }

    @Override
    public void appendRobot(String accid) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ACCID, accid);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}
