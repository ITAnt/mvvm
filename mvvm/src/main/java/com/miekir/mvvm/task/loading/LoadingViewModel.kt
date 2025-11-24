package com.miekir.mvvm.task.loading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.CopyOnWriteArrayList

internal class LoadingViewModel: ViewModel() {
    val dialogLiveDataList = CopyOnWriteArrayList<MutableLiveData<DialogData>>()
}