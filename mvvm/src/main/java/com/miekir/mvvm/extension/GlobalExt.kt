package com.miekir.mvvm.extension

import android.content.res.Resources
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.miekir.mvvm.tools.SizeTools
import com.miekir.mvvm.tools.ToastTools
import kotlin.math.absoluteValue

/**
 * 防止快速点击
 */
fun View.setSingleClick(limit: Long = 1000L, clickAction: (view: View) -> Unit) {
    var lastTime = 0L
    this.setOnClickListener {
        val tmpTime = System.currentTimeMillis()
        if ((tmpTime - lastTime).absoluteValue > limit) {
            lastTime = tmpTime
            clickAction.invoke(this)
        }
    }
}

/**
 * EditText优雅判空
 * val mobile = etMobile.getString("手机号不能为空") ?: return
 */
private fun TextView.getString(message: String? = null): String? {
    val text = this.text.toString()
    if (TextUtils.isEmpty(text)) {
        if (!TextUtils.isEmpty(message)) {
            ToastTools.showShort(message)
        }
        return null
    }
    return text
}

/**
 * 把dp值转成像素值
 */
val Float.dp2px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * 把sp值转成像素值
 */
val Float.sp2px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * 把dp值转成像素值
 */
val Float.px2dp
    get() = SizeTools.px2dp(this)

/**
 * 把dp值转成像素值
 */
val Float.px2sp
    get() = SizeTools.px2sp(this)

///**
// * @description 批量设置控制点击事件
// * @param v 点击的控件
// * @param block 处理点击事件回调代码块
// * @time 2020/11/9 15:04
// * 点括号.()的使用
// */
//fun setOnClickListener(vararg v: View?, block: View.() -> Unit) {
//    val listener = View.OnClickListener {
//        it.block()
//        // 等价于block.invoke(it)，block是View的一个扩展函数
//    }
//    v.forEach {
//        it?.setOnClickListener(listener)
//    }
//}
