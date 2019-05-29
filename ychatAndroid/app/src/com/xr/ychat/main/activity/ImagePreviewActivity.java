package com.xr.ychat.main.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.xr.ychat.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ImagePreviewActivity extends SwipeBackUI {
    private static final String EXTRA_IMAGE = "extra_image";
    private PhotoView photoView;
    private HeadImageView headImageView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityView(R.layout.activity_image_preview);
        url = getIntent().getData().getQueryParameter(EXTRA_IMAGE);
        headImageView = (HeadImageView) findViewById(R.id.head_view);
        photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setOnPhotoTapListener(((view, x, y) -> finish()));
        if (TextUtils.isEmpty(url)||"0".equals(url)) {
            headImageView.setVisibility(View.GONE);
            photoView.setImageResource(R.drawable.nim_avatar_default);
        } else {
            headImageView.setVisibility(View.VISIBLE);
            headImageView.loadAvatar(url);
            RequestOptions options = new RequestOptions()
                    .dontAnimate()
                    .centerCrop()
                    .error(R.drawable.nim_avatar_default)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .placeholder(R.color.color_f2f2f2);
            Observable.create((ObservableEmitter<Bitmap> emitter) -> {
                emitter.onNext(Glide.with(ImagePreviewActivity.this).asBitmap().load(url).apply(options).submit().get());
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Bitmap>() {
                        @Override
                        public void accept(Bitmap s) throws Exception {
                            headImageView.setVisibility(View.GONE);
                            photoView.setImageBitmap(s);
                        }
                    });
        }
    }

}
