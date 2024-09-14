package com.miekir.mvvm.core.view.result;

import android.content.Intent;

/**
 * @author : 詹子聪
 * startActivityForResult的结果回调
 * @date : 2021/4/16 21:57
 */
public abstract class ActivityResult {
    public void onResultOK(Intent backIntent) {
    }

    public void onResultFail(int code) {
    }

    public void onResult() {}
}