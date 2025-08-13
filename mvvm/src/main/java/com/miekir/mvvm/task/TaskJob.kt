package com.miekir.mvvm.task

import com.miekir.mvvm.widget.loading.DialogData
import com.miekir.mvvm.widget.loading.TaskLoading
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class TaskJob {
    private var context: CoroutineContext? = null
    var job: Job? = null

    /**
     * 是否首次发起该类型的任务，默认为true，如果该tag任务在进行中，再次发起，则为false
     */
    internal var firstLaunch = true

    private var mDialogData: DialogData? = null

    fun setup(context: CoroutineContext, job: Job?) {
        this.context = context
        this.job = job
    }

    fun cancel() {
        if (job?.isActive == true) {
            job?.cancel()
        }
        context = null
        job = null
        onResult()
    }

    fun onResult() {
        mDialogData?.completeLiveData?.postValue(true)
    }

    fun isActive(): Boolean {
        return job?.isActive ?: false
    }

    fun setupDialogData(dialogData: DialogData) {
        mDialogData = dialogData
    }
}