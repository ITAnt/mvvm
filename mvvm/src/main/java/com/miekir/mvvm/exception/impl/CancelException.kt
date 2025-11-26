package com.miekir.mvvm.exception.impl

import com.miekir.mvvm.exception.ExceptionManager
import com.miekir.mvvm.exception.TaskException

/**
 * 任务取消时的异常。因为协程取消时的CancellationException不会被抛出，所以需要自定义异常来处理。
 */
class CancelException: TaskException(ExceptionManager.getInstance().exceptionHandler.cancelCode)