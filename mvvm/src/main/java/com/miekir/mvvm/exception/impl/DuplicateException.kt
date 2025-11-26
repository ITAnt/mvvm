package com.miekir.mvvm.exception.impl

import com.miekir.mvvm.exception.ExceptionManager
import com.miekir.mvvm.exception.TaskException

/**
 * 任务重复提交
 */
class DuplicateException: TaskException(ExceptionManager.getInstance().exceptionHandler.duplicatedCode)