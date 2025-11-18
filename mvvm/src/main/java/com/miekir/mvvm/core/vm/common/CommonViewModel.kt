package com.miekir.mvvm.core.vm.common

import com.miekir.mvvm.context.GlobalContext
import com.miekir.mvvm.exception.TaskException
import com.miekir.mvvm.log.L
import com.miekir.mvvm.exception.CancelException
import com.miekir.mvvm.task.TaskJob
import com.miekir.mvvm.task.net.NetResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

abstract class CommonViewModel<V>(viewFromChild: V) {
    protected var taskScope: CoroutineScope? = null
    var view: V? = null
        private set

    init {
        view = viewFromChild
    }

    /**
     * 不再持有View的引用，无法再直接调用View中的方法
     */
    protected fun destroyViewModel() {
        view = null
        taskScope?.cancel()
        taskScope = null
    }

    /**
     * 取消所有任务，但还持有View的实例
     */
    fun canCelTask() {
        taskScope?.cancel()
    }

    fun <T> launchTask(
        taskBody: suspend () -> T?,
        onSuccess: ((result: T?) -> Unit)? = null,
        onFailure: ((code: Int, message: String, taskException: TaskException) -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onResult: ((errorResult: TaskException?, normalResult: T?) -> Unit)? = null,): TaskJob? {

        if (taskScope == null) {
            L.e("TaskScope is null")
            return null
        }

        var successCallback: ((result: T?) -> Unit)? = onSuccess
        var failureCallback: ((code: Int, message: String, exception: TaskException) -> Unit)? = onFailure
        var cancelCallback: (() -> Unit)? = onCancel
        var resultCallback: ((errorResult: TaskException?, normalResult: T?) -> Unit)? = onResult

        val taskJob = TaskJob()

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            GlobalContext.runOnUiThread {
                // 获取具体错误类型
                if (exception is CancelException) {
                    // 主动取消
                    cancelCallback?.invoke()
                    failureCallback?.invoke(exception.code, exception.resultMessage, exception)
                    resultCallback?.invoke(exception, null)
                } else {
                    // 任务异常
                    val eResult = TaskException(exception)
                    failureCallback?.invoke(eResult.code, eResult.resultMessage, eResult)
                    resultCallback?.invoke(eResult, null)
                }

                cancelCallback = null
                successCallback = null
                failureCallback = null
                resultCallback = null
            }
        }

        val taskContext = CoroutineName("CommonViewModel IO Task") + Dispatchers.IO + coroutineExceptionHandler
        val job = taskScope?.launch(taskContext) {
            var returnTypeObj: T? = null
            // 在“子线程”执行耗时任务，try catch取消异常
            returnTypeObj = taskBody()
            withContext(CoroutineName("CommonViewModel main Task") + Dispatchers.IO + coroutineExceptionHandler) {
                // 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
                if (returnTypeObj is NetResponse) {
                    (returnTypeObj as NetResponse).valid()
                }

                successCallback?.invoke(returnTypeObj)
                resultCallback?.invoke(null, returnTypeObj)

                cancelCallback = null
                successCallback = null
                failureCallback = null
                resultCallback = null
            }
        }
        job?.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                coroutineExceptionHandler.handleException(taskContext, CancelException())
            }
        }

        taskJob.setup(taskContext, job)
        return taskJob
    }
}