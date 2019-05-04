package com.netease.nim.uikit.common.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ImageUtils;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.ui.combinebitmap.helper.BitmapLoader;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.AspectRatioImageView;
import com.netease.nim.uikit.common.util.QrCodeUtils;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroupCodeActivity extends SwipeBackUI {
    public static final String GROUP_FORMAT = "http://share.yaoliaoim.com?groupid=";
    public static final String GROUP_CODE = "group_code";
    public static String ROOTPAHT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ychat" + File.separator;
    private TextView toolbarTitle;
    private TextView slogan;
    private ImageView moreAction;
    private HeadImageView userAvatar;
    private TextView userName;
    private AspectRatioImageView codeImage;
    private ConstraintLayout constraintLayout;
    private Team team;
    private MenuDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_qrcode);

        team = (Team) getIntent().getSerializableExtra(GROUP_CODE);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setNavigationIcon(R.drawable.nim_actionbar_white_back_icon);
        mToolbar.setTitle("");
        toolbarTitle.setText("群二维码");
        mToolbar.setNavigationOnClickListener(v -> finish());
        moreAction = (ImageView) findViewById(R.id.toolbar_more);
        moreAction.setOnClickListener(v -> {
            showRegularTeamMenu();
        });
        userAvatar = (HeadImageView) findViewById(R.id.user_card_avatar);
        userName = (TextView) findViewById(R.id.user_card_name);
        codeImage = (AspectRatioImageView) findViewById(R.id.group_card_image);
        constraintLayout = (ConstraintLayout) findViewById(R.id.group_card_info);
        slogan = (TextView) findViewById(R.id.group_card_slogan);
        slogan.setText("");
        getUserInfo();
    }

    private void getUserInfo() {
        NIMClient.getService(TeamService.class).queryMemberList(team.getId()).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> param) {
                if (param != null && param.size() > 0) {
                    BitmapLoader.getInstance(GroupCodeActivity.this).removeBitmapToMemoryCache("ychat://com.xr.ychat?groupId=" + team.getId());
                    userAvatar.loadTeamIconByTeam(param, team);
                }
            }

            @Override
            public void onFailed(int code) {
                userAvatar.setImageResource(R.drawable.nim_avatar_group);
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
        userName.setText(team.getName());
        codeImage.post(new Runnable() {
            @Override
            public void run() {
                int width = codeImage.getWidth();
                int height = codeImage.getHeight();
                if (width > 0 && height > 0) {
                    String name = GROUP_FORMAT + team.getId();
                    Bitmap bitmap = QrCodeUtils.createQRCodeBitmap(name, width, height);
                    codeImage.setImageBitmap(bitmap);
                }
            }
        });
    }

    public static void start(Context context, Team team) {
        Intent intent = new Intent(context, GroupCodeActivity.class);
        intent.putExtra(GROUP_CODE, team);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * ******************************* Event *********************************
     */
    /**
     * 显示菜单
     */
    private void showRegularTeamMenu() {
        if (dialog == null) {
            List<String> btnNames = new ArrayList<>();
            btnNames.add("保存图片");
            btnNames.add("分享");
            btnNames.add("取消");
            dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    if (name.equals(btnNames.get(0))) {
                        saveCard();
                    } else if (name.equals(btnNames.get(1))) {
                        Bitmap bitmap = Bitmap.createBitmap(constraintLayout.getWidth(), constraintLayout.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        constraintLayout.draw(canvas);
                        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("image/*");//设置分享内容的类型
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent = Intent.createChooser(intent, "分享到");
                        startActivity(intent);
                    }
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    private void saveCard() {
        Bitmap bitmap = Bitmap.createBitmap(constraintLayout.getWidth(), constraintLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        constraintLayout.draw(canvas);
        File file = new File(ROOTPAHT, "group_info.jpg");
        ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        YchatToastUtils.showShort("图片保存成功");
    }
}