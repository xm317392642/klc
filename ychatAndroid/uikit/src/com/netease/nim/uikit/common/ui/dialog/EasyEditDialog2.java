package com.netease.nim.uikit.common.ui.dialog;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.CommonUtil;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.team.model.Team;

/**
 * 简单的带有输入框的对话框
 * <p>
 * Created by huangjun on 2015/5/28.股灾
 */
public class EasyEditDialog2 extends Dialog {
    private ImageView share_img;
    private TextView mTitleTextView, txShareName, txShareDesc;

    private TextView mMessageTextView;

    private EditText mEdit;


    private Button mPositiveBtn;

    private Button mNegativeBtn;

    private int mResourceId;

    private View.OnClickListener mPositiveBtnListener;

    private View.OnClickListener mNegativeBtnListener;

    private String mTitle, mSubTitle, mDesc;
    private String localPicPath;
    private int mPositiveBtnStrResId = R.string.ok;

    private int mNegativeBtnStrResId = R.string.cancel;
    private int posBtnColor;
    private String mMessage;

    private String mEditHint, mEditText, headImgAddress;

    private int mMaxEditTextLength;

    private int mMaxLines = 0;

    private boolean mSingleLine = false;

    private boolean mShowEditTextLength = false;

    private int inputType = -1;
    private HeadImageView headImageView;

    public EasyEditDialog2(Context context, int resourceId, int style) {
        super(context, style);
        mMaxEditTextLength = 16;
        if (-1 != resourceId) {
            setContentView(resourceId);
            this.mResourceId = resourceId;
        }
        LayoutParams Params = getWindow().getAttributes();
        Params.width = LayoutParams.MATCH_PARENT;
        Params.height = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(Params);
    }

    public EasyEditDialog2(Context context, int style) {
        this(context, -1, style);
        mResourceId = R.layout.nim_easy_alert_dialog_with_edit_share;
    }

    public EasyEditDialog2(Context context) {
        this(context, R.style.sdk_share_dialog);
        mResourceId = R.layout.nim_easy_alert_dialog_with_edit_share;
    }

    public void setShareImgFilepath(String localPicPath) {
        this.localPicPath = localPicPath;
    }

    public void setTitle(String title) {
        if (null != title) {
            this.mTitle = title;
            if (null != mTitleTextView)
                mTitleTextView.setText(title);
        }
    }

    public void setSubTitle(String title) {
        this.mSubTitle = title;
    }

    public void setmDesc(String mDesc) {
        this.mDesc = mDesc;
    }

    public void setMessage(String message) {
        if (null != message) {
            this.mMessage = message;
            if (null != mMessageTextView)
                mMessageTextView.setText(message);
        }
    }

    private String accountId;
    private Team team;

    public void setP2pAddr(String accountId) {
        this.accountId = accountId;

    }

    public void setTeamAddr(Team team) {
        this.team = team;


    }

    public void setEditHint(String hint) {
        if (!TextUtils.isEmpty(hint)) {
            this.mEditHint = hint;
            if (null != mEdit) {
                mEdit.setHint(hint);
            }
        }
    }

    public void setEditText(String text) {
        if (!TextUtils.isEmpty(text)) {
            this.mEditText = text;
            if (null != mEdit) {
                mEdit.setText(text);
            }
        }
    }

    public void setInputType(int type) {
        this.inputType = type;
    }

    public void setEditTextMaxLength(int maxLength) {
        this.mMaxEditTextLength = maxLength;
        this.mShowEditTextLength = true;
    }

    public void setEditTextMaxLines(int maxLines) {
        this.mMaxLines = maxLines;
    }

    public void setEditTextSingleLine() {
        this.mSingleLine = true;
    }

    public void addPositiveButtonListener(int resId, int posBtnColor, View.OnClickListener positiveBtnListener) {
        this.mPositiveBtnListener = positiveBtnListener;
        this.mPositiveBtnStrResId = resId;
        this.posBtnColor = posBtnColor;
    }

    public void addPositiveButtonListener(int resId, View.OnClickListener positiveBtnListener) {
        this.mPositiveBtnStrResId = resId;
        this.mPositiveBtnListener = positiveBtnListener;
    }

    public void addPositiveButtonListener(View.OnClickListener positiveBtnListener) {
        this.mPositiveBtnListener = positiveBtnListener;
    }

    public void addNegativeButtonListener(View.OnClickListener negativeBtnListener) {
        this.mNegativeBtnListener = negativeBtnListener;
    }

    public void addNegativeButtonListener(int resId, View.OnClickListener negativeBtnListener) {
        this.mNegativeBtnStrResId = resId;
        this.mNegativeBtnListener = negativeBtnListener;
    }

    public int getResourceId() {
        return mResourceId;
    }

    public void setResourceId(int resourceId) {
        this.mResourceId = resourceId;
    }

    public Button getPositiveBtn() {
        return mPositiveBtn;
    }

    public Button getNegativeBtn() {
        return mNegativeBtn;
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 计算最佳采样率
     *计算合适的采样率(当然这里还可以自己定义计算规则)，reqWidth和reqHeight为期望的图片大小，单位是px
     */
    private static int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int sourceWidth = options.outWidth;
        int sourceHeight = options.outHeight;
        int inSampleSize = 1;
        if (sourceHeight > reqHeight && sourceWidth > reqWidth) {
            int halfWidth = sourceWidth / 2;
            int halfHeight = sourceHeight / 2;
            while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mResourceId);
        try {
            LinearLayout root = (LinearLayout) findViewById(R.id.easy_edit_dialog_root);
            txShareName = findViewById(R.id.share_name);
            txShareDesc = findViewById(R.id.share_desc);
            share_img = findViewById(R.id.share_img);
            headImageView = findViewById(R.id.share_usericon);
            ViewGroup.LayoutParams params = root.getLayoutParams();
            params.width = (int) ScreenUtil.getDialogWidth();
            root.setLayoutParams(params);

            if (accountId != null && !CommonUtil.ASSISTANT_ACCOUNT.equals(accountId)) {
                headImageView.loadBuddyAvatar(accountId);
            }
            if (team != null) {
                headImageView.loadTeamIconByTeam(NimUIKit.getTeamProvider().getTeamMemberList(team.getId()), team);
            }

            if (!TextUtils.isEmpty(localPicPath)) {
                share_img.setVisibility(View.VISIBLE);
                //share_img.setImageBitmap(BitmapFactory.decodeFile(localPicPath));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;// 设置为true，解析图片原始宽高，不加载,只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
                BitmapFactory.decodeFile(localPicPath, options);
                int outWidth = options.outWidth;
                int outHeight = options.outHeight;
                ViewGroup.LayoutParams layoutParams = share_img.getLayoutParams();
                if (outHeight > outWidth) {
                    options.inSampleSize = calculateSampleSize(options,170,240); // 设置为刚才计算的压缩比例
                    layoutParams.height = ScreenUtil.dip2px(240f);
                    layoutParams.width = ScreenUtil.dip2px(170f);
                } else {
                    options.inSampleSize = calculateSampleSize(options,300,150); // 设置为刚才计算的压缩比例
                    layoutParams.height = ScreenUtil.dip2px(150f);
                    layoutParams.width = ScreenUtil.dip2px(300f);
                }
                options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
                Bitmap bitmap = BitmapFactory.decodeFile(localPicPath, options); // 解码文件
                share_img.setImageBitmap(bitmap);
            } else {
                share_img.setVisibility(View.GONE);
            }
            if (mSubTitle != null) {
                txShareName.setText(mSubTitle);
            }
            if (mDesc != null) {
                txShareDesc.setVisibility(View.VISIBLE);
                txShareDesc.setText(mDesc);
            }
            if (mTitle != null) {
                mTitleTextView = (TextView) findViewById(R.id.easy_dialog_title_text_view);
                mTitleTextView.setText(mTitle);
            }

            if (mMessage != null) {
                mMessageTextView = (TextView) findViewById(R.id.easy_dialog_message_text_view);
                mMessageTextView.setText(mMessage);
                mMessageTextView.setVisibility(View.VISIBLE);
            }

            mEdit = (EditText) findViewById(R.id.easy_alert_dialog_edit_text);
            //mLengthTextView.setVisibility(mShowEditTextLength ? View.VISIBLE : View.GONE);
            if (inputType != -1) {
                mEdit.setInputType(inputType);
            }
//            mEdit.addTextChangedListener(new EditTextWatcher(mEdit, mLengthTextView, mMaxEditTextLength,
//                    mShowEditTextLength));

            if (!TextUtils.isEmpty(mEditHint)) {
                mEdit.setHint(mEditHint);
            }
            if (!TextUtils.isEmpty(mEditText)) {
                mEdit.setText(mEditText);
            }
            if (mMaxLines > 0) {
                mEdit.setMaxLines(mMaxLines);
            }
            if (mSingleLine) {
                mEdit.setSingleLine();
            }

            mPositiveBtn = (Button) findViewById(R.id.easy_dialog_positive_btn);
            if (mPositiveBtnStrResId != 0) {
                mPositiveBtn.setText(mPositiveBtnStrResId);
            }
            if (posBtnColor != 0) {
                mPositiveBtn.setTextColor(ContextCompat.getColor(getContext(), posBtnColor));
            }
            mPositiveBtn.setOnClickListener(mPositiveBtnListener);

            mNegativeBtn = (Button) findViewById(R.id.easy_dialog_negative_btn);
            if (mNegativeBtnStrResId != 0) {
                mNegativeBtn.setText(mNegativeBtnStrResId);
            }
            mNegativeBtn.setOnClickListener(mNegativeBtnListener);
            mNegativeBtn.setVisibility(View.VISIBLE);
            findViewById(R.id.easy_dialog_btn_divide_view).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEditMessage() {
        if (mEdit != null)
            return mEdit.getEditableText().toString();
        else return null;
    }

    public static class EditTextWatcher implements TextWatcher {

        private EditText editText;

        private TextView lengthTV;

        private int maxLength;

        private boolean show = false;

        public EditTextWatcher(EditText editText, TextView lengthTV, int maxLength, boolean show) {
            this.maxLength = maxLength;
            this.editText = editText;
            this.lengthTV = lengthTV;
            this.show = show;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (editText == null)
                return;
            int editStart = editText.getSelectionStart();
            int editEnd = editText.getSelectionEnd();
            editText.removeTextChangedListener(this);
            while (StringUtil.counterChars(s.toString()) > maxLength) {
                s.delete(editStart - 1, editEnd);
                editStart--;
                editEnd--;
            }
            editText.setSelection(editStart);
            editText.addTextChangedListener(this);
            if (show && lengthTV != null) {
                long remainLength = maxLength - StringUtil.counterChars(s.toString());
                lengthTV.setText("" + remainLength / 2);
                lengthTV.setVisibility(View.VISIBLE);
            }
        }
    }
}
