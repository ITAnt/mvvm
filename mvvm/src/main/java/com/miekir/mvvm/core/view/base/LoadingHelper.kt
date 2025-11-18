package com.miekir.mvvm.core.view.base

import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap

/**
 * 加载对话框管理单例
 * 使用Activity的唯一标识字符串作为key，LoadingManager作为value
 * 完全避免ViewModel，防止ViewModelLazy的内存泄漏问题
 */
object LoadingHelper {
    // 使用Activity唯一标识作为key，LoadingManager作为value
    private val loadingWrapMap = ConcurrentHashMap<String, LoadingWrap>()

    /**
     * 获取或创建LoadingManager
     */
    fun getOrCreateManager(activityKey: String): LoadingWrap {
        return loadingWrapMap.getOrPut(activityKey) { LoadingWrap() }
    }

    /**
     * 获取LoadingManager，如果不存在则返回null
     */
    fun getManager(activityKey: String): LoadingWrap? {
        return loadingWrapMap[activityKey]
    }

    /**
     * Activity销毁时清理
     */
    fun onActivityDestroy(activityKey: String, activity: LifecycleOwner, isFinishing: Boolean) {
        val manager = loadingWrapMap[activityKey]
        if (manager != null) {
            // 清理观察者
            manager.clearAllObservers(activity)
            
            // 如果Activity真正销毁，清理所有数据并移除映射
            if (isFinishing) {
                manager.clear()
                loadingWrapMap.remove(activityKey)
            }
        }
    }

    /**
     * 强制清理指定key的数据（用于兜底）
     */
    fun forceClean(activityKey: String) {
        val manager = loadingWrapMap.remove(activityKey)
        manager?.clear()
    }

    /**
     * 获取当前管理的ViewModel数量（用于调试）
     */
    fun getManagerCount(): Int {
        return loadingWrapMap.size
    }

    /**
     * 清理所有无效的引用（定期清理，防止内存泄漏）
     */
    fun cleanupInvalidReferences() {
        val iterator = loadingWrapMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val manager = entry.value
            // 如果LoadingManager没有活跃的对话框，可以考虑清理
            if (manager.mLoadingDialogList.isEmpty()) {
                manager.clear()
                iterator.remove()
            }
        }
    }

    /**
     * 获取所有管理器的状态信息（用于调试）
     */
    fun getDebugInfo(): String {
        val sb = StringBuilder()
        sb.append("LoadingHelper Debug Info:\n")
        sb.append("Total Activities: ${loadingWrapMap.size}\n")
        sb.append("----------------------------------------\n")
        
        loadingWrapMap.forEach { (activityKey, manager) ->
            sb.append("Activity Key: $activityKey\n")
            sb.append("Dialog Count: ${manager.mLoadingDialogList.size}\n")
            sb.append("Active Tasks: ${manager.mLoadingDialogList.count { it.taskJob?.isActive() == true }}\n")
            sb.append("----------------------------------------\n")
        }
        return sb.toString()
    }
}