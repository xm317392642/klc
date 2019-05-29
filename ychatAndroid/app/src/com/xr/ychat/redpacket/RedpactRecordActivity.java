package com.xr.ychat.redpacket;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.RequestInfo;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.xr.ychat.DemoCache;
import com.xr.ychat.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RedpactRecordActivity extends SwipeBackUI {
    private TextView datePicker;
    private ImageView redpacketRecord;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private HeadImageView fromAvatar;
    private TextView fromName;
    private TextView statistics;
    private TextView alipayAccount;
    private RedpacketDetailFragment receiveFragment;
    private RedpacketDetailFragment sendFragment;
    private List<Fragment> mFragments;
    private int birthYear;
    private int birthMonth;
    private LocalBroadcastManager localBroadcastManager;
    private MenuDialog dialog;
    private String uid;
    private String myToken;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_redpacket_record);
        localBroadcastManager = LocalBroadcastManager.getInstance(RedpactRecordActivity.this);
        decimalFormat = new DecimalFormat("0.00");
        initToolbar();
        initViewPager();
        initView();
    }

    private void initView() {
        redpacketRecord = (ImageView) findViewById(R.id.redpacket_detail_record);
        redpacketRecord.setOnClickListener(v -> {
            showAlipayMenu();
        });
        datePicker = (TextView) findViewById(R.id.redpacket_record_date);
        datePicker.setOnClickListener(v -> {
                    Calendar selectedCalendar = null;
                    if (birthYear != 0 && birthMonth != 0) {
                        selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(Calendar.YEAR, birthYear);
                        selectedCalendar.set(Calendar.MONTH, birthMonth);
                    }
                    //时间选择器
                    TimePickerView pvTime = new TimePickerBuilder(RedpactRecordActivity.this, new OnTimeSelectListener() {
                        @Override
                        public void onTimeSelect(Date date, View v) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            birthYear = calendar.get(Calendar.YEAR);
                            birthMonth = calendar.get(Calendar.MONTH);
                            sendBroadcastReceiver();

                        }
                    }).isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                            .setType(new boolean[]{true, true, false, false, false, false})
//                            .setTitleBgColor(0xFFBE6913)
//                            .setSubmitColor(Color.WHITE)//确定按钮文字颜色
//                            .setCancelColor(Color.WHITE)//取消按钮文字颜色
                            .setDate(selectedCalendar == null ? Calendar.getInstance() : selectedCalendar)// 如果不设置的话，默认是系统时间
                            .isCyclic(false)//是否循环滚动
                            .build();

                    Dialog mDialog = pvTime.getDialog();
                    if (mDialog != null) {
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                Gravity.BOTTOM);

                        params.leftMargin = 0;
                        params.rightMargin = 0;
                        pvTime.getDialogContainerLayout().setLayoutParams(params);

                        Window dialogWindow = mDialog.getWindow();
                        if (dialogWindow != null) {
                            dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                            dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                            dialogWindow.setDimAmount(0.1f);
                        }
                    }

                    pvTime.show();
                }
        );
        String account = DemoCache.getAccount();
        fromAvatar = (HeadImageView) findViewById(R.id.redpacket_record_avatar);
        fromAvatar.loadBuddyAvatar(account);
        fromName = (TextView) findViewById(R.id.redpacket_detail_name);
        statistics = (TextView) findViewById(R.id.redpacket_record_statistics);
        NimUserInfo userInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {
                @Override
                public void onResult(boolean success, NimUserInfo result, int code) {
                    if (success) {
                        fromName.setText(result.getName());
                    }
                }
            });
        } else {
            fromName.setText(userInfo.getName());
        }
        alipayAccount = (TextView) findViewById(R.id.recipient_account);
        queryAlipayAccount();
        Calendar c = Calendar.getInstance();
        birthYear = c.get(Calendar.YEAR);
        birthMonth = c.get(Calendar.MONTH);
        uid = Preferences.getWeiranUid(this);
        myToken = Preferences.getWeiranToken(this);
        mViewPager.postDelayed(() -> {
            sendBroadcastReceiver();
        }, 100);
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mFragments = new ArrayList<>();
        receiveFragment = RedpacketDetailFragment.newInstance(2);
        mFragments.add(receiveFragment);
        sendFragment = RedpacketDetailFragment.newInstance(1);
        mFragments.add(sendFragment);
        mViewPager.setAdapter(new RedpacketRecordAdapter(this, getSupportFragmentManager(), mFragments));
        mViewPager.setCurrentItem(0);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    private void sendBroadcastReceiver() {
        datePicker.setText(String.format("%1$d年%2$d月", birthYear, birthMonth + 1));
        Intent intent = new Intent("com.xr.ychat.DateChangeBroadcastReceiver");
        StringBuilder builder = new StringBuilder();
        builder.append(birthYear);
        if (birthMonth < 9) {
            builder.append(0);
        }
        builder.append(birthMonth + 1);
        intent.putExtra(RedpacketDetailFragment.TIME, builder.toString());
        localBroadcastManager.sendBroadcast(intent);
        ContactHttpClient.getInstance().queryAlipayMonth(uid, myToken, builder.toString(), new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                statistics.setText(String.format("发出¥%1$s，收到¥%2$s", decimalFormat.format(aVoid.getSend() / 100f), decimalFormat.format(aVoid.getReceive() / 100f)));
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                statistics.setText("");
            }
        });
    }

    /**
     * 显示菜单
     */
    private void showAlipayMenu() {
        if (dialog == null) {
            List<String> btnNames = new ArrayList<>();
            btnNames.add("取消支付宝授权");
            btnNames.add("取消");
            dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    if (name.equals(btnNames.get(0))) {
                        MaterialDialog materialDialog = new MaterialDialog.Builder(RedpactRecordActivity.this)
                                .title("确定取消支付宝授权?")
                                .positiveText("确定")
                                .onPositive((dialog, which) -> {
                                    dialog.dismiss();
                                    cancelAlipayAuth(uid, myToken);
                                })
                                .negativeText("取消")
                                .onNegative((dialog, which) -> dialog.cancel())
                                .build();
                        materialDialog.setCanceledOnTouchOutside(false);
                        materialDialog.show();
                    }
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    /**
     * 支付宝解绑
     */
    private void cancelAlipayAuth(String uid, String mytoken) {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        ContactHttpClient.getInstance().cancelAlipayAuth(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                SPUtils.getInstance().put(CommonUtil.ALIPAYUID, "");
                YchatToastUtils.showShort("支付宝解绑成功");
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                YchatToastUtils.showShort("支付宝解绑失败");
            }
        });
    }

    private void queryAlipayAccount() {
        if (!NetworkUtil.isNetAvailable(this)) {
            YchatToastUtils.showShort(R.string.network_is_not_available);
            return;
        }
        String uid = Preferences.getWeiranUid(this);
        String mytoken = Preferences.getWeiranToken(this);
        ContactHttpClient.getInstance().queryAlipayAccount(uid, mytoken, new ContactHttpClient.ContactHttpCallback<RequestInfo>() {
            @Override
            public void onSuccess(RequestInfo aVoid) {
                alipayAccount.setText(aVoid.getAlilogonid());
                alipayAccount.setVisibility(TextUtils.isEmpty(aVoid.getAlilogonid()) ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailed(int code, String errorMsg) {

            }
        });
    }

    public static void start(Activity context, int requestCode) {
        Intent intent = new Intent(context, RedpactRecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivityForResult(intent, requestCode);
    }
}
