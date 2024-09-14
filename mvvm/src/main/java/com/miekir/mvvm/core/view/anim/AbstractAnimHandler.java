package com.miekir.mvvm.core.view.anim;

import android.app.Activity;

/**
 * 动画执行者
 */
public abstract class AbstractAnimHandler {
    // 如果20毫秒内先启动了一个Activity并finish了一个Activity，此时不要做finish动画了，否则会出现动画错乱
    protected static final int PERIOD_START_FINISH = 20;
    protected long mStartAnimTime;

    /**
     * 进入界面执行的动画
     */
    public abstract void enterAnimation(Activity activity);
    /**
     * 退出界面执行的动画
     */
    public abstract void exitAnimation(Activity activity);

    /**
     * 设置进入动画的时刻
     * @param time 时间戳，单位毫秒
     */
    public void setStartAnimTime(long time) {
        mStartAnimTime = time;
    }

    /**
     * 获取进入动画的时刻
     * @param time 时间戳，单位毫秒
     */
    public long getStartAnimTime(long time) {
        return mStartAnimTime;
    }
}
