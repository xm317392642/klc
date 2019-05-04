package com.netease.nim.uikit.common.util.string;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.blankj.utilcode.util.StringUtils;
import com.netease.nim.uikit.api.model.SimpleCallback;

/**
 * EditText字符数限制
 * Created by hzxuwen on 2015/5/22.
 */
public class StringTextWatcher implements TextWatcher {
    private int length;
    private EditText editText;
    private SimpleCallback simpleCallback;
    public StringTextWatcher(int length, EditText editText) {
        this.length = length;
        this.editText = editText;
    }
    public StringTextWatcher(int length, EditText editText, SimpleCallback simpleCallback) {
        this.length = length;
        this.editText = editText;
        this.simpleCallback=simpleCallback;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        int editEnd = editText.getSelectionEnd();
        editText.removeTextChangedListener(this);
        while (StringUtils.length(s.toString()) > length && editEnd > 0) {
            s.delete(editEnd - 1, editEnd);
            editEnd--;
        }
        editText.setSelection(editEnd);
        editText.addTextChangedListener(this);
        if(simpleCallback!=null){
            simpleCallback.onResult(true,s.toString(),200);
        }

    }
}
