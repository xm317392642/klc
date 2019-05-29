package com.netease.nim.uikit.common.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;

public class MenuHidingEditText extends AppCompatEditText {
    private final Context mContext;

    public MenuHidingEditText(Context context) {
        super(context);
        this.mContext = context;

        blockContextMenu();
    }

    public MenuHidingEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        blockContextMenu();
    }

    public MenuHidingEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;

        blockContextMenu();
    }

    private void blockContextMenu() {
        this.setCustomSelectionActionModeCallback(new BlockedActionModeCallback());
        this.setLongClickable(false);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MenuHidingEditText.this.clearFocus();
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // setInsertionDisabled when user touches the view
            this.setInsertionDisabled();
        }
        return super.onTouchEvent(event);
    }

    private void setInsertionDisabled() {
        try {
            Field editorField = TextView.class.getDeclaredField("mEditor");
            editorField.setAccessible(true);
            Object editorObject = editorField.get(this);

            Class editorClass = Class.forName("android.widget.Editor");
            Field mInsertionControllerEnabledField = editorClass.getDeclaredField("mInsertionControllerEnabled");
            mInsertionControllerEnabledField.setAccessible(true);
            mInsertionControllerEnabledField.set(editorObject, false);
        } catch (Exception ignored) {
            // ignore exception here
        }
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    private class BlockedActionModeCallback implements ActionMode.Callback {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}
