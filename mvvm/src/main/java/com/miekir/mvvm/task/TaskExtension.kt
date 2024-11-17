package com.miekir.mvvm.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miekir.mvvm.context.GlobalContext
import com.miekir.mvvm.exception.TaskException
import com.miekir.mvvm.log.L
import com.miekir.mvvm.task.net.NetResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.cancellation.CancellationException

/*---------------------------------以下为协程实现耗时任务-------------------------------------*/
/**
 * 协程方式实现耗时任务，有无加载框，有无回调都可以自定义
 *
 * 执行网络请求时，taskBody必须是直接返回接口调用，不能返回接口调用后得到的BaseResponse的data成员变量，
 * 否则不会处理服务器正常工作时返回的错误码，直接把它当作请求成功了
 *
 * @param onResult errorResult为null则表示请求成功
 */
fun <T> ViewModel.launchModelTask(
    taskBody: suspend () -> T?,
    onSuccess: ((result: T?) -> Unit)? = null,
    onFailure: ((code: Int, message: String, exception: TaskException) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onResult: ((normalResult: T?, errorResult: TaskException?) -> Unit)? = null,): TaskJob {

    var successCallback: ((result: T?) -> Unit)? = onSuccess
    var failureCallback: ((code: Int, message: String, taskException: TaskException) -> Unit)? = onFailure
    var cancelCallback: (() -> Unit)? = onCancel
    var resultCallback: ((normalResult: T?, errorResult: TaskException?) -> Unit)? = onResult

    val taskJob = TaskJob()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        // 耗时任务出错后，回到主线程
        GlobalContext.runOnUiThread {
            taskJob.onResult()

            // 获取具体错误类型
            if (exception is CancelException) {
                // 主动取消
                cancelCallback?.invoke()
                failureCallback?.invoke(exception.code, exception.resultMessage, exception)
                resultCallback?.invoke(null, exception)
            } else {
                // 任务异常
                val eResult = TaskException(exception)
                failureCallback?.invoke(eResult.code, eResult.resultMessage, eResult)
                resultCallback?.invoke(null, eResult)
            }

            cancelCallback = null
            successCallback = null
            failureCallback = null
            resultCallback = null
        }
    }

    val taskContext = CoroutineName("ViewModel Main Task") + Dispatchers.Main + coroutineExceptionHandler
    val job = viewModelScope.launch(taskContext) {
        var returnTypeObj: T? = null
        // 在“子线程”执行耗时任务，捕获取消异常
        withContext(CoroutineName("ViewModel IO Task") + Dispatchers.IO + coroutineExceptionHandler) {
            // withTimeout超时抛出异常，withTimeoutOrNull超时返回null，而不是抛出异常
            try {
                returnTypeObj = taskBody.invoke()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw CancelException()
                } else {
                    throw e
                }
            }
        }

        taskJob.onResult()

        // 耗时任务完成后，回到主线程
        // 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
        if (returnTypeObj is NetResponse) {
            (returnTypeObj as NetResponse).valid()
        }

        successCallback?.invoke(returnTypeObj)
        resultCallback?.invoke(returnTypeObj, null)

        cancelCallback = null
        successCallback = null
        failureCallback = null
        resultCallback = null
    }

    taskJob.setup(taskContext, job)
    return taskJob
}

/**
 * 运行任务，任务自动切换主线程、子线程，生命周期与scope一致，默认是全局scope
 */
fun <T> launchGlobalTask(
    taskBody: suspend () -> T?,
    onSuccess: ((result: T?) -> Unit)? = null,
    onFailure: ((code: Int, message: String, taskException: TaskException) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onResult: ((normalResult: T?, errorResult: TaskException?) -> Unit)? = null,
    scope: CoroutineScope = GlobalScope
): TaskJob {
    var successCallback: ((result: T?) -> Unit)? = onSuccess
    var failureCallback: ((code: Int, message: String, taskException: TaskException) -> Unit)? = onFailure
    var cancelCallback: (() -> Unit)? = onCancel
    var resultCallback: ((normalResult: T?, errorResult: TaskException?) -> Unit)? = onResult

    val taskJob = TaskJob()
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        // 协程出现异常，不可继续切协程，只能使用线程调度
        GlobalContext.runOnUiThread {
            taskJob.onResult()

            if (exception is CancelException) {
                cancelCallback?.invoke()
                failureCallback?.invoke(exception.code, exception.resultMessage, exception)
                resultCallback?.invoke(null, exception)
            } else {
                val eResult = TaskException(exception)
                failureCallback?.invoke(eResult.code, eResult.resultMessage, eResult)
                resultCallback?.invoke(null, eResult)
            }

            cancelCallback = null
            successCallback = null
            failureCallback = null
            resultCallback = null
        }
    }

    val context = CoroutineName("Global Task") + Dispatchers.IO + coroutineExceptionHandler
    val job = scope.launch(context) {
        val returnTypeObj: T?
        try {
            returnTypeObj = taskBody()
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw CancelException()
            } else {
                throw e
            }
        }

        withContext(Dispatchers.Main) {
            taskJob.onResult()

            // 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
            if (returnTypeObj is NetResponse) {
                (returnTypeObj as NetResponse).valid()
            }
            successCallback?.invoke(returnTypeObj)
            resultCallback?.invoke(returnTypeObj, null)

            cancelCallback = null
            successCallback = null
            failureCallback = null
            resultCallback = null
        }
    }

    taskJob.setup(context, job)
    return taskJob
}

/**
 * 重试
 * inspired by https://stackoverflow.com/questions/46872242/how-to-exponential-backoff-retry-on-kotlin-coroutines
 */
suspend fun <T> retry(
    times: Int,
    block: suspend () -> T,
    startDelayMillis: Long = 0L,
    endDelayMillis: Long = 0L,
): T {
    repeat(times) {
        if (startDelayMillis > 0L) {
            delay(startDelayMillis)
        }
        try {
            return block()
        } catch (exception: Exception) {
            L.e(exception.message)
        }
        if (endDelayMillis > 0L) {
            delay(endDelayMillis)
        }
    }
    // last attempt
    return block()
}
/*
suspend fun <T> retry(
    times: Int,
    initialDelayMillis: Long = 100,
    maxDelayMillis: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMillis
    repeat(times) {
        try {
            return block()
        } catch (exception: Exception) {
            L.e(exception.message)
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
    }
    return block() // last attempt
}*/

/**
 * 多个获取途径取最快的，多个相同返回值的请求同时执行，哪个先返回就用哪个，虽然先返回最快的结果，默认情况下慢的会被取消；
 * 要考虑异常的情况，需要在block处理，如果某个方法总是异常，但总是最快，则每次都是拿不到想要的结果，按照这个规则使用：
 * 异常结果要返回空，如果最先拿到的是空，可设置参数重试，也可以使用waitCombine重试
 * @param cancelOthers 得到最快的结果时，是否取消其他任务
 * @param continueWhenNull 如果最快的是null，继续执行，直到所有任务完毕
 */
suspend fun <T> taskSelect(
    vararg blockList: suspend () -> T,
    cancelOthers: Boolean = true,
    continueWhenNull: Boolean = true
): T? {
    var t: T? = null
    coroutineScope {
        if (blockList.isEmpty()) {
            return@coroutineScope
        }
        val runningList = CopyOnWriteArrayList<Deferred<T>>()
        for (block in blockList) {
            val singleReturn = async(Dispatchers.IO) {
                block.invoke()
            }
            runningList.add(singleReturn)
        }

        if (continueWhenNull) {
            var result: T? = null
            while (result == null && !runningList.isEmpty()) {
                result = select<T> {
                    for (running in runningList) {
                        // 可选：这里可以把it放在Response里进行封装
                        running.onAwait{it}
                    }
                }
                // 尝试每一个结果
                for (running in runningList) {
                    if (running.isCompleted) {
                        runningList.remove(running)
                    }
                }
            }
            t = result
        } else {
            // 直接回调最快的结果
            val fastestValue = select<T> {
                for (running in runningList) {
                    // 可选：这里可以把it放在Response里进行封装
                    running.onAwait{it}
                }
            }
            t = fastestValue
        }

        if (cancelOthers) {
            for (running in runningList) {
                if (!running.isCompleted) {
                    running.cancel()
                }
            }
        }
    }

    return t
}


/**
 * 等待多个结果，全部执行完毕再继续
 * 注意：每个任务都要要单独处理自己的异常，否则会导致整体任务异常中断
 */
suspend fun <T> taskCombine(vararg block: suspend () -> T): List<T> {
    var list: List<T> = ArrayList()
    coroutineScope {
        val runningList = ArrayList<Deferred<T>>()
        for (singleBlock in block) {
            val singleReturn = async(Dispatchers.IO) {
                singleBlock.invoke()
            }
            runningList.add(singleReturn)
        }
        list = awaitAll(*runningList.toTypedArray())
    }
    return list
}
