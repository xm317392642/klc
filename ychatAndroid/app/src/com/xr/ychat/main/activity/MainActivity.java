package com.xr.ychat.main.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.microquation.linkedme.android.LinkedME;
import com.netease.nim.avchatkit.AVChatProfile;
import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.avchatkit.constant.AVChatExtras;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.drop.DropCover;
import com.netease.nim.uikit.common.ui.drop.DropFake;
import com.netease.nim.uikit.common.ui.drop.DropManager;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.impl.preference.UserPreferences;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.xr.ychat.R;
import com.xr.ychat.login.LoginAuthorizeActivity;
import com.xr.ychat.login.LogoutHelper;
import com.xr.ychat.login.MainMenuFragment;
import com.xr.ychat.login.SwitchAccountActivity;
import com.xr.ychat.main.fragment.MainTabFragment;
import com.xr.ychat.main.fragment.MineFragment;
import com.xr.ychat.main.helper.CustomNotificationCache;
import com.xr.ychat.main.helper.SystemMessageUnreadManager;
import com.xr.ychat.main.model.MainTab;
import com.xr.ychat.main.reminder.ReminderItem;
import com.xr.ychat.main.reminder.ReminderManager;
import com.xr.ychat.session.SessionHelper;
import com.xr.ychat.team.TeamCreateHelper;

import java.util.ArrayList;

/**
 * 主界面 测试冲突
 * Created by huangjun on 2015/3/25.
      新增了一个断点续传的方法
 *    private void breakPointDownload(String url) {
 *         //https://github.com/lingochamp/FileDownloader/blob/master/README-zh.md compile 'com.liulishuo.filedownloader:library:1.7.6'
 *         FileDownloader.setup(getContext());
 *         AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
 *         builder.setTitle("正在更新");
 *         // 给下载对话框增加进度条
 *         final LayoutInflater inflater = LayoutInflater.from(getContext());
 *         View v = inflater.inflate(R.layout.softupdate_progress, null);
 *         final ProgressBar mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
 *         final TextView txProgress = (TextView) v.findViewById(R.id.txProgress);
 *         builder.setView(v);
 *         final Dialog mDownloadDialog = builder.create();
 *         mDownloadDialog.setCancelable(false );
 *         mDownloadDialog.show();
 *         String localPath = Tool.getCacheDirPath(context) + File.separator + "tutengdai.apk";
 *         //FileDownloader.isReusedOldFile
 *         Tool.printLog("localPath=" + localPath);
 *         FileDownloader.getImpl().create(url)
 *                 .setPath(localPath)//下载文件的存储绝对路径
 *                 .setForceReDownload(true)//强制重新下载，将会忽略检测文件是否健在
 *                 .setListener(new FileDownloadListener() {
 *                     @Override
 *                     protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
 *                         Tool.printLog("pending");
 *                     }
 *                     @Override
 *                     protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
 *                         Tool.printLog("connected");
 *                     }
 *                     @Override
 *                     protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
 *                         Tool.printLog("soFarBytes=" + soFarBytes + "  totalBytes=" + totalBytes);
 *                         int progress = (int) (((float) soFarBytes / totalBytes) * 100);
 *                         // 设置进度条位置
 *                         mProgress.setProgress(progress);
 *                         txProgress.setText(progress + "%-------");
 *                     }
 *
 *                     @Override
 *                     protected void blockComplete(BaseDownloadTask task) {
 *                         Tool.printLog("blockComplete");
 *                     }
 *
 *                     @Override
 *                     protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
 *                         Tool.printLog("retry");
 *                     }
 *
 *                     @Override
 *                     protected void completed(BaseDownloadTask task) {
 *                         // 安装文件
 *                         new UpdateManager(getContext()).installApk(Tool.getCacheDirPath(context));
 *                         mDownloadDialog.dismiss();
 *                         Toast.makeText(getContext(), "下载完成", 1).show();
 *                     }
 *
 *                     @Override
 *                     protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
 *                         Tool.printLog("paused");
                           Toast.makeText(getContext(), "下载暂停", 1).show();
 *                     }
 *
 *                     @Override
 *                     protected void error(BaseDownloadTask task, Throwable e) {
 *                         Tool.printLog("error=" + e.getMessage());
 *                         mDownloadDialog.dismiss();//下载失败，下回会重新断点续传
 *                          Toast.makeText(getContext(), "下载失败", 1000).show();
 *                     }
 *
 *                     @Override
 *                     protected void warn(BaseDownloadTask task) {
 *                         Tool.printLog("warn");
 *                     }
 *                 }).start();
 *     }
 */
public class MainActivity extends UI implements ReminderManager.UnreadNumChangedCallback, MainMenuFragment.ClickableChildView {
    private static final String TAG_FRAGMENT_SESSION = "tag_fragment_session";
    private static final String TAG_FRAGMENT_CONTACT = "tag_fragment_contact";
    private static final String TAG_FRAGMENT_MINE = "tag_fragment_mine";
    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private static final String EXTRA_APP_AVATAR = "APP_AVATAR";
    private static final int REQUEST_CODE_NORMAL = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;
    private static final int BASIC_PERMISSION_REQUEST_CODE = 100;
    private static final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private DropFake sessionDropFake;
    private DropFake contactDropFake;
    private RadioButton sessionRadioButton;
    private RadioButton contactRadioButton;
    private RadioButton mineRadioButton;
    private TextView title;
    private ImageView add;
    private RadioGroup radioGroup;
    private MainTabFragment sessionFragment;
    private MainTabFragment contactFragment;
    private Fragment mineFragment;
    private MainMenuFragment mainMenuFragment;
    private int currentItem;
    private LocalBroadcastManager localBroadcastManager;
    private Observer<Integer> sysMsgUnreadCountChangedObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer unreadCount) {
            if (unreadCount != 0) {
                int unread = unreadCount + CustomNotificationCache.getUnreadCount();
                SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
                ReminderManager.getInstance().updateContactUnreadNum(unread);
            }
        }
    };
    private ClearSystemCountBroadcastReceiver receiver;

    private class ClearSystemCountBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
            SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(0);
            ReminderManager.getInstance().updateContactUnreadNum(0);
        }
    }

    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 500;  // 快速点击间隔

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit, String avatar) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        extra.putExtra(EXTRA_APP_AVATAR, avatar);
        start(context, extra);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserPreferences.setShare(false);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.empty;
        setToolBar(R.id.toolbar, options);
        if (parseIntent()) {
            return;
        }
        init();
    }

    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            intent.removeExtra(EXTRA_APP_QUIT);
            String avatar = intent.getStringExtra(EXTRA_APP_AVATAR);
            onLogout(avatar);
            return true;
        }
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    NimUIKit.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
            }
            return false;
        }
        if (intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT) && AVChatProfile.getInstance().isAVChatting()) {
            intent.removeExtra(AVChatActivity.INTENT_ACTION_AVCHAT);
            Intent localIntent = new Intent();
            localIntent.setClass(this, AVChatActivity.class);
            startActivity(localIntent);
            return true;
        }
        String account = intent.getStringExtra(AVChatExtras.EXTRA_ACCOUNT);
        if (intent.hasExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION) && !TextUtils.isEmpty(account)) {
            intent.removeExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION);
            NimUIKit.startP2PSession(this, account);
            return true;
        }
        return false;
    }

    private void init() {
        observerSyncDataComplete();
        findViews();
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        requestSystemMessageUnreadCount();
        initUnreadCover();
        requestBasicPermission();
    }

    private void observerSyncDataComplete() {
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(new Observer<Void>() {
            @Override
            public void onEvent(Void v) {
                DialogMaker.dismissProgressDialog();
            }
        });
        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false);
        }
    }

    private void findViews() {
        View.OnClickListener listener = v -> {
            switch (v.getId()) {
                case R.id.main_add: {
                    if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
                        return;
                    }
                    lastClickTime = System.currentTimeMillis();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    MainMenuFragment mainMenuFragment = (MainMenuFragment) fragmentManager.findFragmentByTag("MainMenuFragment");
                    if (mainMenuFragment != null) {
                        fragmentManager.beginTransaction().remove(mainMenuFragment).commit();
                    }
                    mainMenuFragment = new MainMenuFragment();
                    if (!isFinishing()) {
                        mainMenuFragment.show(getSupportFragmentManager());
                    }
                }
                break;
                case R.id.main_tab_chat: {
                    if (currentItem != R.id.main_tab_chat) {
                        add.setVisibility(View.VISIBLE);
                        currentItem = R.id.main_tab_chat;
                        radioGroup.check(R.id.main_tab_chat);
                        title.setText(R.string.home_tab_chat);
                        MainTabFragment sessionFragment = (MainTabFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_SESSION);
                        MainTabFragment contactFragment = (MainTabFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_CONTACT);
                        Fragment mineFragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MINE);
                        changeFragment(sessionFragment, contactFragment, mineFragment);
                    }
                }
                break;
                case R.id.main_tab_contact: {
                    if (currentItem != R.id.main_tab_contact) {
                        add.setVisibility(View.VISIBLE);
                        currentItem = R.id.main_tab_contact;
                        radioGroup.check(R.id.main_tab_contact);
                        title.setText(R.string.home_tab_contact);
                        MainTabFragment sessionFragment = (MainTabFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_SESSION);
                        MainTabFragment contactFragment = (MainTabFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_CONTACT);
                        Fragment mineFragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MINE);
                        changeFragment(contactFragment, mineFragment, sessionFragment);
                    }
                }
                break;
                case R.id.main_tab_mine: {
                    if (currentItem != R.id.main_tab_mine) {
                        add.setVisibility(View.GONE);
                        currentItem = R.id.main_tab_mine;
                        radioGroup.check(R.id.main_tab_mine);
                        title.setText(R.string.home_tab_mine);
                        MainTabFragment sessionFragment = (MainTabFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_SESSION);
                        MainTabFragment contactFragment = (MainTabFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_CONTACT);
                        Fragment mineFragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MINE);
                        changeFragment(mineFragment, sessionFragment, contactFragment);
                    }
                }
                break;
            }
        };
        radioGroup = findViewById(R.id.main_tab_group);
        sessionRadioButton = findViewById(R.id.main_tab_chat);
        contactRadioButton = findViewById(R.id.main_tab_contact);
        mineRadioButton = findViewById(R.id.main_tab_mine);
        add = findViewById(R.id.main_add);
        add.setOnClickListener(listener);
        sessionRadioButton.setOnClickListener(listener);
        contactRadioButton.setOnClickListener(listener);
        mineRadioButton.setOnClickListener(listener);
        title = findViewById(R.id.main_title);
        title.setText(R.string.home_tab_chat);
        sessionDropFake = findViewById(R.id.tab_new_chat_msg);
        sessionDropFake.setTouchListener(new DropFake.ITouchListener() {
            @Override
            public void onDown() {
                DropManager.getInstance().setCurrentId(String.valueOf(0));
                DropManager.getInstance().down(sessionDropFake, sessionDropFake.getText());
            }

            @Override
            public void onMove(float curX, float curY) {
                DropManager.getInstance().move(curX, curY);
            }

            @Override
            public void onUp() {
                DropManager.getInstance().up();
            }
        });
        contactDropFake = findViewById(R.id.tab_new_contact_msg);
        contactDropFake.setTouchListener(new DropFake.ITouchListener() {
            @Override
            public void onDown() {
                DropManager.getInstance().setCurrentId(String.valueOf(1));
                DropManager.getInstance().down(contactDropFake, contactDropFake.getText());
            }

            @Override
            public void onMove(float curX, float curY) {
                DropManager.getInstance().move(curX, curY);
            }

            @Override
            public void onUp() {
                DropManager.getInstance().up();
                NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
                SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(0);
                ReminderManager.getInstance().updateContactUnreadNum(0);
            }
        });
        sessionFragment = initMainTabFragment(MainTab.RECENT_CONTACTS);
        contactFragment = initMainTabFragment(MainTab.CONTACT);
        mineFragment = MineFragment.newInstance();
        currentItem = R.id.main_tab_chat;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_content, sessionFragment, TAG_FRAGMENT_SESSION)
                .add(R.id.main_content, contactFragment, TAG_FRAGMENT_CONTACT)
                .add(R.id.main_content, mineFragment, TAG_FRAGMENT_MINE)
                .show(sessionFragment)
                .hide(contactFragment)
                .hide(mineFragment)
                .commit();
    }

    @Override
    public void clickChatView() {
        //群人数上限
        ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 200);
        NimUIKit.startContactSelector(MainActivity.this, advancedOption, REQUEST_CODE_ADVANCED);
    }

    public MainTabFragment initMainTabFragment(MainTab mainTab) {
        try {
            MainTabFragment fragment = mainTab.clazz.newInstance();
            fragment.attachTabData(mainTab);
            return fragment;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private void changeFragment(Fragment showFragment, Fragment hideFragment1, Fragment hideFragment2) {
        getSupportFragmentManager().beginTransaction()
                .show(showFragment)
                .hide(hideFragment1)
                .hide(hideFragment2)
                .commit();
    }

    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
        if (register) {
            localBroadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);
            receiver = new ClearSystemCountBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.xr.ychat.ClearSystemCountBroadcastReceiver");
            localBroadcastManager.registerReceiver(receiver, intentFilter);
        } else {
            localBroadcastManager.unregisterReceiver(receiver);
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(sysMsgUnreadCountChangedObserver, register);
    }

    /**
     * 查询系统消息未读数
     */
    private void requestSystemMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock() + CustomNotificationCache.getUnreadCount();
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
        ReminderManager.getInstance().updateContactUnreadNum(unread);
    }

    //初始化未读红点动画
    private void initUnreadCover() {
        DropManager.getInstance().init(this, (DropCover) findView(R.id.unread_cover),
                new DropCover.IDropCompletedListener() {
                    @Override
                    public void onCompleted(Object id, boolean explosive) {
                        if (id == null || !explosive) {
                            return;
                        }

                        if (id instanceof RecentContact) {
                            RecentContact r = (RecentContact) id;
                            NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(), r.getSessionType());
                            return;
                        }

                        if (id instanceof String) {
                            if (((String) id).contentEquals("0")) {
                                NIMClient.getService(MsgService.class).clearAllUnreadCount();
                            } else if (((String) id).contentEquals("1")) {
                                NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
                            }
                        }
                    }
                });
    }

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(MainActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    private void onLogout(String avatar) {
        Preferences.saveUserToken(this, "");
        // 清理缓存&注销监听
        LogoutHelper.logout();
        // 如果该用户绑定了手机号，则跳转到切换账号页面；如果没有绑定手机号，则跳转到首页登录页面
        if (RegexUtils.isMobileExact(Preferences.getUserPhone(this))) {
            SwitchAccountActivity.start(this, avatar);
        } else {
            LoginAuthorizeActivity.start(this);
        }
        finish();
    }

    /**
     * 设置最近联系人的消息为已读
     * <p>
     * account, 聊天对象帐号，或者以下两个值：
     * {@link MsgService#MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     * {@link MsgService#MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
     */
    private void enableMsgNotification(boolean enable) {
        boolean msg = (currentItem == R.id.main_tab_chat);
        if (enable | msg) {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableMsgNotification(false);
        getResources();
        LinkedME.getInstance().setImmediate(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        enableMsgNotification(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
        DropManager.getInstance().destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_NORMAL) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                TeamCreateHelper.createNormalTeam(MainActivity.this, selected, false, null);
            } else {
                YchatToastUtils.showShort("请选择至少一个联系人！");
            }
        } else if (requestCode == REQUEST_CODE_ADVANCED) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            TeamCreateHelper.createAdvancedTeam(MainActivity.this, selected);
        }
    }

    //未读消息数量观察者实现
    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        DropFake cover = (item.getId() == 0) ? sessionDropFake : contactDropFake;
        int unread = item.unread();
        if (unread > 0) {
            cover.setVisibility(View.VISIBLE);
            cover.setText(unread > 99 ? "99+" : String.valueOf(unread));
        } else {
            cover.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            YchatToastUtils.showShort("未全部授权，部分功能可能无法正常运行！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public Resources getResources() {
        float fontSizeScale = SPUtils.getInstance().getFloat(Extras.EXTRA_TYPEFACE);
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        if (fontSizeScale > 0.5) {
            config.fontScale = fontSizeScale;//1 设置正常字体大小的倍数
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}
