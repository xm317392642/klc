package com.xr.ychat.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.business.session.extension.TeamAuthAttachment;
import com.netease.nim.uikit.business.session.fragment.MessageFragment;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.helper.UpdateMemberChangeService;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ContactHttpClient;
import com.netease.nim.uikit.common.Preferences;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.xr.ychat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * 邀请详情
 * Created by hzxuwen on 2015/3/20.
 */
public class InviteDetailActivity extends SwipeBackUI {
    //private String teamId, fromId, toId, inviteContent, msgid;
    private TextView tx_invite_nickname, tx_invite_count, tx_invite_content;
    private Button btn;
    private HeadImageView invite_head_image;
    private GridView gridView;
    private IMMessage message;
    private TeamAuthAttachment paramAttachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.nim_invite_detail_activity);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.back_white_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(v -> finish());
        findViews();
        parseIntentData();
        updateUI();

        if ("-1".equals(paramAttachment.getInviteTipType())) {
            btn.setText("已确认");
            btn.setEnabled(false);
        } else {
            queryStatusRequest();
        }
    }

    private void findViews() {
        tx_invite_nickname = (TextView) findViewById(R.id.tx_invite_nickname);
        tx_invite_count = (TextView) findViewById(R.id.tx_invite_count);
        tx_invite_content = (TextView) findViewById(R.id.tx_invite_content);
        invite_head_image = (HeadImageView) findViewById(R.id.invite_head_image);
        gridView = findView(R.id.team_member_grid_view);
        btn = (Button) findViewById(R.id.btn);
    }

    private void parseIntentData() {
        Intent intent = getIntent();
        message = (IMMessage) intent.getSerializableExtra("message");
        paramAttachment = (TeamAuthAttachment) message.getAttachment();
    }


    private void updateUI() {
        tx_invite_nickname.setText(UserInfoHelper.getUserName(paramAttachment.getInviteTipFromId()));
        tx_invite_content.setText("\"" + paramAttachment.getInviteTipContent() + "\"");
        int count = 1;
        String toId = paramAttachment.getInviteTipToId();
        String[] toIdArray;
        if (toId.contains(",")) {
            toIdArray = toId.split(",");
            count = toIdArray.length;
        } else {
            toIdArray = new String[]{toId};
        }
        tx_invite_count.setText("邀请 " + count + " 位朋友加入群聊");
        invite_head_image.loadBuddyAvatar(paramAttachment.getInviteTipFromId());

        InviteMemberAdapter memberAdapter = new InviteMemberAdapter(this, toIdArray);
        //if (count >= 5) {
        gridView.setNumColumns(5);
//        }else{
//            gridView.setNumColumns(toIdArray.length);
//        }
        gridView.setAdapter(memberAdapter);
    }

    /**
     * 客户端查询是否有管理员同意邀请认证
     */
    private void queryStatusRequest() {
        DialogMaker.showProgressDialog(this, "", false);
        String uid = Preferences.getWeiranUid(this);
        String mytoken = Preferences.getWeiranToken(this);
        //msgid:时间戳+accid(时间戳单位：秒)
        ContactHttpClient.getInstance().queryInviteStatus(uid, mytoken, paramAttachment.getInviteTipId(), new ContactHttpClient.ContactHttpCallback<String>() {
            @Override
            public void onSuccess(String agree) {
                DialogMaker.dismissProgressDialog();
                if ("1".equals(agree)) {
                    btn.setText("已确认");
                    btn.setEnabled(false);
                    if(!"-1".equals(paramAttachment.getInviteTipType())){
                        setConfirmStatus();//因为群主手机确认了，本地状态变为-1了，但是其他手机端的管理员由于没有-1，所以进来查询后，再置为-1即可，就刷新为 “已确认”了。
                    }
                } else {
                    btn.setText("确认邀请");
                    btn.setOnClickListener(v -> agreeInviteRequest());//普通成员申请拉人后，群主、管理员点击同意邀请成功inteam=1后，发tipType=2的消息
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 客户端管理员同意邀请认证
     * 普通成员申请拉人后，群主、管理员点击同意邀请成功后（inteam=1），发tipType=2的消息
     */
    private void agreeInviteRequest() {
        if (!TeamHelper.canConfirmJoin(message.getSessionId(), NimUIKit.getAccount())) {
            YchatToastUtils.showShort("被取消管理无权限无法确认");
            finish();
            return;
        }
        DialogMaker.showProgressDialog(this, "", false);
        String uid = Preferences.getWeiranUid(this);
        String mytoken = Preferences.getWeiranToken(this);
        //msgid:时间戳+accid(时间戳单位：秒)
        Team team = NimUIKitImpl.getTeamProvider().getTeamById(message.getSessionId());
        String owner = team.getCreator();//群主
        ContactHttpClient.getInstance().agreeInvite(uid, mytoken, owner, message.getSessionId(), paramAttachment.getInviteTipFromId(), paramAttachment.getInviteTipToId(), paramAttachment.getInviteTipId(), TeamHelper.getDisplayNameWithoutMe(message.getSessionId(), NimUIKit.getAccount()), team.getName(),
                new ContactHttpClient.ContactHttpCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        DialogMaker.dismissProgressDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            btn.setText("已确认");
                            btn.setEnabled(false);
                            //processed=1,已处理，processed=0当前第一次处理
                            int processed = 0;
                            if (jsonObject.has("jsonObject")) {
                                processed = jsonObject.getInt("processed");
                            }
                            if (processed == 1) {
                                YchatToastUtils.showShort("邀请已被同意");
                                finish();
                            } else {
                                //普通成员申请拉人后，群主、管理员点击同意邀请成功后（inteam=1），发tipType=2的消息
                                if (jsonObject.getInt("inteam") == 1) {
                                    String toId = jsonObject.getString("toaccid");
                                    if (!TextUtils.isEmpty(toId)) {//因为之前的操作被邀请进群了，可能返回空。
                                        UpdateMemberChangeService.start(InviteDetailActivity.this, toId, team.getId(), 3, paramAttachment.getInviteTipFromId());
                                        sendTip(toId);
                                    }
                                } else {
                                    setConfirmStatus();//本地临时改变状态为-1已确认
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        DialogMaker.dismissProgressDialog();
                        YchatToastUtils.showShort(errorMsg + code);
                    }
                });
    }

    //String notifica = " " + "\"" + message.getFromNick() + "\"" + "想邀请" + toIdArray.length + "位朋友加入群聊  已确认";
    private void setConfirmStatus() {
        List<Activity> activityList = ActivityUtils.getActivityList();
        for (Activity activity : activityList) {
            if (TextUtils.equals("TeamMessageActivity", activity.getClass().getSimpleName())) {
                TeamMessageActivity teamMessageActivity = (TeamMessageActivity) activity;
                MessageFragment messageFragment = (MessageFragment) teamMessageActivity.getSupportFragmentManager().getFragments().get(0);
                messageFragment.messageListPanel.updateInviteAuthMessage(message);
                break;
            }
        }
    }

    /**
     * 普通成员申请拉人后，群主、管理员点击同意邀请成功后（inteam=1），发tipType=2的消息
     */
    private void sendTip(String toaccid) {

        TeamAuthAttachment authAttachment = new TeamAuthAttachment();
        authAttachment.setInviteTipType(TeamAuthAttachment.AGREE2);
        authAttachment.setInviteTipId(System.currentTimeMillis() / 1000 + NimUIKit.getAccount());//msgid:时间戳+accid(时间戳单位：秒)
        String fromId=((TeamAuthAttachment) message.getAttachment()).getInviteTipFromId();
        authAttachment.setInviteTipFromId(fromId);
        authAttachment.setInviteTipToId(toaccid);

        String connectNickname = "";
        String[] toIdArray = toaccid.split(",");
        for (int i = 0, len = toIdArray.length; i < len; i++) {
            String toNickname = TeamHelper.getTeamMemberDisplayNameYou(message.getSessionId(), toIdArray[i]);
            if (i == len - 1) {
                connectNickname = connectNickname + toNickname;
            } else {
                connectNickname = connectNickname + toNickname + ",";
            }
        }
        String tipContent=" " + TeamHelper.getTeamMemberDisplayNameYou(message.getSessionId(), fromId) + "邀请" + connectNickname + "进入了群 ";
        authAttachment.setInviteTipContent(tipContent);
        CustomMessageConfig config = new CustomMessageConfig();
        config.enableHistory = false;
        config.enablePush = false;
        config.enableUnreadCount = false;
        IMMessage imMessage = MessageBuilder.createCustomMessage(message.getSessionId(), SessionTypeEnum.Team, "", authAttachment, config);
        DialogMaker.showProgressDialog(this, "", true);
        NIMClient.getService(MsgService.class).sendMessage(imMessage, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                setConfirmStatus();
                List<Activity> activityList = ActivityUtils.getActivityList();
                for (Activity activity : activityList) {
                    if (TextUtils.equals("TeamMessageActivity", activity.getClass().getSimpleName())) {
                        TeamMessageActivity teamMessageActivity = (TeamMessageActivity) activity;
                        MessageFragment messageFragment = (MessageFragment) teamMessageActivity.getSupportFragmentManager().getFragments().get(0);
                        messageFragment.messageListPanel.onMsgSend(imMessage);
                        break;
                    }
                }


            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    class InviteMemberAdapter extends BaseAdapter        //设置图片适配器，继承自BaseAdapter
    {
        private Context mContext;
        private String[] toIdArray;

        public InviteMemberAdapter(Context c, String[] toIdArray) {
            this.mContext = c;
            this.toIdArray = toIdArray;
        }

        @Override
        public int getCount() {
            return toIdArray.length;
        }

        @Override
        public Object getItem(int position) {
            return toIdArray[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            HeadImageView headImageView;
            TextView nameTextView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holer;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(com.netease.nim.uikit.R.layout.nim_invite_detail_team_member_item, null);
                holer = new ViewHolder();
                holer.headImageView = convertView.findViewById(R.id.imageViewHeader);
                holer.nameTextView = convertView.findViewById(R.id.textViewName);
                convertView.setTag(holer);
            } else {
                holer = (ViewHolder) convertView.getTag();
            }
            String accoundId = toIdArray[position];
            holer.headImageView.loadBuddyAvatar(accoundId);
            holer.nameTextView.setText(UserInfoHelper.getUserName(accoundId));
            return convertView;
        }
    }
}
