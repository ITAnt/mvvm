package com.miekir.mvvm.core.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding

/**
 * 基础Fragment，不做屏幕适配
 * 注意不要在子类的onDestroy里调用binding了
 */
abstract class BasicBindingFragment<VB : ViewBinding> : BasicFragment() {
    var bindingNullable: VB? = null
    val binding get() = bindingNullable!!

    /**
     * 布局文件绑定
     */
    abstract fun onBindingInflate(): VB
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bindingNullable = onBindingInflate()
        if (bindingNullable is ViewDataBinding) {
            // 不能赋值this，会产生内存泄漏
            (bindingNullable as? ViewDataBinding?)?.lifecycleOwner = viewLifecycleOwner
        }
        return bindingNullable?.root
    }

    override fun onDestroyView() {
        // 在navigation框架下，跳转新Fragment，旧Fragment会执行onDestroyView儿不执行onDestroy
        bindingNullable = null
        super.onDestroyView()
    }
}