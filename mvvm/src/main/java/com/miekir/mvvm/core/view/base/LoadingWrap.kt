package com.miekir.mvvm.core.view.base

import androidx.lifecycle.LifecycleOwner
import com.miekir.mvvm.widget.loading.DialogData
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 加载对话框管理器，不继承ViewModel，避免内存泄漏
 * 实际的业务逻辑都在这里实现
 */
class LoadingWrap {
    /**
     * 与Activity关联的加载框列表
     */
    val mLoadingDialogList = CopyOnWriteArrayList<DialogData>()
    
    /**
     * 当前关联的Activity的弱引用，用于清理观察者
     */
    private var currentActivityRef: WeakReference<LifecycleOwner>? = null

    /**
     * 设置当前Activity
     */
    fun attachActivity(activity: LifecycleOwner) {
        currentActivityRef = WeakReference(activity)
    }

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
     * 清理所有观察者，防止内存泄漏
     */
    fun clearAllObservers(lifecycleOwner: LifecycleOwner) {
        for (dialogData in mLoadingDialogList) {
            dialogData.completeLiveData.removeObservers(lifecycleOwner)
        }
    }

    /**
     * 清理所有资源（相当于原来的onCleared逻辑）
     */
    fun clear() {
        // 清理当前Activity的观察者
        currentActivityRef?.get()?.let { activity ->
            clearAllObservers(activity)
        }
        
        // 创建副本避免并发修改异常
        val dialogList = ArrayList(mLoadingDialogList)
        for (dialogData in dialogList) {
            dialogData.taskJob?.cancel()
            // 清理LiveData的值，避免观察者持有引用
            dialogData.completeLiveData.value = null
        }
        mLoadingDialogList.clear()
        currentActivityRef = null
    }
}