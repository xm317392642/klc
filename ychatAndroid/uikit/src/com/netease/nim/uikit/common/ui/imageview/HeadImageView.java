package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.TeamExtension;
import com.netease.nim.uikit.common.ui.combinebitmap.CombineBitmap;
import com.netease.nim.uikit.common.ui.combinebitmap.layout.WechatLayoutManager;
import com.netease.nim.uikit.common.ui.combinebitmap.listener.OnSubItemClickListener;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.impl.cache.TeamDataCache;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.util.NosThumbImageUtil;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import java.util.Iterator;
import java.util.List;

/**
 * Created by huangjun on 2015/11/13.
 */
public class HeadImageView extends RoundedImageView {

    public static final int DEFAULT_AVATAR_THUMB_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_max_size);
    public static final int DEFAULT_AVATAR_NOTIFICATION_ICON_SIZE = (int) NimUIKit.getContext().getResources().getDimension(R.dimen.avatar_notification_size);
    private static final int DEFAULT_AVATAR_RES_ID = R.drawable.nim_avatar_default;

    public HeadImageView(Context context) {
        super(context);
    }

    public HeadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param url 头像地址
     */
    public void loadAvatar(final String url) {
        doLoadImage(url, DEFAULT_AVATAR_RES_ID, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param account 用户账号
     */
    public void loadBuddyAvatar(String account) {
        final UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
        doLoadImage(userInfo != null ? userInfo.getAvatar() : null, DEFAULT_AVATAR_RES_ID, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param message 消息
     */
    public void loadBuddyAvatar(IMMessage message) {
        String account = message.getFromAccount();
        if (message.getMsgType() == MsgTypeEnum.robot) {
            RobotAttachment attachment = (RobotAttachment) message.getAttachment();
            if (attachment.isRobotSend()) {
                account = attachment.getFromRobotAccount();
            }
        }
        if (TextUtils.equals(account, CommonUtil.ASSISTANT_ACCOUNT)) {
            setImageResource(R.drawable.nim_avatar_assistant);
        } else {
            loadBuddyAvatar(account);
        }
    }

    /**
     * 加载群头像（默认大小的缩略图）
     *
     * @param team 群
     */
    public void loadTeamIconByTeam(final Team team) {
        doLoadImage(team != null ? team.getIcon() : null, R.drawable.nim_avatar_group, DEFAULT_AVATAR_THUMB_SIZE);
    }

    /**
     * 加载群组合头像
     */
    public void loadTeamIconByTeam(List<TeamMember> members, Team team) {
        String robotId = getRobotId(team);
        if (!TextUtils.isEmpty(robotId)) {
            Iterator<TeamMember> iterator = members.iterator();
            while (iterator.hasNext()) {
                TeamMember teamMember = iterator.next();
                if (TextUtils.equals(teamMember.getAccount(), robotId)) {
                    iterator.remove();
                }
            }
        }
        if (members.size() > 9) {
            members = members.subList(0, 9);
        }
        CombineBitmap.init(getContext())
                .setLayoutManager(new WechatLayoutManager())
                .setSize(DEFAULT_AVATAR_THUMB_SIZE)
                .setGap(2)
                .setGapColor(Color.parseColor("#E8E8E8"))
                .setMembers(members)
                .setTeamUrl("ychat://com.xr.ychat?groupId=" + team.getId())
                .setImageView(this)
                .setOnSubItemClickListener(new OnSubItemClickListener() {
                    @Override
                    public void onSubItemClick(int index) {

                    }
                }).build();
    }

    public void loadTeamIconByTeam(List<TeamMember> members, String teamId) {
        Team result = TeamDataCache.getInstance().getTeamById(teamId);
        if (result != null) {
            loadTeamIconByTeam(members, result);
        }
    }

    private String getRobotId(Team team) {
        if (team == null) {
            return null;
        }
        TeamExtension extension;
        if (!TextUtils.isEmpty(team.getExtension())) {
            try {
                Gson gson = new Gson();
                extension = gson.fromJson(team.getExtension(), new TypeToken<TeamExtension>() {
                }.getType());
            } catch (Exception exception) {
                extension = new TeamExtension();
            }
        } else {
            extension = new TeamExtension();
        }
        return extension.getRobotId();
    }

    /**
     * ImageLoader异步加载
     */
    private void doLoadImage(final String url, final int defaultResId, final int thumbSize) {
        /*
         * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
         * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
         */
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(ScreenUtil.dip2px(8f));
        final String thumbUrl = makeAvatarThumbNosUrl(url, thumbSize);
        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .bitmapTransform(roundedCorners)
                .placeholder(defaultResId)
                .error(defaultResId)
                .override(thumbSize, thumbSize);


        Glide.with(getContext().getApplicationContext()).asBitmap()
                .load(thumbUrl)
                .apply(requestOptions)
                .into(this);
    }

    /**
     * 解决ViewHolder复用问题
     */
    public void resetImageView() {
        setImageBitmap(null);
    }

    /**
     * 生成头像缩略图NOS URL地址（用作ImageLoader缓存的key）
     */
    private static String makeAvatarThumbNosUrl(final String url, final int thumbSize) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        return thumbSize > 0 ? NosThumbImageUtil.makeImageThumbUrl(url, NosThumbParam.ThumbType.Crop, thumbSize, thumbSize) : url;
    }

    public static String getAvatarCacheKey(final String url) {
        return makeAvatarThumbNosUrl(url, DEFAULT_AVATAR_THUMB_SIZE);
    }
}
