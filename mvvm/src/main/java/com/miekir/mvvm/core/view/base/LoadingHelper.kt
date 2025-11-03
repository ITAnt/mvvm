package com.miekir.mvvm.core.view.base

import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap

/**
 * 加载对话框管理单例
 * 使用LoadingViewModel作为key，LoadingManager作为value
 * 避免ViewModel直接持有数据导致的内存泄漏问题
 */
object LoadingHelper {
    private val loadingManagerMap = ConcurrentHashMap<LoadingViewModel, LoadingManager>()

    /**
     * 获取或创建LoadingManager
     */
    fun getOrCreateManager(viewModel: LoadingViewModel): LoadingManager {
        return loadingManagerMap.getOrPut(viewModel) { LoadingManager() }
    }

    /**
     * 获取LoadingManager，如果不存在则返回null
     */
    fun getManager(viewModel: LoadingViewModel): LoadingManager? {
        return loadingManagerMap[viewModel]
    }

    /**
     * Activity销毁时清理
     */
    fun onActivityDestroy(viewModel: LoadingViewModel, activity: LifecycleOwner, isFinishing: Boolean) {
        val manager = loadingManagerMap[viewModel]
        if (manager != null) {
            // 清理观察者
            manager.clearAllObservers(activity)
            
            // 如果Activity真正销毁，清理所有数据并移除映射
            if (isFinishing) {
                manager.clear()
                loadingManagerMap.remove(viewModel)
            }
        }
    }

    /**
     * 强制清理指定ViewModel的数据（用于兜底）
     */
    fun forceClean(viewModel: LoadingViewModel) {
        val manager = loadingManagerMap.remove(viewModel)
        manager?.clear()
    }

    /**
     * 获取当前管理的ViewModel数量（用于调试）
     */
    fun getManagerCount(): Int {
        return loadingManagerMap.size
    }

    /**
     * 清理所有无效的引用（定期清理，防止内存泄漏）
     */
    fun cleanupInvalidReferences() {
        val iterator = loadingManagerMap.entries.iterator()
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
        sb.append("Total ViewModels: ${loadingManagerMap.size}\n")
        loadingManagerMap.forEach { (viewModel, manager) ->
            sb.append("ViewModel: ${viewModel.hashCode()}, DialogCount: ${manager.mLoadingDialogList.size}\n")
        }
        return sb.toString()
    }
}