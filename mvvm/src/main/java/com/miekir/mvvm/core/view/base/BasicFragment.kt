package com.miekir.mvvm.core.view.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.miekir.mvvm.core.view.IView

/**
 * 基础Fragment，不做屏幕适配
 * 坑：如果集成了谷歌的navigation，经常会存在onDestroyView但没有onDestroy的情况，如果Fragment有成员变量Adapter，
 * 需要在onDestroyView中释放(mRecyclerView.adapter = null)，不然会内存泄漏（adapter会持有RecyclerView的引用）
 */
abstract class BasicFragment : Fragment(), IView {
    /**
     * 当前是否第一次显示
     */
    private var firstVisible = true

    /**
     * savedInstanceState == null表示Fragment第一次被创建
     */
    protected var mSavedInstanceState: Bundle? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mSavedInstanceState = savedInstanceState
    }

    /**
     * 在这里添加LiveData的观察者是安全的，不会产生内存泄漏
     */
    open fun onObserve() {}

    override fun onResume() {
        super.onResume()
        if (firstVisible) {
            firstVisible = false
            // 解决在navigation下，Fragment刚创建就navigate到一个新的Fragment，旧的Fragment里的LiveData会产生内存泄漏
            view?.post {
                onInit()
                val created = try {
                    viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
                } catch (e: Exception) {
                    false
                }
                if (created) {
                    onObserve()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firstVisible = true
    }

    /**
     * 懒加载，当Fragment可见的时候，再去加载数据；
     * 应用初始化会先调用完所有的setUserVisibleHint再调用onViewCreated，然后切换的时候，就只调用setUserVisibleHint了（已过时）；
     * 在Fragment还不可见的时候，会先执行onCreateView和onStart；
     * savedInstanceState == null表示Fragment第一次被创建；
     */
    protected abstract fun onInit()
}