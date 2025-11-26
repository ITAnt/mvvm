package com.miekir.mvvm.exception.impl

import com.miekir.mvvm.exception.ExceptionManager
import com.miekir.mvvm.exception.TaskException

/**
 * 任务超时
 */
class TimeoutException: TaskException(ExceptionManager.getInstance().exceptionHandler.timeoutCode)