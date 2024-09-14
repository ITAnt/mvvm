package com.miekir.mt.ui.main

import com.miekir.mvvm.core.view.base.IView


interface IMainView : IView {
    fun onMainTaskCallback()
    fun onLoginResult(success: Boolean, result: String?, code: Int, errorMessage: String?)
}