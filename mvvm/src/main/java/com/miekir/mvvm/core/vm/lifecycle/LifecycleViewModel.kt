package com.miekir.mvvm.core.vm.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.miekir.mvvm.core.vm.common.CommonViewModel

/**
 * 注意：如果在Dialog中使用，则强烈建议遵循Dialog的使用规则
 * ①在调用Dialog的show方法之后，再调用presenter开始执行耗时任务；
 * ②需要在Dialog的onDismiss方法里调用[CommonViewModel.canCelTask]方法，避免dismiss之后仍存在后台任务；
 * ③如果需要执行的是和Dialog生命周期不一致的任务，建议直接在单例中使用launchGlobalTask，配合LiveData更新数据。
 */
open class LifecycleViewModel<V>(view: V, lifecycle: Lifecycle): CommonViewModel<V>(view) {
    init {
        lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    destroyViewModel()
                }
            }
        })
        taskScope = lifecycle.coroutineScope
    }
}

/**
 * View初始化它对应的Presenter
 */
/*
inline fun <reified V, reified P : LifecycleViewModel<V>> V.viewModel(lifecycle: Lifecycle) = lazy {
    val presenter: P = P::class.java
        .getDeclaredConstructor(V::class.java, Lifecycle::class.java)
        .newInstance(this, lifecycle)
    presenter
}
*/

/*
class MyDialog(lifecycle: Lifecycle) {
    private val presenter1: MyDialogPresenter by lazy { MyDialogPresenter(this, lifecycle) }
    private val presenter2: MyDialogPresenter by presenter(lifecycle)

    init {
        presenter2.go()
    }

    fun onOk() {
        L.e("view ok")
    }
}

class MyDialogPresenter(view: MyDialog, lifecycle: Lifecycle): LifecyclePresenter<MyDialog>(view, lifecycle) {
    fun go() {
        launchTask(
            {
                delay(10_000L)
            }, onSuccess = {
                L.e("on success")
                view?.onOk()
            }, onCancel = {
                L.e("on cancel")
            }
        )
    }
}
 */