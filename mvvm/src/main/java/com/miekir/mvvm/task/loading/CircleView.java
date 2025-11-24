package com.miekir.mvvm.task.loading;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;

/**
 * 自定义加载动画
 *
 * @author zc
 */
public class CircleView extends View {
    private float mWidth = 0f;
    private float mPadding = 0f;
    private float startAngle = 0f;
    private Paint mPaint;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() > getHeight()) {
            mWidth = getMeasuredHeight();
        } else {
            mWidth = getMeasuredWidth();
        }
        mPadding = 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.argb(100, 255, 255, 255));
        canvas.drawCircle(mWidth / 2, mWidth / 2, mWidth / 2 - mPadding, mPaint);
        mPaint.setColor(Color.WHITE);
        RectF rectF = new RectF(mPadding, mPadding, mWidth - mPadding, mWidth - mPadding);
        //第四个参数是否显示半径
        canvas.drawArc(rectF, startAngle, 100, false, mPaint);

    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(8);
    }

    public void startAnim() {
        stopAnim();
        startViewAnim(0f, 1f, 1000);
    }

    public void stopAnim() {
        if (valueAnimator != null) {
            clearAnimation();
            valueAnimator.setRepeatCount(1);
            valueAnimator.cancel();
            valueAnimator.end();
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.removeAllListeners();
        }
        valueAnimator = null;
    }

    private ValueAnimator valueAnimator;

    private ValueAnimator startViewAnim(float startF, final float endF, long time) {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            return valueAnimator;
        }

        valueAnimator = ValueAnimator.ofFloat(startF, endF);
        valueAnimator.setDuration(time);
        valueAnimator.setInterpolator(new LinearInterpolator());
        //无限循环
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        if (mUpdateListener == null) {
            mUpdateListener = new UpdateListener(this);
        }
        valueAnimator.addUpdateListener(mUpdateListener);
        valueAnimator.start();
        return valueAnimator;
    }

    private UpdateListener mUpdateListener;

    private static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private WeakReference<CircleView> mView;

        public UpdateListener(CircleView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mView == null) {
                return;
            }
            CircleView view = mView.get();
            if (view == null) {
                return;
            }

            float value = (float) view.valueAnimator.getAnimatedValue();
            view.startAngle = 360 * value;
            view.invalidate();
        }
    }
}

