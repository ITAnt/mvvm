package com.miekir.mvvm.tools;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

/**
 * @author : zzc
 * @date : 2022/3/24 15:51
 */
public final class KeyboardTools {
    private KeyboardTools(){}

    /**
     * 获取焦点
     *
     * @param activity
     * @param view
     */
    public static void requestInputFocus(@NonNull Activity activity, @NonNull final View view) {
        // 进入页面弹出软键盘
        view.requestFocus();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        // 或使用以下方法（view.post不行）
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * 隐藏输入法
     * // 点击空白处隐藏输入法（要配合Activity根布局增加以下标志使用
     * // android:focusable="true"
     * // android:focusableInTouchMode="true"）
     */
    public static void hideInputMethod(@NonNull Activity activity, View rootView) {
        final InputMethodManager imm = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        final View currentFocusView = activity.getCurrentFocus();
        if (currentFocusView != null) {
            imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        if (rootView != null) {
            rootView.requestFocus();
        }
    }
}
