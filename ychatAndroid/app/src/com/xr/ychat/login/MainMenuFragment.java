package com.xr.ychat.login;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.xr.ychat.R;
import com.xr.ychat.contact.activity.AddFriendActivity;

public class MainMenuFragment extends DialogFragment implements View.OnClickListener {
    private TextView chat;
    private TextView add;
    private TextView scan;
    private View background;
    private ClickableChildView clickableChildView;

    public void show(FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, "MainMenuFragment");
        ft.commit();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_main_menu, null);
        chat = view.findViewById(R.id.main_menu_chat);
        chat.setOnClickListener(this);
        add = view.findViewById(R.id.main_menu_add);
        add.setOnClickListener(this);
        scan = view.findViewById(R.id.main_menu_scan);
        scan.setOnClickListener(this);

        background = view.findViewById(R.id.main_menu_background);
        background.setOnClickListener(this);
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCancelable(true);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams attributes = dialogWindow.getAttributes();
        attributes.y = ScreenUtil.dip2px(45f);
        //attributes.x = ScreenUtil.dip2px(6f);
        dialogWindow.setAttributes(attributes);
        attributes.windowAnimations = R.style.main_menu_anim;
        dialogWindow.setGravity(Gravity.TOP | Gravity.RIGHT);
        ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(android.R.color.transparent));
        dialogWindow.setBackgroundDrawable(colorDrawable);
        dialogWindow.setDimAmount(0);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ClickableChildView) {
            clickableChildView = (ClickableChildView) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (clickableChildView != null) {
            clickableChildView = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_menu_chat: {
                dismiss();
                if (clickableChildView != null) {
                    clickableChildView.clickChatView();
                }
            }
            break;
            case R.id.main_menu_add: {
                dismiss();
                AddFriendActivity.start(getContext());
            }
            break;
            case R.id.main_menu_scan: {
                dismiss();
                ScanCodeActivity.start(getContext());
            }
            break;
            case R.id.main_menu_background: {
                dismiss();
            }
            break;
        }
    }

    public interface ClickableChildView {
        void clickChatView();
    }

}
