package com.miekir.mvvm.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 不和ViewPager2冲突的RecyclerView
 */
class RecyclerView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var startX = 0f
    private var startY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = e.x
                startY = e.y
                // 初始请求不拦截，确保RecyclerView处理事件
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(e.x - startX)
                val dy = abs(e.y - startY)
                // 超过阈值后判断方向
                if (dx > touchSlop || dy > touchSlop) {
                    if (dx > dy) {
                        // 水平滑动：允许父容器（ViewPager2）拦截
                        parent.requestDisallowInterceptTouchEvent(false)
                    } else {
                        // 垂直滑动：继续阻止父容器拦截
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 释放时重置
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onInterceptTouchEvent(e)
    }
}
