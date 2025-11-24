package com.miekir.mt.ui.fragment

import androidx.lifecycle.ViewModel
import com.miekir.mvvm.log.L
import com.miekir.mvvm.task.core.TaskJob
import com.miekir.mvvm.task.core.launchModelTask
import kotlinx.coroutines.delay

class TestViewModel: ViewModel() {
    fun go(): TaskJob {
        return launchModelTask(
            {
                delay(5000L)
            }, onSuccess = {
                L.e("fragment success")
            }
        )
    }
}