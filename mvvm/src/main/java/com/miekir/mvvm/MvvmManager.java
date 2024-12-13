package com.miekir.mvvm;

import androidx.annotation.NonNull;

import com.miekir.mvvm.core.view.anim.AbstractAnimHandler;
import com.miekir.mvvm.exception.AbstractExceptionHandler;
import com.miekir.mvvm.exception.ExceptionManager;
import com.miekir.mvvm.log.ILogHandler;
import com.miekir.mvvm.log.L;
import com.miekir.mvvm.log.LogCallback;
import com.miekir.mvvm.widget.loading.DefaultTaskLoading;
import com.miekir.mvvm.widget.loading.TaskLoading;

/**
 * @author : 詹子聪
 * 任务初始化
 * @date : 2021-6-25 22:30
 */
public class MvvmManager {
    private AbstractAnimHandler mAnimHandler;

    private MvvmManager() {}

    private static class Factory {
        public static final MvvmManager INSTANCE = new MvvmManager();
    }

    public static MvvmManager getInstance() {
        return Factory.INSTANCE;
    }

    /**
     * @return 获取Activity切换动画
     */
    public AbstractAnimHandler getActivityAnimation() {
        return mAnimHandler;
    }

    /**
     * 加载对话框
     */
    private Class<? extends TaskLoading> mTaskLoadingClass = DefaultTaskLoading.class;

    public TaskLoading newTaskLoading() {
        if (mTaskLoadingClass == null) {
            return null;
        }
        try {
            return mTaskLoadingClass.newInstance();
        } catch (Exception e) {
            L.e(e.getMessage());
        }
        return null;
    }

    /**
     * 设置任务出错时的处理器
     *
     * @param handler 出错处理器
     * @return TaskManager
     */
    public MvvmManager exceptionHandler(@NonNull AbstractExceptionHandler handler) {
        ExceptionManager.getInstance().setExceptionHandler(handler);
        return this;
    }

    /**
     * 加载对话框的样式
     * @return TaskManager
     */
    public MvvmManager globalTaskLoading(Class<? extends TaskLoading> taskDialogClass) {
        mTaskLoadingClass = taskDialogClass;
        return this;
    }

    /**
     * 设置日志
     *
     * @param handler 日志处理器
     * @return TaskManager
     */
    public MvvmManager logHandler(@NonNull ILogHandler handler) {
        L.setLogHandler(handler);
        return this;
    }

    /**
     * 设置日志打印回调
     *
     * @param callback 日志打印回调
     * @return TaskManager
     */
    public MvvmManager logCallback(@NonNull LogCallback callback) {
        L.setLogCallback(callback);
        return this;
    }

    /**
     * 设置Activity切换动画
     * @param handler 动画实现
     * @return TaskManager
     */
    public MvvmManager activityAnimation(AbstractAnimHandler handler) {
        this.mAnimHandler = handler;
        return this;
    }
}
