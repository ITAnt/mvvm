package com.miekir.mvvm.task.loading;

public enum LoadingType {
    /**
     * 有可以手动取消的加载框
     */
    VISIBLE,

    /**
     * 有可以手动取消的加载框，但加载框生命周期不和任务绑定
     */
    VISIBLE_ALONE,

    /**
     * 有不能手动取消的加载框，需要任务终了加载框才能消失
     */
    STICKY,

    /**
     * 没有加载框，默默在后台运行
     */
    INVISIBLE,
}
