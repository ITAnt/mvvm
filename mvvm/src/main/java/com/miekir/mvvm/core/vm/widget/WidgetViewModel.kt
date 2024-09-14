package com.miekir.mvvm.core.vm.widget

import android.view.View
import android.view.View.OnAttachStateChangeListener
import com.miekir.mvvm.core.vm.common.CommonViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

/**
 * 针对自定义View控件的Presenter
 * private val presenter2: MyViewPresenter by presenter()
 */
open class WidgetViewModel<V: View>(view: V): CommonViewModel<V>(view) {
    private val coroutineScope: CloseableCoroutineScope by lazy {
        CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    init {
        view.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                // 要在onViewAttachedToWindow初始化，防止onResume之前就finish了，
                // 导致onViewDetachedFromWindow不执行，产生内存泄漏
                // （onViewAttachedToWindow和onViewDetachedFromWindow必须成对使用）
                taskScope = coroutineScope
            }

            override fun onViewDetachedFromWindow(v: View?) {
                view.removeOnAttachStateChangeListener(this)
                taskScope?.cancel()
            }
        })
    }
}

class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}

/**
 * View初始化它对应的Presenter
 */
inline fun <reified V : View, reified P : WidgetViewModel<V>> View.viewModel() = lazy {
    val presenter: P = P::class.java
        .getDeclaredConstructor(V::class.java)
        .newInstance(this)
    presenter
}

///**
// * View初始化它对应的Presenter
// */
//inline fun <reified V : View, reified P : ViewPresenter<V>> View.presenter(noinline factory: (() -> P)? = null) = lazy {
//    if (factory == null) {
//        val presenter: P = P::class.java.getDeclaredConstructor().newInstance()
//        //presenter.initView(this as V)
//        presenter
//    } else {
//        val presenter: P = factory()
//        //presenter.initView(this as V)
//        presenter
//    }
//}
