package com.miekir.mvvm.core.view

interface IView {
    /**
     * 加载框是否自动恢复重建
     */
    fun enableTaskLoadingRecreate(): Boolean {
        return true
    }
}

