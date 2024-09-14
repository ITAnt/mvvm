package com.miekir.mvvm.core.view.base

import androidx.lifecycle.ViewModel
import com.miekir.mvvm.widget.loading.TaskLoading
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 没有在清单文件配置onConfigChanges="orientation|screenSize"时，加载框在Activity销毁重建的时候销毁与恢复，
 * 如：屏幕旋转等
 *
 * 原则来说，ViewModel不应该持有Dialog的实例，旧Activity销毁的时候，必须让它上面的Dialog全部销毁，否则会产生内存泄漏；
 * 但是在Activity销毁重建，任务还在继续的情况下，确实还要显示Dialog：
 * 方案1：在清单文件配置onConfigChanges="orientation|screenSize"
 * 方案2；Activity正常销毁重建，再重建的时候，但要在onDestroy的时候调用BaseViewModel的detachView，
 * 在onCreate的时候重新调用attachView
 */
class LoadingViewModel: ViewModel() {
    /**
     * 与ViewModel关联的加载框列表
     */
    private val mLoadingDialogList = CopyOnWriteArrayList<TaskLoading>()

    /**
     * 新增任务弹窗
     */
    fun addLoadingDialog(dialog: TaskLoading?) {
        if (dialog == null || mLoadingDialogList.contains(dialog)) {
            return
        }
        mLoadingDialogList.add(dialog)
    }

    /**
     * 移除任务弹窗
     */
    fun removeLoadingDialog(dialog: TaskLoading?) {
        if (dialog == null) {
            return
        }
        mLoadingDialogList.remove(dialog)
    }

    /**
     * Activity生命周期重建，如旋转屏幕等，需要重建对话框，防止崩溃
     */
    fun onViewAttach(view: BasicActivity) {
        if (!view.enableTaskLoadingRecreate() || mLoadingDialogList.isEmpty()) {
            return
        }

        // 恢复任务弹窗
        for (dialog in mLoadingDialogList) {
            dialog.recreate(view)
        }
    }

    /**
     * 仅销毁界面
     */
    fun onViewDetach() {
        for (dialog in mLoadingDialogList) {
            dialog.dismiss()
        }
    }

    /**
     * 界面和任务都销毁
     */
    override fun onCleared() {
        super.onCleared()
        for (dialog in mLoadingDialogList) {
            dialog.cancelTaskAndDismiss()
        }
        mLoadingDialogList.clear()
    }
}