package com.netease.nim.uikit.common.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.UpdateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtils {
    //下载器
    private DownloadManager downloadManager;
    private Context mContext;
    //下载的ID
    private long downloadId;
    private String apkName = "";
    private String pathstr;


    public void download(Context context, String versionName, String url, String name) {
        this.mContext = context;
        this.apkName = name;
        try {
            downloadAPK(versionName, url, name);
        } catch (Exception e) {
            e.printStackTrace();
            YchatToastUtils.showToastLong("下载失败，请稍后再试");
        }
    }

    //下载apk
    private void downloadAPK(String versionName, String url, String name) {
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        request.setTitle(versionName);
        request.setDescription("新版本下载中...");
        request.setVisibleInDownloadsUi(true);
        //request.setDestinationInExternalPublicDir("download", "time2plato.apk");
        // 设置为可被媒体扫描器找到
        request.allowScanningByMediaScanner();

        //设置下载的路径
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
        request.setDestinationUri(Uri.fromFile(file));
        pathstr = file.getAbsolutePath();
        //获取DownloadManager
        if (downloadManager == null)
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);
        }

        //注册广播接收者，监听下载状态
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        mContext.registerReceiver(receiver, filter);
    }

    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };

    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    //下载完成安装APK
                    cursor.close();
                    mContext.unregisterReceiver(receiver);
                    installAPK();
                    YchatToastUtils.showShort("下载成功！");
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    cursor.close();
                    mContext.unregisterReceiver(receiver);
                    YchatToastUtils.showShort("下载失败");
                    break;
            }
        }
    }

    private void installAPK() {
        setPermission(pathstr);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Android 7.0以上要使用FileProvider
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(mContext, "com.xr.ychat.fileprovider", new File(pathstr));
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(Environment.DIRECTORY_DOWNLOADS, apkName)), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }

    //修改文件权限
    private void setPermission(String absolutePath) {
        String command = "chmod " + "777" + " " + absolutePath;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    private static final int DOWNLOAD_ERROR = 3;
    /* 记录进度条数量 */
    private int progress;

    /* 更新进度条 */
    private ProgressBar mProgress;
    private TextView txProgress;
    private Dialog mDownloadDialog;
    private String updateApkUrl = "";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    txProgress.setText(progress + "%");
                    break;
                case DOWNLOAD_FINISH:

                    simpleCallback.onResult(true, "", 200);
                    // 安装文件
                    //installApk();

                    break;
                case DOWNLOAD_ERROR://更新异常的情况下，就用通知栏下载
                    simpleCallback.onResult(false, "", -1);
                    mDownloadDialog.dismiss();
                    //download(mContext, AppUtils.getAppName(), updateApkUrl, apkName);
                    break;
            }
        }

    };

    public DownloadUtils(Context context) {
        this.mContext = context;
    }

    private SimpleCallback simpleCallback;

    /**
     * 显示软件下载对话框
     */
    public void showDownloadDialog(String updateUrl, String newVersion, SimpleCallback simpleCallback) {
        this.updateApkUrl = updateUrl;
        this.simpleCallback = simpleCallback;
        apkName = "konglechui" + newVersion + ".apk";
        // 构造软件下载对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = v.findViewById(R.id.update_progress);
        txProgress = v.findViewById(R.id.txProgress);
        builder.setView(v);
        mDownloadDialog = builder.create();
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.show();
        // 启动新线程下载软件
        new DownloadUtils.DownloadApkThread().start();
    }


    /**
     * 下载文件线程
     *
     * @author coolszy
     * @date 2012-4-26
     * @blog http://blog.92coding.com
     */
    private class DownloadApkThread extends Thread {

        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径

                    //URL url = new URL(mHashMap.get("url"));
                    URL url = new URL(updateApkUrl);
                    // 创建连接 http://wdj-qn-apk.wdjcdn.com/f/90/7ab35e732d179be00997595620d4390f.apk
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int fileContentLength = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File apkDir = new File(CommonUtil.getCacheDirPath());
                    // 判断文件目录是否存在
                    if (!apkDir.exists()) {
                        apkDir.mkdirs();
                    }

                    File apkFile = new File(CommonUtil.getCacheDirPath(), apkName);
                    if (!apkFile.exists()) {
                        apkFile.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    int len = 0;
                    while ((len = is.read(buf)) != -1) {
                        count += len;
                        // 计算进度条位置
                        progress = (int) (((float) count / fileContentLength) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        // 写入文件
                        fos.write(buf, 0, len);
                        if (progress == 100) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                    }
                    fos.close();
                    is.close();
                }
            } catch (Exception e) {
                mHandler.sendEmptyMessage(DOWNLOAD_ERROR);
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    }


    /**
     * 新增了一个断点续传的方法
     *
     * @param updateInfo
     */
    public static void breakPointDownload(UpdateInfo updateInfo, SimpleCallback simpleCallback) {
        Activity activity = ActivityUtils.getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        FileDownloader.setup(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(activity);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        final ProgressBar mProgress = v.findViewById(R.id.update_progress);
        final TextView txProgress = v.findViewById(R.id.txProgress);
        builder.setView(v);
        final Dialog mDownloadDialog = builder.create();
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.show();
        String localPath = CommonUtil.getCacheDirPath() + File.separator + "konglechui" + updateInfo.getVersion() + ".apk";
        //FileDownloader.isReusedOldFile
        FileDownloader.getImpl().create(updateInfo.getDownUrl())
                .setPath(localPath)//下载文件的存储绝对路径
                .setForceReDownload(true)//强制重新下载，将会忽略检测文件是否健在
                .setAutoRetryTimes(0)//任务在下载失败的时候都自动重试0次
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        int progress = (int) (((float) soFarBytes / totalBytes) * 100);
                        CommonUtil.isDownloading = true;
                        // 设置进度条位置
                        mProgress.setProgress(progress);
                        txProgress.setText(progress + "%");

                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {

                        SPUtils.getInstance().put("localApkPath", CommonUtil.getCacheDirPath() + File.separator + "konglechui" + updateInfo.getVersion() + ".apk");
                        mDownloadDialog.dismiss();
                        simpleCallback.onResult(true, "", 200);
                        CommonUtil.isDownloading = false;
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        mDownloadDialog.dismiss();//下载失败，下回会重新断点续传
                        simpleCallback.onResult(false, "", -1);//0-100代表正在下载进行时
                        YchatToastUtils.showShort("下载失败");
                        CommonUtil.isDownloading = false;
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();
    }

    public static void installApk(Activity activity, SimpleCallback simpleCallback) {
        String localApkPath = SPUtils.getInstance().getString("localApkPath");
        File apkfile = new File(localApkPath);
        if (!apkfile.exists()) {
            YchatToastUtils.showToastLong("APK文件已损坏,需要重新下载.");
            CommonUtil.clearUpdateInfo();
            simpleCallback.onResult(false, "", 0);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
                //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 给目标应用一个临时授权
                intent.setDataAndType(FileProvider.getUriForFile(activity, "com.xr.ychat.fileprovider", apkfile), "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
            }
            activity.startActivity(intent);
        }

    }

    /**
     * 版本检测，判断是否更新
     * isForce(1：强制更新0：不需要强制更新)
     */
    public static void queryAppVersion(SimpleCallback simpleCallback) {
        SPUtils.getInstance().put("server_version", "");
        ContactHttpClient.getInstance().queryAppVersion(new ContactHttpClient.ContactHttpCallback<UpdateInfo>() {
            @Override
            public void onSuccess(UpdateInfo updateInfo) {
                if ("1".equals(updateInfo.getEmergency())) {
                    CommonUtil.webviwDownload(updateInfo.getEmergencyUrl());
                } else if ("1".equals(updateInfo.getUpdate())) {//需要更新
                    CommonUtil.setUpdateInfo(updateInfo.getIsForce(), updateInfo.getUpdate(), updateInfo.getVersion(), updateInfo.getDownUrl());
                    simpleCallback.onResult(true, updateInfo, 0);
                } else {
                    simpleCallback.onResult(false, updateInfo, 0);
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                simpleCallback.onResult(false, null, -1);
            }
        });
    }
}
