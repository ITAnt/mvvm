package com.miekir.mt.ui.main

import androidx.lifecycle.ViewModel
import com.miekir.mvvm.log.L
import com.miekir.mvvm.task.TaskJob
import com.miekir.mvvm.task.launchModelTask
import kotlinx.coroutines.delay

class MainViewModel : ViewModel() {
    fun testFast(): TaskJob {
        return launchModelTask(
            {
                L.e("dddd")
                delay(15000)
            }, onFailure = { code, message, exception ->
                L.e("test error: $code, $message, ${exception.message}")
            }, onSuccess = {
                L.e("test success")
            }
        )
    }
}