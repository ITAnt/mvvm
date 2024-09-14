package com.miekir.mvvm.context;

import android.content.Context;

/**
 * 上下文包裹者
 * @author : zzc
 * @date : 2021/12/14 17:54
 */
public class ContextWrapper {
    private Context context;

    public ContextWrapper(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
