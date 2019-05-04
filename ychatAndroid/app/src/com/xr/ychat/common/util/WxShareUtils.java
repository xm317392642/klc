package com.xr.ychat.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xr.ychat.R;
import com.xr.ychat.login.MyCodeActivity;

public class WxShareUtils {
    public static final String APP_ID = "wx023ce9cfb56beca3";

    /**
     * 分享网页类型至微信
     *
     * @param context 上下文
     * @param appId   微信的appId
     * @param webUrl  网页的url
     * @param title   网页标题
     * @param content 网页描述
     * @param bitmap  位图
     */
    public static void shareWeb(Context context, String appId, String webUrl, String title, String content, Bitmap bitmap) {
        // 通过appId得到IWXAPI这个对象
        IWXAPI wxapi = WXAPIFactory.createWXAPI(context, appId);
        // 检查手机或者模拟器是否安装了微信
        if (!wxapi.isWXAppInstalled()) {
            YchatToastUtils.showShort( "您还没有安装微信");
            return;
        }

        // 初始化一个WXWebpageObject对象
        WXWebpageObject webpageObject = new WXWebpageObject();
        // 填写网页的url
        webpageObject.webpageUrl = webUrl;

        // 用WXWebpageObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage(webpageObject);
        // 填写网页标题、描述、位图
        msg.title = title;
        msg.description = content;
        // 如果没有位图，可以传null，会显示默认的图片
        msg.setThumbImage(bitmap);

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        // transaction用于唯一标识一个请求（可自定义）
        req.transaction = "webpage";
        // 上文的WXMediaMessage对象
        req.message = msg;
        // SendMessageToWX.Req.WXSceneSession是分享到好友会话
        // SendMessageToWX.Req.WXSceneTimeline是分享到朋友圈
        req.scene = SendMessageToWX.Req.WXSceneSession;

        // 向微信发送请求
        wxapi.sendReq(req);
    }

    //bitmap中的透明色用白色替换
    public static Bitmap changeColor(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] colorArray = new int[w * h];
        int n = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = getMixtureWhite(bitmap.getPixel(j, i));
                colorArray[n++] = color;
            }
        }
        return Bitmap.createBitmap(colorArray, w, h, Bitmap.Config.ARGB_8888);
    }

    //获取和白色混合颜色
    private static int getMixtureWhite(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.rgb(getSingleMixtureWhite(red, alpha), getSingleMixtureWhite(green, alpha),
                getSingleMixtureWhite(blue, alpha));
    }

    // 获取单色的混合值
    private static int getSingleMixtureWhite(int color, int alpha) {
        int newColor = color * alpha / 255 + 255 - alpha;
        return newColor > 255 ? 255 : newColor;
    }

    public static void shareContent(Context context, String webUrl) {
        String title = "和好友一起加入空了吹";
        String content = "我们都在用空了吹，快来加入我们吧";
        //本地文件
        Glide.with(context).asBitmap().load(R.mipmap.logo).into(new SimpleTarget<Bitmap>() {
            /**
             * 成功的回调
             */
            @Override
            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                // 下面这句代码是一个过度dialog，因为是获取网络图片，需要等待时间
                // 调用方法
                WxShareUtils.shareWeb(context, APP_ID,
                        webUrl, title, content,
                        changeColor(bitmap));
            }

            /**
             * 失败的回调
             */
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                WxShareUtils.shareWeb(context, APP_ID,
                        webUrl, title, content,
                        null);
            }
        });
    }

    public static void share(Context context) {
        NimUserInfo nimUserInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(NimUIKit.getAccount());
        if (nimUserInfo == null) {
            NimUIKit.getUserInfoProvider().getUserInfoAsync(NimUIKit.getAccount(), (success, result, code) -> {
                shareContent(context, MyCodeActivity.USER_FORMAT + ((NimUserInfo) result).getSignature());
            });
        } else {
            shareContent(context, MyCodeActivity.USER_FORMAT + nimUserInfo.getSignature());
        }


    }
}