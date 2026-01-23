package com.miekir.mvvm.task.core

import com.miekir.mvvm.exception.TaskException
import com.miekir.mvvm.exception.impl.CancelException
import com.miekir.mvvm.exception.impl.DuplicateException
import com.miekir.mvvm.exception.impl.TimeoutException
import com.miekir.mvvm.log.L
import com.miekir.mvvm.task.net.NetResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 任务状态
 * 使用sealed class确保类型安全和穷尽性检查
 *
 * @param T 成功时返回的数据类型
 */
sealed class TaskStatus<out T> {
    /**
     * 初始状态 - 任务尚未开始
     */
    data object Idle : TaskStatus<Nothing>()

    /**
     * 加载中
     * @param isDuplicate 是否是重复提交的任务
     */
    data class Loading(val isDuplicate: Boolean = false) : TaskStatus<Nothing>()

    /**
     * 成功
     * @param data 任务返回的数据
     */
    data class Success<T>(val data: T) : TaskStatus<T>()

    /**
     * 失败
     * @param exception 任务失败的异常
     */
    data class Failure(val exception: TaskException) : TaskStatus<Nothing>()
}

private class TaskScope(private val tag: String?) : AutoCloseable {
    init { TaskManager.addTask(tag) }
    override fun close() {
        TaskManager.removeTask(tag)
    }
}

/**
 * 任务管理器
 * 用于跟踪正在执行的任务，防止重复提交
 */
private object TaskManager {
    private val activeTasks = CopyOnWriteArraySet<String>()

    fun isTaskActive(tag: String?): Boolean {
        return tag != null && tag in activeTasks
    }

    fun addTask(tag: String?) {
        tag?.let { activeTasks.add(it) }
    }

    fun removeTask(tag: String?) {
        tag?.let { activeTasks.remove(it) }
    }
}

/**
 * 启动任务
 *
 * @param taskBody 任务执行体
 * @param flow 状态流，用于发送任务状态
 * @param tag 任务标签，用于防止重复提交
 * @return 任务执行结果
 */
suspend fun <T> startTask(
    taskBody: suspend () -> T,
    flow: MutableStateFlow<TaskStatus<T>>? = null,
    timeoutMillis: Long = -1L,
    tag: String? = null
): Result<T> {
    // 检查重复任务
    if (TaskManager.isTaskActive(tag)) {
        L.d("Task with tag '$tag' is already running")
        flow?.emit(TaskStatus.Loading(isDuplicate = true))
        return Result.failure(DuplicateException())
    }

    // 添加任务标签
    TaskManager.addTask(tag)

    // 发送加载状态
    flow?.emit(TaskStatus.Loading(isDuplicate = false))

    return TaskScope(tag).use {
        // scope -> 我们这里没用到scope，所以可以不写
        // 执行任务
        val result = runCatching {
            val data = if (timeoutMillis > 0L) {
                withTimeout(timeoutMillis) {
                    taskBody()
                }
            } else {
                taskBody()
            }

            // 如果是网络响应，验证响应
            if (data is NetResponse) {
                data.valid()
            }
            data
        }

        // fold = 打开Result，分别处理成功和失败两种情况，相比直接.onSuccess和.onFailure，重新抛出异常起作用
        result.fold(
            onSuccess = { data ->
                L.d("Task succeeded: $data")
                flow?.emit(TaskStatus.Success(data))
                Result.success(data)
            },
            onFailure = { exception ->
                L.e("Task failed: $exception")

                val taskException = when (exception) {
                    is TimeoutCancellationException -> {
                        L.d("Task timed out")
                        TimeoutException()
                    }
                    is CancellationException -> {
                        L.d("Task cancelled")
                        CancelException()
                    }
                    is TaskException -> exception
                    else -> TaskException(exception)
                }

                flow?.emit(TaskStatus.Failure(taskException))

                //这里抛出异常的话，协程里startTask后的代码也不会执行
                //if (exception is CancellationException) { throw exception }
                Result.failure(taskException)
            }
        )
    }
}

/**
 * 扩展函数：为MutableStateFlow添加startTask方法
 *
 * 使用示例：
 * ```
 * val statusFlow = MutableStateFlow<TaskStatus<String>>(TaskStatus.Idle)
 *
 * viewModelScope.launch {
 *     statusFlow.startTask(tag = "fetch_data") {
 *         repository.fetchData()
 *     }
 * }
 * ```
 */
suspend fun <T> MutableStateFlow<TaskStatus<T>>.startTask(
    tag: String? = null,
    taskBody: suspend () -> T
): Result<T> {
    return startTask(
        taskBody = taskBody,
        flow = this,
        tag = tag
    )
}

// ============================================ 使用示例 ============================================

/**
 * ViewModel示例
 */
/*
class MyViewModel : ViewModel() {
    private val _taskStatus = MutableStateFlow<TaskStatus<String>>(TaskStatus.Idle)
    val taskStatus: StateFlow<TaskStatus<String>> = _taskStatus.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _taskStatus.startTask(tag = "load_data") {
                // 模拟网络请求
                delay(2000)
                "Success data"
            }
        }
    }

    fun reset() {
        _taskStatus.value = TaskStatus.Idle
    }
}
*/

/**
 * Composable示例
 */
/*
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    val status by viewModel.taskStatus.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val currentStatus = status) {
            is TaskStatus.Idle -> {
                Button(onClick = { viewModel.loadData() }) {
                    Text("开始任务")
                }
            }

            is TaskStatus.Loading -> {
                if (currentStatus.isDuplicate) {
                    Text("任务正在执行中...")
                } else {
                    CircularProgressIndicator()
                }
            }

            is TaskStatus.Success -> {
                Text("成功: ${currentStatus.data}")
                Button(onClick = { viewModel.reset() }) {
                    Text("重新开始")
                }
            }

            is TaskStatus.Failure -> {
                Text("失败: ${currentStatus.exception.message}")
                Button(onClick = { viewModel.reset() }) {
                    Text("重试")
                }
            }
        }
    }
}
*/
