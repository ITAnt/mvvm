package com.miekir.mvvm.extension

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.Window
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


/**
 * 扩展控件点击范围
 * @param leftSize 左侧扩展的大小（像素）
 * @param topSize 上侧扩展的大小（像素）
 * @param rightSize 右侧扩展的大小（像素）
 * @param bottomSize 下侧扩展的大小（像素）
 */
fun View.expandClick(leftSize: Int = 16, topSize: Int = 16, rightSize: Int = 16, bottomSize: Int = 16) {
    val viewParent = parent as View
    val view = this
    viewParent.post {
        val delegateArea = Rect()
        view.getHitRect(delegateArea)
        delegateArea.left -= leftSize
        delegateArea.top -= topSize
        delegateArea.right += rightSize
        delegateArea.bottom += bottomSize
        viewParent.touchDelegate = TouchDelegate(delegateArea, view)
    }
}

fun Bitmap.tint(color: Int): Bitmap =
    Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888).also { outBmp ->
        Canvas(outBmp).drawBitmap(
            this, 0f, 0f,
            Paint().apply {
                this.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        )
    }

/**
 * @date : 2021/4/10 20:55
 * @author : 詹子聪
 *  View的扩展
 */
fun View.colorTransition(
    @ColorRes startColor: Int,
    @ColorRes endColor: Int,
    duration: Long = 10000L
): ValueAnimator {
    val colorFrom = ContextCompat.getColor(context, startColor)
    val colorTo = ContextCompat.getColor(context, endColor)
    val colorAnimation: ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
    colorAnimation.duration = duration

    colorAnimation.addUpdateListener {
        if (it.animatedValue is Int) {
            val color = it.animatedValue as Int
            setBackgroundColor(color)
        }
    }
    colorAnimation.start()
    return colorAnimation
}

/**
 * 隐藏底部导航栏
 */
fun hideNavigationBar(window: Window) {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // 上浮到状态栏
            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 状态栏深色字体
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
}
/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    window.setDecorFitsSystemWindows(false)
    val controller = window.insetsController
    if (controller != null) {
        controller.hide(WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
} else {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
}*/

/**
 * 界面销毁的时候移除弹窗
 */
fun Dialog.dismissWhenActivityDestroy() {
    (context as? androidx.activity.ComponentActivity)?.lifecycle?.addObserver(object:
        LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                dismiss()
            }
        }
    })
}

/**
 * 让EditText获取焦点并显示软键盘
 */
fun EditText.requestFocusAndShowKeyboard() {
    requestFocus()
    dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0f, 0f, 0))
    dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0))
}

///**
// * 获取Presenter实例
// * ViewModelStoreOwner不同时，则无论key是否相同presenter实例都不同（Activity自发的生命周期重建如屏幕旋转等除外），
// * P不同时，则无论key是否相同presenter实例都不同，
// * ViewModelStoreOwner相同，P相同时，key相同（或不传）则presenter实例相同，key不同presenter实例都不同。
// * 用法：
// * private val mWorkPresenter: PublishWorkPresenter by lazy { getPresenter() }
// */
//inline fun <reified V : IView, reified P : BasePresenter<V>> IView.getPresenter(
//    owner: ViewModelStoreOwner? = null,
//    key: String? = null
//): P {
//    val presenter: P
//    if (owner != null) {
//        presenter = if (TextUtils.isEmpty(key)) {
//            ViewModelProvider(owner).get(P::class.java)
//        } else {
//            ViewModelProvider(owner).get(key!!, P::class.java)
//        }
//    } else {
//        if (this !is ViewModelStoreOwner) {
//            throw RuntimeException("如果owner参数为空，则当前类必须继承${BasicActivity::class.java.name}或${BasicFragment::class.java.name}")
//        }
//        presenter = if (TextUtils.isEmpty(key)) {
//            ViewModelProvider(this as ViewModelStoreOwner).get(P::class.java)
//        } else {
//            ViewModelProvider(this as ViewModelStoreOwner).get(key!!, P::class.java)
//        }
//    }
//    presenter.attachView(this as V)
//    return presenter
//}

