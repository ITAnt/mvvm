package com.miekir.mvvm.task.core

import androidx.lifecycle.MutableLiveData
import com.miekir.mvvm.task.loading.DialogData
import com.miekir.mvvm.task.loading.LoadingType
import kotlinx.coroutines.Job

class TaskJob {
    var job: Job? = null
    /**
     * 是否首次发起该类型的任务，默认为true，如果该tag任务在进行中，再次发起，则为false
     */
    internal var firstLaunch = true

    private var dialogLiveData: MutableLiveData<DialogData>? = null

    fun cancel() {
        if (job?.isActive == true) {
            job?.cancel()
        }
        job = null
        onResult()
    }

    fun onResult() {
        dialogLiveData?.let { liveData ->
            liveData.value?.let {
                liveData.postValue(it.copy(loadingType = LoadingType.INVISIBLE,))
            }
        }
        // 清理DialogData引用，防止内存泄漏
        dialogLiveData = null
    }

    fun isActive(): Boolean {
        return job?.isActive ?: false
    }

    fun setupJob(job: Job?) {
        this.job = job
    }

    fun setupDialogData(liveData: MutableLiveData<DialogData>) {
        if (isActive()) {
            dialogLiveData = liveData
        } else {
            liveData.value?.let {
                liveData.postValue(it.copy(loadingType = LoadingType.INVISIBLE,))
            }
        }
    }
}