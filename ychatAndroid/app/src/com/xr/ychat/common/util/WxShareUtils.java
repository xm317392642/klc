package com.xr.ychat.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xr.ychat.R;
import com.xr.ychat.login.MyCodeActivity;

import java.util.ArrayList;
import java.util.List;

public class WxShareUtils {
    public static final String APP_ID = "wx023ce9cfb56beca3";
    static MenuDialog menuDialog;

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



    /**
     * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码）
     * @param flag 0:分享到微信好友，1：分享到微信朋友圈)
     */

    public static void wechatShare(Context context,NimUserInfo nimUserInfo,int flag,IWXAPI wxapi){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = MyCodeActivity.USER_FORMAT + nimUserInfo.getSignature();
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "和好友一起加入空了吹";
        msg.description = "我们都在用空了吹，快来加入我们吧！";
        //这里替换一张自己工程里的图片资源
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo);
        msg.setThumbImage(changeColor(thumb));

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;
        wxapi.sendReq(req);
    }
    /**
     * 显示分享菜单
     */
    public static void shareMenu(Context context) {
        // 通过appId得到IWXAPI这个对象
        IWXAPI wxapi = WXAPIFactory.createWXAPI(context, APP_ID);
        // 检查手机或者模拟器是否安装了微信
        if (!wxapi.isWXAppInstalled()) {
            YchatToastUtils.showShort( "您还没有安装微信");
            return;
        }
        List<String> btnNames = new ArrayList<>(2);
        btnNames.add("  分享微信好友");
        btnNames.add("  分享到朋友圈");
         menuDialog = new MenuDialog(context, btnNames, name -> {
             NimUserInfo nimUserInfo = (NimUserInfo) NimUIKit.getUserInfoProvider().getUserInfo(NimUIKit.getAccount());
             if(name.contains("朋友圈")){
                 if (nimUserInfo == null) {
                     NimUIKit.getUserInfoProvider().getUserInfoAsync(NimUIKit.getAccount(), (success, result, code) -> {
                         wechatShare(context,nimUserInfo,1,wxapi);//分享到微信朋友圈
                     });
                 } else {
                     wechatShare(context,nimUserInfo,1,wxapi);//分享到微信朋友圈
                 }
             }else{
                 if (nimUserInfo == null) {
                     NimUIKit.getUserInfoProvider().getUserInfoAsync(NimUIKit.getAccount(), (success, result, code) -> {
                         wechatShare(context,nimUserInfo,0,wxapi);//分享到微信好友
                     });
                 } else {
                     wechatShare(context,nimUserInfo,0,wxapi);//分享到微信好友
                 }
             }
            menuDialog.dismiss();
             menuDialog=null;
        });


        menuDialog.show();
    }
}