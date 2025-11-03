package com.miekir.mvvm.core.view.base

import androidx.lifecycle.ViewModel

/**
 * 空的LoadingViewModel，仅用作LoadingHelper中ConcurrentHashMap的key
 * 不持有任何变量和方法，避免内存泄漏
 * 实际的业务逻辑都在LoadingManager中实现
 * 
 * 设计思路：
 * - LoadingViewModel：仅作为key，利用ViewModel在配置变更时的生命周期特性
 * - LoadingManager：实际的业务逻辑实现
 * - LoadingHelper：单例管理两者的映射关系
 */
class LoadingViewModel: ViewModel() {
    
    /**
     * 某些定制ROM可能不会回调onCleared，这里提供兜底清理
     * 正常情况下，清理逻辑在BasicActivity的onDestroy中通过LoadingHelper处理
     */
    override fun onCleared() {
        super.onCleared()
        // 兜底清理，防止某些ROM不回调onCleared导致的内存泄漏
        LoadingHelper.forceClean(this)
    }
}