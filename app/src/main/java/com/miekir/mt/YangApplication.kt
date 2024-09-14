package com.miekir.mt

import android.app.Application
import com.miekir.mvvm.MvvmManager
import com.miekir.mvvm.task.net.RetrofitManager
import com.miekir.mvvm.widget.loading.DefaultTaskLoading
import com.readystatesoftware.chuck.ChuckInterceptor

/**
 * Copyright (C), 2019-2020, Miekir
 * @author Miekir
 * @date 2020/11/25 7:44
 * Description:
 */
class YangApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        MvvmManager.getInstance().globalTaskLoading(DefaultTaskLoading::class.java)
        RetrofitManager.getDefault().addInterceptors(ChuckInterceptor(this))
    }

//    /**
//     * 将要被回收的时候，应用自杀
//     */
//    override fun onTrimMemory(level: Int) {
//        super.onTrimMemory(level)
//        AppUtils.exitApp()
//    }
}