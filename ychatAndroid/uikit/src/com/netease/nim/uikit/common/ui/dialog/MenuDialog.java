package com.netease.nim.uikit.common.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangjun on 2015/3/21.
 */
public class MenuDialog extends Dialog implements View.OnClickListener {
    public interface MenuDialogOnButtonClickListener {
        public void onButtonClick(final String name);
    }

    private Context context;
    private ViewGroup rootView;
    private LinearLayout itemsRootView;

    private List<String> btnNames;
    private List<View> itemViews;
    private MenuDialogOnButtonClickListener clickListener;
    private boolean selectMode = false;
    private int selectIndex = -1; // 要勾选的项
    private int invalidSelectIndex = -1; // 不能勾选的项目
    private int preSelectIndex = -1; // 之前勾选的项目

    public MenuDialog(Context context, List<String> btnNames, MenuDialogOnButtonClickListener listener) {
        super(context, R.style.dialog_default_style);
        this.context = context;
        this.btnNames = btnNames;
        this.clickListener = listener;
    }

    public MenuDialog(Context context, List<String> btnNames, int selectIndex, int invalidSelectIndex,
                      MenuDialogOnButtonClickListener listener) {
        this(context, btnNames, listener);

        if (selectIndex >= 0 && selectIndex < btnNames.size()) {
            this.selectMode = true;
            this.selectIndex = selectIndex;
            this.preSelectIndex = selectIndex;
            this.invalidSelectIndex = invalidSelectIndex;
        }
    }

    /**
     * 设置菜单item文字的颜色
     *
     * @param textView
     * @param color
     */
    private void setMenuTextColor(TextView textView, int color) {
        textView.setTextColor(context.getResources().getColor(color));
    }

    private boolean isSetGray(String value) {
        if (
                value.equals(
                        context.getString(R.string.team_member_remove_confirm)) ||
                        value.equals(context.getString(R.string.clear_team_msg)) ||
                        value.equals(context.getString(R.string.delete_and_exit)) ||
                        value.equals(context.getString(R.string.out_team)) ||
                        value.equals("请操作")
        ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        rootView = (ViewGroup) View.inflate(context, R.layout.nim_menu_dialog, null);
        itemsRootView = rootView.findViewById(R.id.menu_dialog_items_root);
        if (selectMode) {
            itemViews = new ArrayList<>();
        }
        View itemView;
        for (int i = 0; i < btnNames.size(); i++) {
            String value = btnNames.get(i);
            itemView = View.inflate(context, R.layout.nim_menu_dialog_item, null);
            TextView tx = itemView.findViewById(R.id.menu_button);
            tx.setText(value);
            if (isSetGray(value)) {
                setMenuTextColor(tx, R.color.color_grey_999999);//设置为灰色
                tx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            }
            if (value.equals(context.getString(R.string.ok))) {
                setMenuTextColor(tx, R.color.color_red_ff0000);
            }
            if (value.equals(context.getString(R.string.cancel))) {
                setMenuTextColor(tx, R.color.black);//当为取消按钮的时候，设置为黑色
            }
            if (value.contains("微信好友")) {
                tx.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.wechat), null, null, null);
            } else if (value.contains("朋友圈")) {
                tx.setCompoundDrawablesWithIntrinsicBounds( ContextCompat.getDrawable(context, R.drawable.wechat_circle), null,null, null);
            }
            itemView.setTag(i);
            itemView.setOnClickListener(this);
            if (selectMode) {
                itemViews.add(itemView);
            }
            itemsRootView.addView(itemView);
        }

        selectItem();

        rootView.setOnClickListener(v -> dismiss());
        setContentView(rootView);
        Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//设置横向全屏
        window.setWindowAnimations(R.style.dialog_default_anim);  //添加动画
        //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//            @Override
//            public void onSystemUiVisibilityChange(int visibility) {
//                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                        //布局位于状态栏下方
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                        //全屏
//                        View.SYSTEM_UI_FLAG_FULLSCREEN |
//                        //隐藏导航栏
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//                if (Build.VERSION.SDK_INT >= 19) {
//                    uiOptions |= 0x00001000;
//                } else {
//                    uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
//                }
//                window.getDecorView().setSystemUiVisibility(uiOptions);
//            }
//        });
    }

    // 撤销最后一次选择，恢复上一次选择
    public void undoLastSelect() {
        if (selectMode && preSelectIndex >= 0 && preSelectIndex < btnNames.size()) {
            selectIndex = preSelectIndex;
            selectItem();
        }
    }

    private void selectItem() {
        if (selectMode == false || selectIndex < 0 || selectIndex >= btnNames.size() || itemViews == null || itemViews
                .isEmpty()) {
            return;
        }

        View item;
        for (int i = 0; i < itemViews.size(); i++) {
            item = itemViews.get(i);
            item.findViewById(R.id.menu_select_icon).setVisibility(selectIndex == i ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int index = (int) v.getTag();
        if (selectMode && index != invalidSelectIndex) {
            preSelectIndex = selectIndex;
            selectIndex = index;
            selectItem();
        }

        String btnName = btnNames.get(index);
        if (clickListener != null) {
            clickListener.onButtonClick(btnName);
        }
    }

}
