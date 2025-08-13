package com.miekir.mvvm.widget.loading

import androidx.lifecycle.MutableLiveData
import com.miekir.mvvm.task.TaskJob

data class DialogData(
    var title: String? = null,
    var loadingType: LoadingType = LoadingType.VISIBLE,
    var taskJob: TaskJob? = null,
    val completeLiveData: MutableLiveData<Boolean?> = MutableLiveData(false),
)
