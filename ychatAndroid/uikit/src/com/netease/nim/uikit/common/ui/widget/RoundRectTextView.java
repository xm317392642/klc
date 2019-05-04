package com.netease.nim.uikit.common.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

public class RoundRectTextView extends AppCompatTextView {
    Paint paint;
    private int normalBgColor,pressBgColor;
    private float radius;
    RectF rectF;
    public RoundRectTextView(Context context) {
        super(context);
        init();
    }

    public RoundRectTextView(Context context, AttributeSet attrs) {
        //super(context, attrs);
        this(context, attrs, 0);
        init();

    }

    public RoundRectTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getResources().obtainAttributes(attrs, R.styleable.RoundRectTextView);
        normalBgColor = typedArray.getColor(R.styleable.RoundRectTextView_bgColor, Color.BLUE);
        pressBgColor = typedArray.getColor(R.styleable.RoundRectTextView_pressBgColor ,0);
        radius = typedArray.getDimension(R.styleable.RoundRectTextView_rrtRadius, 0);
        typedArray.recycle();
        init();

    }

    public void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(normalBgColor);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawRoundRect(rectF,ScreenUtil.dip2px(radius),ScreenUtil.dip2px(radius),paint);
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        rectF=new RectF(0,0,getWidth(),getHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://0
                Log.e("TAG", "LinearLayout onTouchEvent 按住");
                if(pressBgColor!=0){
                    paint.setColor(pressBgColor);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP://1
                if(pressBgColor!=0){
                    paint.setColor(normalBgColor);
                    invalidate();
                }
                Log.e("TAG", "LinearLayout onTouchEvent 抬起");
                break;
            case MotionEvent.ACTION_MOVE://2
                Log.e("TAG", "LinearLayout onTouchEvent 移动");
                break;
        }
        return super.onTouchEvent(event);
    }

}
