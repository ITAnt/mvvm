package com.miekir.mvvm.context

import android.content.Context
import androidx.startup.Initializer

/**
 * 放弃多个Provider初始化的方式，会拖慢APP启动，共用官方的启动Provider
 */
class ContextInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        GlobalContext.getInstance().initContext(context.applicationContext)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // 初始化操作不依赖于其他Initializer，返回空列表即可
        return emptyList()
    }
}
