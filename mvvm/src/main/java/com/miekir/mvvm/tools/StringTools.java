package com.miekir.mvvm.tools;

import android.content.Context;

import androidx.annotation.StringRes;

import com.miekir.mvvm.context.GlobalContext;

public final class StringTools {
    private StringTools() {}

    /**
     * 根据资源Id获取对应字符串
     * 注意：resourceId必须是系统存在的
     */
    public static String getString(@StringRes int resourceId) {
        Context context = GlobalContext.getContext();
        if (context == null) {
            return "";
        }

        return context.getResources().getString(resourceId);
    }
}
