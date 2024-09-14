//package com.miekir.mvp.task
//
//import android.content.Context
//import android.view.View
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.viewModelScope
//import com.miekir.mvp.MvpManager
//import com.miekir.mvp.common.context.GlobalContext
//import com.miekir.mvp.common.exception.ExceptionManager
//import com.miekir.mvp.common.tools.ToastTools
//import com.miekir.mvp.common.widget.loading.DefaultTaskDialog
//import com.miekir.mvp.common.widget.loading.LoadingType
//import com.miekir.mvp.common.widget.loading.TaskDialog
//import com.miekir.mvp.presenter.BasePresenter
//import com.miekir.mvp.task.net.IResponse
//import com.miekir.mvp.view.base.IView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.concurrent.CancellationException
//
///*---------------------------------以下为协程实现耗时任务-------------------------------------*/
///**
// * 协程方式实现耗时任务，有无加载框，有无回调都可以自定义
// * 注意：此回调如使用view::onLoginResult方法传递，会把对Activity的弱引用转为强引用，
// * 需要配合清单文件的configChanges使用，否则任务执行过程中，屏幕旋转后会发生内存泄露
// * @param showErrorToast true 发生错误时提示错误
// * @param loadingMessage 任务加载时的提示
// */
//inline fun <V : IView, reified T> BasePresenter<V>.launchModelTask(
//    noinline taskBody: suspend () -> T,
//    loadingType: LoadingType = LoadingType.INVISIBLE,
//    taskDialog: TaskDialog? = null,
//    loadingMessage: String = "",
//    showErrorToast: Boolean = false,
//    dialogContext: Context? = null,
//    noinline onSuccess: ((result: T) -> Unit)? = null,
//    noinline onFailure: ((code: Int, message: String) -> Unit)? = null,
//    noinline onCancel: (() -> Unit)? = null,
//    noinline onResults: (() -> Unit)? = null,): Job {
//    // 按需弹出加载框
//    var mTaskDialog: TaskDialog? = taskDialog
//    val presenter = this
//
//    val job = viewModelScope.launch(Dispatchers.Main) {
//        if (loadingType != LoadingType.INVISIBLE && view !== null) {
//            var context:Context? = null
//            context = dialogContext
//            if (context == null) {
//                when (view) {
//                    is Context -> {
//                        context = view as Context
//                    }
//                    is Fragment -> {
//                        context = (view as Fragment).context
//                    }
//                    is View -> {
//                        context = (view as View).context
//                    }
//                }
//            }
//
//            if (context == null) {
//                throw IllegalArgumentException("Task dialog context is null")
//            }
//
//            if (loadingType != LoadingType.INVISIBLE) {
//                if (mTaskDialog == null) {
//                    mTaskDialog = MvpManager.getInstance().taskDialog
//                }
//                if (mTaskDialog == null) {
//                    mTaskDialog = DefaultTaskDialog()
//                }
//                mTaskDialog?.setupWithJob(context, presenter, null, loadingType, loadingMessage)
//                mTaskDialog?.show()
//                addLoadingDialog(mTaskDialog)
//            }
//        }
//
//        var exception: Exception? = null
//        var returnType: T? = null
//        // 在“子线程”执行耗时任务。launch里面try catch可以捕获取消异常，withContext要在外面
//        try {
//            withContext(Dispatchers.IO) {
//                returnType = taskBody.invoke()
//            }
//        } catch (e: Exception) {
//            exception = e
//        }
//
//        // 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
//        if (exception == null && returnType != null) {
//            if (returnType is IResponse) {
//                try {
//                    (returnType as IResponse).run {
//                        valid()
//                        onSuccess?.invoke(returnType!!)
//                    }
//                } catch (e: Exception) {
//                    exception = e
//                }
//            } else {
//                onSuccess?.invoke(returnType!!)
//            }
//        }
//
//        // 耗时任务完成后，回到主线程
//        removeLoadingDialog(mTaskDialog)
//        mTaskDialog?.dismiss()
//        mTaskDialog = null
//
//        if (exception != null) {
//            // 获取具体错误类型
//            if (exception is CancellationException) {
//                // 主动取消
//                onCancel?.invoke()
//            } else {
//                // 任务异常
//                val eResult = ExceptionManager.getInstance().exceptionHandler.handleException(exception)
//                if (showErrorToast) {
//                    ToastTools.showLong(eResult.errorMessage)
//                }
//                onFailure?.invoke(eResult.code, eResult.errorMessage)
//            }
//        }
//        onResults?.invoke()
//    }
//    GlobalContext.runOnUiThread {
//        mTaskDialog?.setJob(job)
//    }
//    return job
//}
//
///*---------------------------------以下为RX实现耗时任务-------------------------------------*/
/////**
//// * 获取耗时任务
//// * @param taskBody 耗时任务方法体
//// */
////fun <T> launchKtRxTask(taskBody: () -> T): Observable<T> {
////    return Observable.create(object : ObservableOnSubscribe<T> {
////        override fun subscribe(emitter: ObservableEmitter<T>) {
////            emitter.onNext(taskBody.invoke())
////            emitter.onComplete()
////        }
////    })
////}
//
/////**
//// * 异步执行，获取结果
//// * 网络请求，使用NetObserver.Builder创建
//// * 普通请求，使用[CommonObserver.Builder]创建
//// */
////fun <T> Observable<T>.startForResult(observer: Observer<T>) {
////    this.subscribeOn(Schedulers.io())
////        .observeOn(AndroidSchedulers.mainThread())
////        .subscribe(observer)
////}
////
/////**
//// * 异步执行，忽略结果，默认没有加载框
//// */
////fun <R, V : IView> Observable<R>.startIgnoreResult(
////    presenter: BasePresenter<V>,
////    loadingType: LoadingType = LoadingType.INVISIBLE,
////    message: String? = null,
////    context: Context? = null
////) {
////    val observer = CommonObserver.Builder.with<Any?>(presenter)
////        .loadType(loadingType)
////        .context(context)
////        .message(message)
////        .create()
////
////    this.subscribeOn(Schedulers.io())
////        .observeOn(AndroidSchedulers.mainThread())
////        .subscribe(observer)
////}



//package com.miekir.mvp.task
//
//import com.miekir.mvp.common.context.GlobalContext
//import com.miekir.mvp.common.exception.ExceptionManager
//import com.miekir.mvp.task.net.IResponse
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.concurrent.CancellationException
//
///**
// * 运行任务，任务自动切换主线程、子线程，生命周期与scope一致，默认是全局scope
// */
//inline fun <T> launchGlobalTask(
//    noinline taskBody: suspend () -> T,
//    noinline onSuccess: ((result: T) -> Unit)? = null,
//    noinline onFailure: ((code: Int, message: String) -> Unit)? = null,
//    noinline onCancel: (() -> Unit)? = null,
//    noinline onResults: (() -> Unit)? = null,
//    scope: CoroutineScope = GlobalScope
//): Job {
//    return scope.launch(Dispatchers.IO) {
//        try {
//            val t = taskBody()
//            withContext(Dispatchers.Main) {
//                // 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
//                if (t is IResponse) {
//                    try {
//                        (t as IResponse).run {
//                            valid()
//                            onSuccess?.invoke(t)
//                        }
//                    } catch (e: Exception) {
//                        val eResult = ExceptionManager.getInstance().exceptionHandler.handleException(e)
//                        onFailure?.invoke(eResult.code, eResult.errorMessage)
//                    }
//                } else {
//                    onSuccess?.invoke(t)
//                }
//
//                onResults?.invoke()
//            }
//        } catch (e: Exception) {
//            // 协程出现异常，不可继续切协程，只能使用线程调度
//            GlobalContext.runOnUiThread {
//                if (e is CancellationException) {
//                    onCancel?.invoke()
//                } else {
//                    val eResult = ExceptionManager.getInstance().exceptionHandler.handleException(e)
//                    onFailure?.invoke(eResult.code, eResult.errorMessage)
//                }
//                onResults?.invoke()
//            }
//        }
//    }
//}