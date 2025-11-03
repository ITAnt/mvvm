package com.miekir.mvvm.core.view.base

import androidx.lifecycle.ViewModel
import com.miekir.mvvm.widget.loading.DialogData
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
    val mLoadingDialogList = CopyOnWriteArrayList<DialogData>()

    /**
     * 新增任务弹窗
     */
    fun addLoadingDialogData(data: DialogData) {
        if (mLoadingDialogList.contains(data)) {
            return
        }
        mLoadingDialogList.add(data)
    }

    /**
     * 移除任务弹窗
     */
    fun removeLoadingDialogData(dialog: DialogData) {
        mLoadingDialogList.remove(dialog)
    }



    /**
     * 界面和任务都销毁
     */
    override fun onCleared() {
        super.onCleared()
        for (dialogData in mLoadingDialogList) {
            dialogData.taskJob?.cancel()
            // 清理LiveData的值，避免观察者持有引用
            dialogData.completeLiveData.value = null
        }
        mLoadingDialogList.clear()
    }
}