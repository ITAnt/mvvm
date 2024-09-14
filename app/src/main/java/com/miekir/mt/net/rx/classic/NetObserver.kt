//package com.miekir.mt.net.rx.classic
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
//import com.itant.mvp.observer.listener.CompleteListener
//import com.itant.mvp.observer.listener.ErrorListener
//import com.itant.mvp.observer.listener.ResultListener
//import com.itant.mvp.observer.listener.StartListener
//import com.itant.mvp.observer.listener.SuccessListener
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
//            mBuilder.bOnError?.onError(result.code, result.message)
//            mBuilder.bOnResult?.onResult(false, null, result.code, result.message)
//            if (onceTask) {
//                mBuilder.bOnComplete?.onComplete()
//            }
//        } catch (e: Exception) {
//            L.e(e.toString())
//            mBuilder.bOnError?.onError(ExceptionManager.getInstance().failedCode, e.message)
//            mBuilder.bOnResult?.onResult(false, null, ExceptionManager.getInstance().failedCode, e.message)
//            if (onceTask) {
//                mBuilder.bOnComplete?.onComplete()
//            }
//        }
//    }
//
//    /**
//     * 开始
//     */
//    override fun onStart() {
//        super.onStart()
//        mBuilder.bOnStart?.onStart()
//    }
//
//    /**
//     * 成功
//     */
//    private fun onSuccess(result: T?) {
//        mBuilder.bOnSuccess?.onSuccess(result)
//        mBuilder.bOnResult?.onResult(true, result, ExceptionManager.getInstance().successCode, "")
//        if (onceTask) {
//            mBuilder.bOnComplete?.onComplete()
//        }
//    }
//
//    override fun onError(e: Throwable) {
//        super.onError(e)
//        val exception = ExceptionManager.getInstance().exceptionHandler.handleException(e)
//
//        mBuilder.bOnError?.onError(exception.code, exception.message)
//        mBuilder.bOnResult?.onResult(false, null, exception.code, exception.message)
//        mBuilder.bOnComplete?.onComplete()
//    }
//
//    override fun onComplete() {
//        super.onComplete()
//        if (!onceTask) {
//            mBuilder.bOnComplete?.onComplete()
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
//        internal var bOnStart: StartListener? = null
//
//        // 成功的回调函数
//        internal var bOnSuccess: SuccessListener<R>? = null
//
//        // 失败的回调函数
//        internal var bOnError: ErrorListener? = null
//
//        // 任务完成后的回调函数，成功或失败都会回调
//        internal var bOnComplete: CompleteListener? = null
//
//        // 任务完成后的回调函数，成功或失败都会回调，并附带结果
//        internal var bOnResult: ResultListener<R>? = null
//
//        internal var bContext: Context? = null
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
//        fun onStart(start: StartListener): Builder<R> {
//            bOnStart = start
//            return this
//        }
//
//        /**
//         * 成功的回调函数
//         */
//        fun onSuccess(success: SuccessListener<R>): Builder<R> {
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
//        fun onError(error: ErrorListener): Builder<R> {
//            bOnError = error
//            return this
//        }
//
//        /**
//         * 任务完成后的回调函数，成功或失败都会回调
//         */
//        fun onComplete(complete: CompleteListener): Builder<R> {
//            bOnComplete = complete
//            return this
//        }
//
//        /**
//         * 任务完成后的回调函数，成功或失败都会回调，并附带结果
//         */
//        fun onResult(result: ResultListener<R>): Builder<R> {
//            bOnResult = result
//            return this
//        }
//
//        fun context(context: Context? = null): NetObserver.Builder<R> {
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
