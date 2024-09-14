package com.miekir.mvvm.tools;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Toast;

import com.miekir.mvvm.context.GlobalContext;

/**
 * @author Miekir
 * @date 2020/7/5 0:08
 * Description: Toast工具
 */
public final class ToastTools {

    private ToastTools() {}

    private static class Factory {
        public static ToastTools INSTANCE = new ToastTools();
    }

    private static ToastTools getInstance() {
        return Factory.INSTANCE;
    }

    private static final long PERIOD_SHORT = 1500L;
    private static final long PERIOD_LONG = 2500L;

    private String lastShortString;
    private String lastLongString;

    private long mLastShortToastMillis;
    private long mLastLongToastMillis;

    private int mVerticalMargin = 0;

    /**
     * @param text 要弹出的语句
     */
    public static void showShort(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Context context = GlobalContext.getContext();
        if (context == null) {
            return;
        }

        initMargin(context);

        /*if (System.currentTimeMillis() - getInstance().mLastShortToastMillis > PERIOD_SHORT ||
                !TextUtils.equals(getInstance().lastShortString, text)) {
            getInstance().lastShortString = text;
            getInstance().mLastShortToastMillis = System.currentTimeMillis();
        }*/
        if (ThreadTools.isMainThread()) {
            showShortOnMain(context, text);
        } else {
            GlobalContext.runOnUiThread(() -> showShortOnMain(context, text));
        }
    }

    /**
     * @param text 要弹出的语句
     */
    public static void showLong(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Context context = GlobalContext.getContext();
        if (context == null) {
            return;
        }

        initMargin(context);

        /*if (System.currentTimeMillis() - getInstance().mLastLongToastMillis > PERIOD_LONG  ||
                !TextUtils.equals(getInstance().lastLongString, text)) {
            getInstance().lastLongString = text;
            getInstance().mLastLongToastMillis = System.currentTimeMillis();
        }*/
        if (ThreadTools.isMainThread()) {
            showLongOnMain(context, text);
        } else {
            GlobalContext.runOnUiThread(() -> showLongOnMain(context, text));
        }
    }

    /**
     * 短Toast，需要保证在主线程执行
     * @param context 上下文
     * @param text 内容
     */
    private static void showShortOnMain(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        //toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, getInstance().mVerticalMargin);
        toast.show();
    }

    /**
     * 长Toast，需要保证在主线程执行
     * @param context 上下文
     * @param text 内容
     */
    private static void showLongOnMain(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, getInstance().mVerticalMargin);
        toast.show();
    }

    private static synchronized void initMargin(Context context) {
        if (context == null) {
            return;
        }
        if (getInstance().mVerticalMargin == 0) {
            getInstance().mVerticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    72,
                    context.getResources().getDisplayMetrics());
        }
    }
}
