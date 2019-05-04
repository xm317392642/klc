package com.xr.ychat.main.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.netease.nim.uikit.common.activity.SwipeBackUI;
import com.xr.ychat.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ImagePreviewActivity extends SwipeBackUI {
    private static final String EXTRA_IMAGE = "extra_image";
    private PhotoView photoView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        url = getIntent().getData().getQueryParameter(EXTRA_IMAGE);
        photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setOnPhotoTapListener(((view, x, y) -> finish()));
        if (TextUtils.isEmpty(url)) {
            photoView.setImageResource(R.drawable.nim_avatar_default);
        } else {
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
                            photoView.setImageBitmap(s);
                        }
                    });
        }
    }

}
