package com.netease.nim.uikit.business.session.viewholder;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.activity.WatchVideoActivity;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;

import java.io.File;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

/**
 * Created by zhoujianghua on 2015/8/5.
 */
public class MsgViewHolderVideo extends MsgViewHolderThumbBase {

    public MsgViewHolderVideo(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_video;
    }

    @Override
    protected void onItemClick() {
        if (CommonUtil.isFastDoubleClick()) {
            return;
        }
        WatchVideoActivity.start(context, message);
       /* try {
            VideoAttachment videoAttachment = (VideoAttachment) message.getAttachment();
            Log.e("xx","videoAttachment.getPath()="+videoAttachment.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri ;
            File file = new File(videoAttachment.getPath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){//24
                uri = FileProvider.getUriForFile(context,"com.xr.ychat.fileprovider",file);
            }else {
                uri = Uri.fromFile(file);
            }
            intent.setDataAndType(uri, "video/mp4");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("xx","e="+e.getMessage());
        }*/
    }

    @Override
    protected String thumbFromSourceFile(String path) {
        VideoAttachment attachment = (VideoAttachment) message.getAttachment();
        String thumb = attachment.getThumbPathForSave();
        return BitmapDecoder.extractThumbnail(path, thumb) ? thumb : null;
    }

    @Override
    protected int leftBackground() {
        return R.color.transparent;
    }

    @Override
    protected int rightBackground() {
        return R.color.transparent;
    }

}
