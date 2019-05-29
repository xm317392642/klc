package com.netease.nim.uikit.common.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CacheMemoryUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.SPUtils;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.UpdateInfo;
import com.netease.nim.uikit.common.fragment.TFragment;
import com.netease.nim.uikit.common.swipebacklayout.lib.app.SwipeBackActivity;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.util.DownloadUtils;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.ReflectionUtil;
import com.tendcloud.tenddata.TCAgent;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import qiu.niorgai.StatusBarCompat;

public abstract class SwipeBackUI extends SwipeBackActivity {

    private boolean destroyed = false;

    private static Handler handler;

    @Override
    protected void onStart() {
        super.onStart();
    }

    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }


    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            boolean result = fixOrientation();
        }
        super.onCreate(savedInstanceState);
    }

    public void setActivityView(@LayoutRes int layoutResID) {
        setContentView(layoutResID);
        String activityName = getClass().getSimpleName();
        if ("RedpactRecordActivity".equals(activityName) || "RedpactDetailActivity".equals(activityName)) {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.redpacket));//红包记录
        } else if ("WelcomeActivity".equals(activityName) || "LoginAuthorizeActivity".equals(activityName)) {
            StatusBarCompat.translucentStatusBar(this);
        } else if ("WatchMessagePictureActivity".equals(activityName) || "WatchVideoActivity".equals(activityName) || "CaptureVideoActivity".equals(activityName)) {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.black));
        } else if ("LoginActivity".equals(activityName)) {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.white));
        } else {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.color_be6913));
        }
    }

    public void setActivityView(View view) {
        setContentView(view);
        String activityName = getClass().getSimpleName();
        if ("RedpactRecordActivity".equals(activityName) || "RedpactDetailActivity".equals(activityName)) {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.redpacket));//红包记录
        } else if ("WelcomeActivity".equals(activityName) || "LoginAuthorizeActivity".equals(activityName)) {
            StatusBarCompat.translucentStatusBar(this);
        } else if ("WatchMessagePictureActivity".equals(activityName) || "WatchVideoActivity".equals(activityName) || "CaptureVideoActivity".equals(activityName)) {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.black));
        } else if ("LoginActivity".equals(activityName)) {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.white));
        } else {
            StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.color_be6913));
        }
    }

    @Override
    public void onBackPressed() {
        invokeFragmentManagerNoteStateNotSaved();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyboardUtils.hideSoftInput(this);
        destroyed = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        TCAgent.onPageStart(this, getClass().getName());
        String activityName = getClass().getSimpleName();
        if ("UserProtocolActivity".equals(activityName) || "ResetPasswordActivity".equals(activityName)) {
            return;
        }
        if (CommonUtil.isDownloading) {
            return;
        }
        DownloadUtils.queryAppVersion((boolean success, Object result, int code) -> {
            if (success) {
                update((UpdateInfo) result);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        TCAgent.onPageEnd(this, getClass().getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onNavigateUpClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onNavigateUpClicked() {
        onBackPressed();
    }

    protected final Handler getHandler() {
        if (handler == null) {
            handler = new Handler(getMainLooper());
        }
        return handler;
    }

    protected void showKeyboard(boolean isShow) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isShow) {
            if (getCurrentFocus() == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                imm.showSoftInput(getCurrentFocus(), 0);
            }
        } else {
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 延时弹出键盘
     *
     * @param focus 键盘的焦点项
     */
    protected void showKeyboardDelayed(View focus) {
        final View viewToFocus = focus;
        if (focus != null) {
            focus.requestFocus();
        }

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (viewToFocus == null || viewToFocus.isFocused()) {
                    showKeyboard(true);
                }
            }
        }, 200);
    }


    public boolean isDestroyedCompatible() {
        if (Build.VERSION.SDK_INT >= 17) {
            return isDestroyedCompatible17();
        } else {
            return destroyed || super.isFinishing();
        }
    }

    @TargetApi(17)
    private boolean isDestroyedCompatible17() {
        return super.isDestroyed();
    }

    /**
     * fragment management
     */
    public TFragment addFragment(TFragment fragment) {
        List<TFragment> fragments = new ArrayList<>(1);
        fragments.add(fragment);

        List<TFragment> fragments2 = addFragments(fragments);
        return fragments2.get(0);
    }

    public List<TFragment> addFragments(List<TFragment> fragments) {
        List<TFragment> fragments2 = new ArrayList<>(fragments.size());

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        boolean commit = false;
        for (int i = 0; i < fragments.size(); i++) {
            // install
            TFragment fragment = fragments.get(i);
            int id = fragment.getContainerId();

            // exists
            TFragment fragment2 = (TFragment) fm.findFragmentById(id);

            if (fragment2 == null) {
                fragment2 = fragment;
                transaction.add(id, fragment);
                commit = true;
            }

            fragments2.add(i, fragment2);
        }

        if (commit) {
            try {
                transaction.commitAllowingStateLoss();
            } catch (Exception e) {

            }
        }

        return fragments2;
    }

    public TFragment switchContent(TFragment fragment) {
        return switchContent(fragment, false);
    }

    protected TFragment switchContent(TFragment fragment, boolean needAddToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(fragment.getContainerId(), fragment);
        if (needAddToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        try {
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {

        }

        return fragment;
    }

    protected boolean displayHomeAsUpEnabled() {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                return onMenuKeyDown();

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    protected boolean onMenuKeyDown() {
        return false;
    }

    private void invokeFragmentManagerNoteStateNotSaved() {
        FragmentManager fm = getSupportFragmentManager();
        ReflectionUtil.invokeMethod(fm, "noteStateNotSaved", null);
    }

    protected void switchFragmentContent(TFragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(fragment.getContainerId(), fragment);
        try {
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean isCompatible(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }


    /**
     * 检查本地的apk是不是已经下载好的最新的apk
     *
     * @param isForce
     */
    public void checkLocalApk(String isForce) {
        if (CommonUtil.localDialogIsShow) {
            return;
        }
        if ("1".equals(CacheMemoryUtils.getInstance().get("cancel")) && "0".equals(isForce)) {
            return;//更新接口返回非强制更新，并且本地的标记位为-1（说明用户点过一次取消，后面重新打开应用，才会再弹一次）
        }
        CommonUtil.localDialogIsShow = true;
        EasyAlertDialog localDialog = new EasyAlertDialog(this);
        localDialog.setMessage("检测到手机已有下载好的最新安装包，请前去安装!");
        localDialog.setCancelable(false);
        localDialog.addPositiveButton("确定", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                v -> {
                    CommonUtil.localDialogIsShow = false;
                    localDialog.dismiss();
                    DownloadUtils.installApk(this, (boolean success2, Object result2, int code2) -> {
                        if (success2 == false) {
                            DownloadUtils.queryAppVersion((boolean success, Object result, int code) -> {
                                if (success) {
                                    update((UpdateInfo) result);
                                }
                            });//安装失败后，再调用更新接口
                        }
                    });
                });
        if ("0".equals(isForce)) {
            localDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        CommonUtil.localDialogIsShow = false;
                        CommonUtil.setCancelValue(true);
                        localDialog.dismiss();
                    });
        }
        localDialog.show();
    }


    public void update(UpdateInfo updateInfo) {
        if (CommonUtil.isDownloading) {
            return;//当前正在下载的话，不要再弹出更新对话框
        }
        if ("1".equals(CacheMemoryUtils.getInstance().get("cancel")) && "0".equals(updateInfo.getIsForce())) {
            return;//更新接口返回非强制更新，并且本地的标记位为-1（说明用户点过一次取消，后面重新打开应用，才会再弹一次）
        }
        String localApkPath = SPUtils.getInstance().getString("localApkPath");
        if (!TextUtils.isEmpty(localApkPath) && localApkPath.contains(updateInfo.getVersion())) {//这说明本地的安装包是最新的。
            checkLocalApk(updateInfo.getIsForce());
        } else {
            if (CommonUtil.updateDialogIsShow) {
                return;
            }
            CommonUtil.updateDialogIsShow = true;
            //强制更新只有一个确定按钮（非强制更新则有确定和取消）
            EasyAlertDialog updateDialog = new EasyAlertDialog(this);
            updateDialog.setTitle("当前最新版本为v" + updateInfo.getVersion());
            updateDialog.setMessage("更新内容为：" + System.getProperty("line.separator") + updateInfo.getRelease_log());
            updateDialog.setCancelable(false);
            updateDialog.addPositiveButton("确定", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    v -> {
                        CommonUtil.setCancelValue(false);
                        CommonUtil.updateDialogIsShow = false;
                        updateDialog.dismiss();
                        DownloadUtils.breakPointDownload(updateInfo, (boolean success1, Object result1, int progressCode) -> {
                            //下载成功
                            if (success1) {
                                DownloadUtils.installApk(this, (boolean success2, Object result2, int code2) -> {
                                    if (success2 == false) {//安装失败后，再调用更新接口
                                        DownloadUtils.queryAppVersion((boolean success3, Object result3, int code3) -> {
                                            if (success3) {
                                                update((UpdateInfo) result3);
                                            }
                                        });
                                    }
                                });
                            } else {
                                //下载失败后，再调用更新接口
//                                DownloadUtils.queryAppVersion((boolean success4, Object result4, int code4)-> {
//                                    if (success4){
//                                        update((UpdateInfo)result4);
//                                    }
//                                });
                            }
                        });
                    });
            if ("0".equals(updateInfo.getIsForce())) {
                updateDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                        v -> {
                            CommonUtil.updateDialogIsShow = false;
                            CommonUtil.setCancelValue(true);
                            updateDialog.dismiss();
                        });
            }
            updateDialog.show();
        }
    }


}
