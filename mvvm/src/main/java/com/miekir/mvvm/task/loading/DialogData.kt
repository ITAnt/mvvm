package com.miekir.mvvm.task.loading

import com.miekir.mvvm.task.core.TaskJob


data class DialogData(
    var title: String? = null,
    var loadingType: LoadingType = LoadingType.VISIBLE,
    var taskJob: TaskJob? = null,
    val loadingClazz: Class<out TaskLoading>?
)
