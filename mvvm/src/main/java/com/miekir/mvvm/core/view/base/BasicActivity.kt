package com.miekir.mvvm.core.view.base

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentOnAttachListener
import com.miekir.mvvm.MvvmManager
import com.miekir.mvvm.core.view.result.ActivityResult
import com.miekir.mvvm.extension.applyHighRefreshRate
import com.miekir.mvvm.log.L
import com.miekir.mvvm.widget.loading.DialogData
import com.miekir.mvvm.widget.loading.LoadingType
import com.miekir.mvvm.widget.loading.TaskLoading
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.absoluteValue

/**
 * 基础Activity，不做屏幕适配
 */
abstract class BasicActivity : AppCompatActivity(), IView {
    private val loadingDialogMap = ConcurrentHashMap< TaskLoading, Dialog>()

    /**
     * 权限申请
     */
    private val mPermissionList = ArrayList<String>()
    private val mPermissionQueue = LinkedBlockingQueue<Runnable>()
    @Volatile
    private var mPermissionCallback: ((granted: Boolean, temp: Boolean, detail : Map<String, Boolean>) -> Unit)? = null
    private val mPermissionLauncher = registerForActivityResult<Array<String>, Map<String, Boolean>>(ActivityResultContracts.RequestMultiplePermissions()) { isGranted: Map<String, Boolean> ->
        // 回调这里是主线程
        var isTemp = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in mPermissionList) {
                isTemp = shouldShowRequestPermissionRationale(permission)
                if (!isTemp) {
                    break
                }
            }
        } else {
            isTemp = false
        }

        if (isGranted.containsValue(false)) {
            mPermissionCallback?.invoke(false, isTemp, isGranted)
        } else {
            mPermissionCallback?.invoke(true, isTemp, isGranted)
        }
        mPermissionCallback = null
        waitingPermissionResult = false

        mPermissionQueue.poll()?.run()
    }

    /**
     * 代替startActivityForResult
     */
    @Volatile
    private var mActivityResultCallback: ActivityResult? = null
    private val mActivityQueue = LinkedBlockingQueue<Runnable>()
    private val mActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback { result ->
        if (mActivityResultCallback == null) {
            return@ActivityResultCallback
        }
        if (result.resultCode == RESULT_OK) {
            mActivityResultCallback?.onResultOK(result.data)
        } else {
            mActivityResultCallback?.onResultFail(result.resultCode)
        }
        mActivityResultCallback?.onResult()

        mActivityResultCallback = null
        waitingActivityResult = false
        mActivityQueue.poll()?.run()
    })

    @Volatile
    private var waitingPermissionResult = false
    @Volatile
    private var waitingActivityResult = false

    /**
     * 界面是否包含Fragment
     */
    private var containsFragments = false
    /**
     * 用于监听界面是否有Fragment
     */
    private val fragmentAttachListener: FragmentOnAttachListener = FragmentOnAttachListener { _, _ ->
        containsFragments = true
    }

    /**
     * savedInstanceState == null表示Activity第一次创建，部分情况如屏幕旋转Activity会二次创建
     */
    protected var mSavedInstanceState: Bundle? = null

    /**
     * 用于恢复加载框
     */
    val loadingViewModel by viewModels<LoadingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 系统调节字体大小不影响本APP，必须放到super.onCreate前面
        if (enableHighRefreshRate()) {
            applyHighRefreshRate()
        }
        super.onCreate(savedInstanceState)
        mSavedInstanceState = savedInstanceState
        supportFragmentManager.addFragmentOnAttachListener(fragmentAttachListener)
        //进入页面隐藏输入框
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        onRestoreLoading()
    }

    /**
     * Activity生命周期重建，如旋转屏幕等，需要重建对话框，防止崩溃
     */
    private fun onRestoreLoading() {
        if (!enableTaskLoadingRecreate() || loadingViewModel.mLoadingDialogList.isEmpty()) {
            return
        }

        // 恢复任务弹窗
        for (dialogData in loadingViewModel.mLoadingDialogList) {
            dialogData.taskJob?.let { job ->
                if (job.isActive() && dialogData.completeLiveData.value != true) {
                    val taskLoading = MvvmManager.getInstance().newTaskLoading()
                    showLoading(taskLoading, dialogData)
                } else {
                    loadingViewModel.removeLoadingDialogData(dialogData)
                }
            } ?: run {
                loadingViewModel.removeLoadingDialogData(dialogData)
            }
        }
    }


    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        handleEnterAnimation()
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        handleEnterAnimation()
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        handleEnterAnimation()
    }

    override fun finish() {
        super.finish()
        handleExitAnimation()
    }

    /**
     * 启动其他Activity时，是否使用配置的Activity动画
     */
    open fun enableStartAnimation():Boolean {
        return false
    }

    /**
     * 销毁当前Activity时，是否使用配置的Activity动画
     */
    open fun enableDestroyAnimation():Boolean {
        return false
    }

    /**
     * 执行进入的动画
     */
    private fun handleEnterAnimation() {
        if (enableStartAnimation()) {
            MvvmManager.getInstance().activityAnimation?.enterAnimation(this)
        }
    }

    /**
     * 执行退出的动画
     */
    private fun handleExitAnimation() {
        if (enableDestroyAnimation()) {
            MvvmManager.getInstance().activityAnimation?.exitAnimation(this)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!waitingPermissionResult) {
            mPermissionQueue.peek()?.run()
        }

        if (!waitingActivityResult) {
            mActivityQueue.peek()?.run()
        }
    }

    /**
     * 申请权限扩展
     */
    fun requestPermissionsForResult(permissions: List<String>, callback: ((granted: Boolean, temp: Boolean, detail : Map<String, Boolean>) -> Unit)? = null) {
        if (permissions.isEmpty()) {
            callback?.invoke(true, false, hashMapOf())
            return
        }
        // 在主线程发起申请
        if (/*(lifecycle.currentState == Lifecycle.State.RESUMED || lifecycle.currentState == Lifecycle.State.STARTED) && */!waitingPermissionResult) {
            // 界面可见，且没有已弹出的权限申请对话框，则直接申请
            waitingPermissionResult = true
            mPermissionList.clear()
            mPermissionCallback = callback
            try {
                mPermissionList.addAll(LinkedHashSet<String>(permissions))
                mPermissionLauncher.launch(mPermissionList.toTypedArray())
            } catch (e: Exception) {
                L.e(e.toString())
                waitingPermissionResult = false
                mPermissionList.clear()
                mPermissionCallback = null
                mPermissionQueue.poll()?.run()
                // 让调用者决定要不要继续，如果调用者catch了，那就继续，调用者不catch，那就Game Over
                throw e
            }
        } else {
            // 界面不可见，或者有正在申请的权限对话框，则入队等待
            mPermissionQueue.offer(Runnable {
                requestPermissionsForResult(permissions, callback)
            })
        }
    }

    fun openActivityForResult(intent: Intent, result: ActivityResult) {
        // 在主线程发起申请
        if (/*(lifecycle.currentState == Lifecycle.State.RESUMED || lifecycle.currentState == Lifecycle.State.STARTED) && */!waitingActivityResult) {
            waitingActivityResult = true
            mActivityResultCallback = result
            try {
                mActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                L.e(e.toString())
                waitingActivityResult = false
                mActivityResultCallback = null
                mActivityQueue.poll()?.run()
                throw e
            }
        } else {
            mActivityQueue.offer(Runnable {
                openActivityForResult(intent, result)
            })
        }
    }

    /**
     * 由于屏幕旋转、语言更改、字体大小变化导致Activity重建，如果界面存在Fragment，可能会出现数据混乱的问题
     * 此方法表示是否允许有Fragment存在的界面保存数据重建（可能造成数据混乱），默认不允许
     * false：禁用Activity重建，即在上述情况发生时，界面存在的Fragment会重新创建，而不是使用旧的；
     * true：保持默认，即允许Activity重建
     */
    open fun enableHistoryOutState(): Boolean {
        return true
    }

    /**
     * 是否启用高刷新率
     */
    open fun enableHighRefreshRate(): Boolean {
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (enableHistoryOutState()) {
            // 无论界面是否包含Fragment都允许保存数据进行重建，可能会数据混乱
            super.onSaveInstanceState(outState)
        } else {
            if (containsFragments) {
                // 界面包含Fragment时不使用旧数据重建
                super.onSaveInstanceState(Bundle())
            } else {
                // 界面不包含Fragment时使用旧数据重建（原生默认）
                super.onSaveInstanceState(outState)
            }
        }
    }

    /**
     * 点击空白处隐藏软键盘，需要在根布局配置
     * android:focusable="true"
     * android:focusableInTouchMode="true"
     * 否则焦点乱飞，比如点击提交按钮，焦点被上一个输入框获取了
     */
    open fun enableTouchSpaceHideKeyboard(): Boolean {
        return false
    }

    //private val moveSlop:Int by lazy { ViewConfiguration.get(this).scaledTouchSlop * 2 }
    /**
     * 上一次执行隐藏软键盘的时间戳，防止短时间内多次执行
     */
    private var lastHideMillis = 0L
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (enableTouchSpaceHideKeyboard() && event.action == MotionEvent.ACTION_DOWN) {
            val currentFocusedView = currentFocus
            if (currentFocusedView is EditText) {
                val outRect = Rect()
                currentFocusedView.getGlobalVisibleRect(outRect)
                if (currentFocusedView.isFocused &&
                    (System.currentTimeMillis()-lastHideMillis).absoluteValue > 500 &&
                    !outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    lastHideMillis = System.currentTimeMillis()
                    currentFocusedView.clearFocus()
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
                        hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 是否要隐藏软键盘
     */
    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if ((v is EditText)) {
            val l = intArrayOf(0, 0)
            v.getLocationOnScreen(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(event.rawX > left && event.rawX < right && event.rawY > top && event.rawY < bottom)
        }
        return false
    }

    /**
     * Android 12之后，主界面按返回居然不销毁Activity，哪个家伙拍脑袋想出来的（可能是根Activity才有这个特性）
     */
    /*override fun onBackPressed() {
        finish()
    }*/

    /**
     * Activity旋转，需要重新创建，否则ViewModel会一直持有旧Dialog实例-->Activity的引用，
     * 导致内存泄露，如果一段时间后调用dismiss，会闪退，所以应该发现旋转时马上dismiss
     */
    override fun onDestroy() {
        for ((taskLoading, dialog) in loadingDialogMap) {
            dialog.setOnCancelListener(null)
            taskLoading.dismiss()
        }
        loadingDialogMap.clear()
        
        // 清理LoadingViewModel中的DialogData观察者
        loadingViewModel.clearAllObservers(this)
        
        super.onDestroy()
        supportFragmentManager.removeFragmentOnAttachListener(fragmentAttachListener)
        mPermissionQueue.clear()
        mActivityQueue.clear()
    }

    /**
     * 仅仅关闭对话框，不销毁任务进程
     */
    internal fun dismissLoading(taskLoading: TaskLoading) {
        loadingDialogMap[taskLoading]?.setOnCancelListener(null)
        taskLoading.dismiss()
        loadingDialogMap.remove(taskLoading)
    }

    @MainThread
    internal fun showLoading(realLoading: TaskLoading, dialogData: DialogData) {
        realLoading.mDialogData = dialogData
        val dialog = realLoading.newLoadingDialog(this)
        realLoading.mDialog = dialog
        loadingDialogMap[realLoading] = dialog
        dialog.setCanceledOnTouchOutside(false)
        if (dialogData.loadingType == LoadingType.STICKY) {
            dialog.setCancelable(false)
        } else {
            dialog.setCancelable(true)
        }
        dialog.setOnCancelListener {
            if (dialogData.loadingType != LoadingType.VISIBLE_ALONE) {
                dialogData.taskJob?.cancel()
                loadingViewModel.removeLoadingDialogData(dialogData)
                dismissLoading(realLoading)
            }
        }
        realLoading.show()
        // 使用单次观察者，避免重复观察和内存泄漏
        dialogData.completeLiveData.observe(this) { completed ->
            if (completed == true) {
                loadingViewModel.removeLoadingDialogData(dialogData)
                dismissLoading(realLoading)
            }
        }
    }
}