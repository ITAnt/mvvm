package com.miekir.mvvm.widget.loading

import com.miekir.mvvm.core.livedata.SingleLiveEvent
import com.miekir.mvvm.task.TaskJob

data class DialogData(
    var title: String? = null,
    var loadingType: LoadingType = LoadingType.VISIBLE,
    var taskJob: TaskJob? = null,
    val completeLiveData: SingleLiveEvent<Boolean?> = SingleLiveEvent<Boolean?>().apply { value = false },
)
