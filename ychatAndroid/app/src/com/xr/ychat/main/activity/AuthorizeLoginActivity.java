package com.xr.ychat.main.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;

import org.json.JSONObject;

import java.util.List;

/**
 * 授权登录
 */
public class AuthorizeLoginActivity extends UI {

    private String scheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_auth_login_activity);
        scheme = getIntent().getStringExtra("scheme");
        findView(R.id.cancel).setOnClickListener(v-> authorizeResulst("",CommonUtil.SHARE_CANCEL));
        findView(R.id.login).setOnClickListener(v->verificationCodeRequest(Preferences.getWeiranUid(getApplicationContext()),Preferences.getWeiranToken(getApplicationContext()), DemoCache.getAccount()));

    }

    @Override
    public void onBackPressed() {
        authorizeResulst("",CommonUtil.SHARE_CANCEL);
    }

    /**
     * 客户端验证授权code
     * 使用scheme跳转到空了吹授权登录页面；
     * 空了吹app已经登录（玩家跳转到授权登录页面），未登录（登录成功后，再跳转到授权登录页面）
     * app调用接口返回access_token给游戏端
     */
    public void verificationCodeRequest(String uid,String mytoken,String accid){
        DialogMaker.showProgressDialog(this, "登录中...");
        ContactHttpClient.getInstance().verificationCodeRequest(uid,mytoken,accid,new ContactHttpClient.ContactHttpCallback<String>() {
            @Override
            public void onSuccess(String token) {
                YchatToastUtils.showShort("授权登录成功");
                DialogMaker.dismissProgressDialog();
                authorizeResulst(token,CommonUtil.SHARE_SUCCESS);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                YchatToastUtils.showShort("空了吹授权失败");
                DialogMaker.dismissProgressDialog();
                authorizeResulst("",CommonUtil.SHARE_FAIL);
            }
        });

    }

    /**
     *登录授权结果
     * @param token
     * @param authResult
     */
    private void authorizeResulst(String token,int authResult){
        Uri uri = Uri.parse(scheme + "://yaoliao/userinfo?access_token="+token+"&share_result="+ authResult);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);
        if (activities.size() > 0) {
            startActivity(intent);
            finish();
        } else {
            YchatToastUtils.showShort("uri格式有误，无法跳转：" + uri.toString());
        }
    }
}
