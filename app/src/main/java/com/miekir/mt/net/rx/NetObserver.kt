//package com.miekir.mt.net.rx
//
//import android.content.Context
//import com.miekir.mt.net.BaseResponse
//import com.miekir.mvp.common.exception.ExceptionManager
//import com.miekir.mvp.common.log.L
//import com.miekir.mvp.common.widget.loading.LoadingType
//import com.miekir.mvp.common.widget.loading.TaskDialog
//import com.miekir.mvp.presenter.BasePresenter
//import com.itant.mvp.observer.BaseObserver
//import com.itant.mvp.observer.listener.CancelListener
//
///**
// * 返回结果的实体最外层是[BaseResponse]
// * 这个任务默认有可取消的加载框，但是没有文字
// */
//class NetObserver<T> @JvmOverloads internal constructor(
//    presenter: BasePresenter<*>?,
//    loadingType: LoadingType? = LoadingType.VISIBLE,
//    taskDialog: TaskDialog? = null,
//    message: String? = "",
//    private val onceTask: Boolean = true,
//    private val onCancel: CancelListener? = null,
//    dialogContext: Context? = null
//) : BaseObserver<BaseResponse<T>>(presenter, loadingType, taskDialog, message, onceTask, onCancel, dialogContext) {
//    lateinit var mBuilder: Builder<T>
//
//    override fun onNext(result: BaseResponse<T>) {
//        super.onNext(result)
//        try {
//            if (result.code == ExceptionManager.getInstance().successCode) {
//                onSuccess(result.resultObj)
//                return
//            }
//            L.e(result.message)
//            //if (result.code == Code.TOKEN_EXPIRE) {
//                // 重新登录
//            //}
//            mBuilder.bOnError?.invoke(result.code, result.message)
//            mBuilder.bOnResult?.invoke(false, null, result.code, result.message)
//            if (onceTask) {
//                mBuilder.bOnComplete?.invoke()
//            }
//        } catch (e: Exception) {
//            L.e(e.toString())
//            mBuilder.bOnError?.invoke(ExceptionManager.getInstance().failedCode, e.message)
//            mBuilder.bOnResult?.invoke(false, null, ExceptionManager.getInstance().failedCode, e.message)
//            if (onceTask) {
//                mBuilder.bOnComplete?.invoke()
//            }
//        }
//    }
//
//    /**
//     * 开始
//     */
//    override fun onStart() {
//        super.onStart()
//        mBuilder.bOnStart?.invoke()
//    }
//
//    /**
//     * 成功
//     */
//    private fun onSuccess(result: T?) {
//        mBuilder.bOnSuccess?.invoke(result)
//        mBuilder.bOnResult?.invoke(true, result, ExceptionManager.getInstance().successCode, "")
//        if (onceTask) {
//            mBuilder.bOnComplete?.invoke()
//        }
//    }
//
//    override fun onError(e: Throwable) {
//        super.onError(e)
//        val exception = ExceptionManager.getInstance().exceptionHandler.handleException(e)
//
//        mBuilder.bOnError?.invoke(exception.code, exception.message)
//        mBuilder.bOnResult?.invoke(false, null, exception.code, exception.message)
//        mBuilder.bOnComplete?.invoke()
//    }
//
//    override fun onComplete() {
//        super.onComplete()
//        if (!onceTask) {
//            mBuilder.bOnComplete?.invoke()
//        }
//    }
//
//    class Builder<R> {
//        private lateinit var bPresenter: BasePresenter<*>
//
//        // 需要处理返回结果的任务，默认是有加载框的
//        private var bLoadingType: LoadingType? = LoadingType.VISIBLE
//        private var bTaskDialog: TaskDialog? = null
//        private var bMessage: String? = null
//
//        // 是否是一次性任务，是的话，onNext相当于onComplete，onNext执行后就马上去除加载框（存在的话），
//        // 否则onComplete时才去除加载框（存在的话）
//        private var bIsOnceTack: Boolean = true
//        private var bOnCancel: CancelListener? = null
//
//        internal var bOnStart: (() -> Unit)? = null
//
//        // 成功的回调函数
//        internal var bOnSuccess: ((result: R?) -> Unit)? = null
//
//        // 失败的回调函数
//        internal var bOnError: ((code: Int, message: String?) -> Unit)? = null
//
//        // 任务完成后的回调函数，成功或失败都会回调
//        internal var bOnComplete: (() -> Unit)? = null
//
//        internal var bContext: Context? = null
//
//        // 任务完成后的回调函数，成功或失败都会回调，并附带结果
//        internal var bOnResult: ((success: Boolean, result: R?, code: Int, message: String?) -> Unit)? = null
//
//        constructor(presenter: BasePresenter<*>) {
//            bPresenter = presenter
//        }
//
//        companion object {
//            fun <R> with(presenter: BasePresenter<*>): Builder<R> {
//                return Builder<R>(presenter)
//            }
//        }
//
//        /**
//         * 加载框类型
//         */
//        fun loadType(type: LoadingType?): Builder<R> {
//            bLoadingType = type
//            return this
//        }
//
//        /**
//         * 加载框布局
//         */
//        fun taskDialog(taskDialog: TaskDialog): Builder<R> {
//            bTaskDialog = taskDialog
//            return this
//        }
//
//        /**
//         * 加载中的消息
//         */
//        fun message(message: String?): Builder<R> {
//            bMessage = message
//            return this
//        }
//
//        /**
//         * 是否一次性任务
//         */
//        fun onceTask(once: Boolean): Builder<R> {
//            bIsOnceTack = once
//            return this
//        }
//
//        /**
//         * 任务完开始前的回调函数，哪个线程调就是哪个线程
//         *
//         */
//        fun onStart(start: () -> Unit): Builder<R> {
//            bOnStart = start
//            return this
//        }
//
//        /**
//         * 成功的回调函数
//         */
//        fun onSuccess(success: (result: R?) -> Unit): Builder<R> {
//            bOnSuccess = success
//            return this
//        }
//
//        /**
//         * 取消时的回调
//         */
//        fun onCancel(cancel: CancelListener): Builder<R> {
//            bOnCancel = cancel
//            return this
//        }
//
//        /**
//         * 失败的回调函数
//         */
//        fun onError(error: (code: Int, message: String?) -> Unit): Builder<R> {
//            bOnError = error
//            return this
//        }
//
//        /**
//         * 任务完成后的回调函数，成功或失败都会回调
//         */
//        fun onComplete(complete: () -> Unit): Builder<R> {
//            bOnComplete = complete
//            return this
//        }
//
//        /**
//         * 任务完成后的回调函数，成功或失败都会回调，并附带结果
//         */
//        fun onResult(result: (success: Boolean, result: R?, code: Int, message: String?) -> Unit): Builder<R> {
//            bOnResult = result
//            return this
//        }
//
//        fun context(context: Context? = null): Builder<R> {
//            bContext = context
//            return this
//        }
//
//        fun create(): NetObserver<R> {
//            val observer = NetObserver<R>(bPresenter, bLoadingType, bTaskDialog, bMessage, bIsOnceTack, bOnCancel, bContext)
//            observer.mBuilder = this
//            return observer
//        }
//    }
//}
//
