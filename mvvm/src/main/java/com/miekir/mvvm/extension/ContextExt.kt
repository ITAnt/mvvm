@file:JvmName("ContextUtils")
package com.miekir.mvvm.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import com.miekir.mvvm.context.GlobalContext
import com.miekir.mvvm.tools.ThreadTools

/**
 * @date : 2021/4/1 13:12
 * @author : 詹子聪
 *  Context相关扩展
 */

/**
 * 简单地启动Activity，默认不用登录就可以打开
 * @param newTask 是否新开任务栈，仅在context为application context则总是新开任务栈
 */
inline fun <reified T : Activity> Context.openActivity(newTask: Boolean = false) {
    val startIntent = Intent(this, T::class.java)
    if (newTask || this !is Activity) {
        startIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (!ThreadTools.isMainThread()) {
        GlobalContext.runOnUiThread {
            startActivity(startIntent)
        }
    } else {
        startActivity(startIntent)
    }
}

/**
 * 启用高刷新率，如丝流畅
 * M 是 6.0，6.0修改了新的api，并且就已经支持修改window的刷新率了。但是6.0那会儿，也没什么手机支持高刷新率吧，
 * 所以也没什么人注意它。我更倾向于直接判断 O，也就是 Android 8.0，我觉得这个时候支持高刷新率的手机已经开始了。
 */
fun Activity.applyHighRefreshRate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 获取系统window支持的模式
        val modes = window.windowManager.defaultDisplay.supportedModes
        // 对获取的模式，基于刷新率的大小进行排序，从小到大排序
        modes.sortBy {
            it.refreshRate
        }

        window.let {
            val lp = it.attributes
            // 取出最大的那一个刷新率，直接设置给window
            lp.preferredDisplayModeId = modes.last().modeId
            it.attributes = lp
        }
    }
}

/**
 * 让状态栏变透明
 */
fun Activity.transparentStatusBar(): Activity {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.statusBarColor = Color.TRANSPARENT
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    return this
}

/**
 * 状态栏透明，字体黑色
 */
fun Activity.transparentStatusBlack() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.TRANSPARENT
    statusTextColorBlack()
}

/**
 * 状态栏透明，字体白色
 */
fun Activity.transparentStatusWhite() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.TRANSPARENT
    statusTextColorWhite()
}

/**
 * 隐藏导航栏
 * <!--Android 10 及api29之后，解决全透明导航栏不成功的问题-->
 * <!--<item name="android:enforceStatusBarContrast">false</item>-->
 * <!--<item name="android:enforceNavigationBarContrast">false</item>-->
 */
fun Activity.transparentNavigationBar() {
    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    window.navigationBarColor = Color.TRANSPARENT
}

/**
 * 隐藏状态栏和导航栏，滑动出现后，又隐藏
 */
fun Activity.fullScreenSticky() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

/**
 * 让状态栏字体颜色变深（黑色）
 */
fun Activity.statusTextColorBlack() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

/**
 * 让状态栏字体颜色变白
 */
fun Activity.statusTextColorWhite() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
}

/**
 * 设置状态栏颜色
 * @date 2021-7-24 11:48
 * @author 詹子聪
 */
fun Activity.setStatusColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}

// 白色状态栏，黑色字体
/*activity?.window?.run {
    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = ContextCompat.getColor(requireActivity(), R.color.white)
    decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}*/

/*
public static void makeStatusTransparent() {
    //make full transparent statusBar
    if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
    }
    if (Build.VERSION.SDK_INT >= 19) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    if (Build.VERSION.SDK_INT >= 21) {
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}

public static void setWindowFlag(Activity activity, final int bits, boolean on) {
    Window win = activity.getWindow();
    WindowManager.LayoutParams winParams = win.getAttributes();
    if (on) {
        winParams.flags |= bits;
    } else {
        winParams.flags &= ~bits;
    }
    win.setAttributes(winParams);
}*/

/*if (permissions.isNotEmpty()) {
    RxPermissions(this).requestEachCombined(*permissions)
        .subscribe(Consumer() {
            if (!TextUtils.isEmpty(message) && !it.granted) {
                ToastTools.showShort(message)
            }
            callback?.invoke(it.granted, it.shouldShowRequestPermissionRationale)
        })
}*/