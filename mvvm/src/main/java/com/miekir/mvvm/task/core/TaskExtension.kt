package com.miekir.mvvm.task.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miekir.mvvm.context.GlobalContext
import com.miekir.mvvm.exception.TaskException
import com.miekir.mvvm.exception.impl.CancelException
import com.miekir.mvvm.exception.impl.TimeoutException
import com.miekir.mvvm.log.L
import com.miekir.mvvm.task.net.NetResponse
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/*---------------------------------以下为协程实现耗时任务-------------------------------------*/
private val globalTagJobMap: ConcurrentHashMap<String, TaskJob> by lazy {
    ConcurrentHashMap()
}

/*private val globalExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    L.e("Global coroutine exception: ${throwable.message}")
}

private val viewModelExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    L.e("ViewModel coroutine exception: ${throwable.message}")
}*/

/**
 * 这里解释一下为什么用 SupervisorJob()：普通的 Job 只要一个子协程崩溃，所有子协程都会被取消。而 SupervisorJob() 不会，它允许子协程独立运行，一个子协程崩溃不会影响其他子协程。对于全局作用域来说，这个特性很重要——比如"数据同步"的协程崩溃了，不能影响"日志上报"的协程。
 */
private val globalScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/**
 * 协程方式实现耗时任务，有无加载框，有无回调都可以自定义
 *
 * 执行网络请求时，taskBody必须是直接返回接口调用，不能返回接口调用后得到的BaseResponse的data成员变量，
 * 否则不会处理服务器正常工作时返回的错误码，直接把它当作请求成功了
 *
 * @param onResult errorResult为null则表示请求成功
 * @param tag 用于防止重复类型【tag】的任务，只有在继承BaseViewMode的子类中才起作用
 *
 * 如果你需要在一个协程里调用一个会阻塞线程、且没有提供 suspend 版本的 API 时，请使用runInterruptible {}包裹taskBody，如：文件系统操作、数据库/JDBC 调用、不支持协程的网络调用、阻塞队列与锁、遗留库或旧版 SDK；
 * 对于CPU密集型任务应该使用withContext(Dispatchers.Default)将计算任务切换到默认的计算线程池，并通过在循环中调用ensureActive()或yield()来确保取消能够及时响应；
 */
fun <T> ViewModel.launchModelTask(
    taskBody: suspend () -> T?,
    onSuccess: ((result: T?) -> Unit)? = null,
    onFailure: ((code: Int, message: String, exception: TaskException) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onResult: ((normalResult: T?, errorResult: TaskException?) -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    taskContext: CoroutineContext = CoroutineName("ViewModel IO Task") + Dispatchers.IO,
    timeoutMillis: Long = -1L,
    tag: String? = null): TaskJob {
    val taskJob = TaskJob()
    if (!tag.isNullOrBlank()) {
        // 使用 putIfAbsent 保证原子性
        val existingJob = globalTagJobMap.putIfAbsent(tag, taskJob)
        if (existingJob != null && existingJob.job?.isCompleted != true && existingJob.job?.isCancelled != true) {
            existingJob.firstLaunch = false
            GlobalContext.runOnUiThread {
                onDuplicate?.invoke()
            }
            return existingJob
        }
    }

    val job = viewModelScope.launch(taskContext) {
        val result = runCatching {
            val taskResult = if (timeoutMillis > 0L) {
                withTimeout(timeoutMillis) {
                    taskBody()
                }
            } else {
                taskBody()
            }
            
            // 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
            // 注意：在taskBody()里调用网络接口时，不能调用到NetResponse的下一层，且最后的返回值（最后一行的结果）必须是NetResponse
            // 否则自己需要拿到NetResponse手动调用一下valid()方法，只有这样才能进行有效性校验
            if (taskResult is NetResponse) {
                taskResult.valid()
            }
            taskResult
        }
        
        // 切换到主线程处理回调
        GlobalContext.runOnUiThread {
            when {
                result.isSuccess -> {
                    val returnTypeObj = result.getOrNull()
                    // 先调用onResult让dialog消失，再回调结果，因为结果里可能会是RecyclerView的adapter在主线程加载大量数据，导致dialog动画卡住，体验很差。
                    onSuccess?.invoke(returnTypeObj)
                    onResult?.invoke(returnTypeObj, null)
                }
                result.isFailure -> {
                    when (val exception = result.exceptionOrNull()) {
                        is TimeoutCancellationException -> {
                            // 超时取消
                            val timeoutException = TimeoutException()
                            onCancel?.invoke()
                            onFailure?.invoke(timeoutException.code, timeoutException.detailMessage, timeoutException)
                            onResult?.invoke(null, timeoutException)
                        }
                        is CancellationException -> {
                            // 主动取消
                            val cancelException = CancelException()
                            onCancel?.invoke()
                            onFailure?.invoke(cancelException.code, cancelException.detailMessage, cancelException)
                            onResult?.invoke(null, cancelException)
                        }
                        is TaskException -> {
                            // 业务异常
                            onFailure?.invoke(exception.code, exception.detailMessage, exception)
                            onResult?.invoke(null, exception)
                        }
                        else -> {
                            // 其他异常
                            val taskException = TaskException(exception ?: RuntimeException("Unknown error"))
                            onFailure?.invoke(taskException.code, taskException.detailMessage, taskException)
                            onResult?.invoke(null, taskException)
                        }
                    }
                }
            }
            taskJob.onResult()

            if (!tag.isNullOrBlank()) {
                globalTagJobMap.remove(tag)
            }
        }
    }
    taskJob.setupJob(job)
    return taskJob
}

/**
 * 运行任务，任务自动切换主线程、子线程，生命周期与scope一致，默认是应用级scope
 */
fun <T> launchGlobalTask(
    taskBody: suspend () -> T?,
    onSuccess: ((result: T?) -> Unit)? = null,
    onFailure: ((code: Int, message: String, taskException: TaskException) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onResult: ((normalResult: T?, errorResult: TaskException?) -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    scope: CoroutineScope = globalScope,
    taskContext: CoroutineContext = CoroutineName("Global IO Task") + Dispatchers.IO,
    timeoutMillis: Long = -1L,
    tag: String? = null
): TaskJob {
    val taskJob = TaskJob()
    if (!tag.isNullOrBlank()) {
        // 使用 putIfAbsent 保证原子性
        val existingJob = globalTagJobMap.putIfAbsent(tag, taskJob)
        if (existingJob != null && existingJob.job?.isCompleted != true && existingJob.job?.isCancelled != true) {
            existingJob.firstLaunch = false
            GlobalContext.runOnUiThread {
                onDuplicate?.invoke()
            }
            return existingJob
        }
    }

    val job = scope.launch(taskContext) {
        val result = runCatching {
            val taskResult = if (timeoutMillis > 0L) {
                withTimeout(timeoutMillis) {
                    taskBody()
                }
            } else {
                taskBody()
            }
            
            // 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
            if (taskResult is NetResponse) {
                taskResult.valid()
            }
            taskResult
        }
        
        // 切换到主线程处理回调
        GlobalContext.runOnUiThread {
            when {
                result.isSuccess -> {
                    val returnTypeObj = result.getOrNull()
                    onSuccess?.invoke(returnTypeObj)
                    onResult?.invoke(returnTypeObj, null)
                }
                result.isFailure -> {
                    when (val exception = result.exceptionOrNull()) {
                        is TimeoutCancellationException -> {
                            // 超时取消
                            val timeoutException = TimeoutException()
                            onCancel?.invoke()
                            onFailure?.invoke(timeoutException.code, timeoutException.detailMessage, timeoutException)
                            onResult?.invoke(null, timeoutException)
                        }
                        is CancellationException -> {
                            // 主动取消
                            val cancelException = CancelException()
                            onCancel?.invoke()
                            onFailure?.invoke(cancelException.code, cancelException.detailMessage, cancelException)
                            onResult?.invoke(null, cancelException)
                        }
                        is TaskException -> {
                            // 业务异常
                            onFailure?.invoke(exception.code, exception.detailMessage, exception)
                            onResult?.invoke(null, exception)
                        }
                        else -> {
                            // 其他异常
                            val taskException = TaskException(exception ?: RuntimeException("Unknown error"))
                            onFailure?.invoke(taskException.code, taskException.detailMessage, taskException)
                            onResult?.invoke(null, taskException)
                        }
                    }
                }
            }

            taskJob.onResult()

            if (!tag.isNullOrBlank()) {
                globalTagJobMap.remove(tag)
            }
        }
    }

    taskJob.setupJob(job)
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
                // 移除已经完成的，剩下的可能要一并清理
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
                if (!running.isCompleted && !running.isCancelled) {
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