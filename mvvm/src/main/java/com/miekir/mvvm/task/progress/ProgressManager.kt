package com.miekir.mvvm.task.progress

import java.util.concurrent.ConcurrentHashMap

/**
 * 适合界面单个显示的进度，没有动态的文字等，如果是下载列表之类的，建议ROOM+FLOW监听
 */
object ProgressManager {
    private val observerMap = ConcurrentHashMap<String, ProgressObserver>()

    fun register(id: String, observer: ProgressObserver) {
        observerMap[id] = observer
    }

    fun unregister(id: String) {
        observerMap.remove(id)
    }

    fun updateProgress(id: String, progress: Int) {
        observerMap[id]?.onProgress(progress)
    }
}