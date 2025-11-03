package com.miekir.mvvm.core.view.base

import com.miekir.mvvm.MvvmManager
import com.miekir.mvvm.task.TaskJob
import com.miekir.mvvm.widget.loading.DialogData
import com.miekir.mvvm.widget.loading.LoadingType
import com.miekir.mvvm.widget.loading.TaskLoading

interface IView {
    /**
     * 加载框是否自动恢复重建
     */
    fun enableTaskLoadingRecreate(): Boolean {
        return true
    }
}

/**
 * @param taskLoading 自定义的弹窗，不传的话，使用系统默认的弹窗
 */
fun IView.withLoadingDialog(
    loadingType: LoadingType = LoadingType.VISIBLE,
    message: String = "",
    taskLoading: TaskLoading? = null,
    taskGenerator: () -> TaskJob
) {
    // 开启任务
    val taskJob = taskGenerator()
    if (!taskJob.firstLaunch) {
        return
    }

    // 加载框的上下文
    var basicActivity: BasicActivity? = null
    if (this is BasicActivity) {
        basicActivity = this
    } else if (this is BasicFragment) {
        basicActivity = requireActivity() as? BasicActivity
    }

    // 不需要显示加载框
    if (basicActivity == null || loadingType == LoadingType.INVISIBLE) {
        return
    }

    // 创建弹窗
    var realLoading: TaskLoading? = taskLoading
    if (realLoading == null) {
        realLoading = MvvmManager.getInstance().newTaskLoading()
    }
    if (realLoading == null) {
        return
    }

    // 弹出加载框
    val dialogData = DialogData(message, loadingType, taskJob)
    taskJob.setupDialogData(dialogData)
    LoadingHelper.getOrCreateManager(basicActivity.activityKey).addLoadingDialogData(dialogData)
    basicActivity.showLoading(realLoading, dialogData)
}