package com.miekir.mvvm.widget.loading

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import com.miekir.mvvm.core.view.base.BasicActivity
import com.miekir.mvvm.core.view.base.LoadingViewModel
import com.miekir.mvvm.task.TaskJob
import com.miekir.mvvm.task.progress.ProgressManager
import com.miekir.mvvm.task.progress.ProgressObserver

/**
 * @author 詹子聪
 * @date 2021-11-30 19:47
 */
abstract class TaskLoading: ProgressObserver {
    private var mActivity: BasicActivity? = null
    private var mViewModel: LoadingViewModel? = null
    private var mTaskJob: TaskJob? = null
    @JvmField
    protected var mDialog: Dialog? = null
    private var mLoadingType: LoadingType? = null
    @JvmField
    protected var mMessage: String? = null
    @JvmField
    protected var mProgressId: String? = null
    private var mCanceled = false

    /**
     * 注意：在此方法中，不能为Dialog设置setOnCancelListener，因为在父类使用了该方法监听回收资源等。
     * setOnCancelListener能监听到返回键，onDismissListener不行
     * @return 对话框实例
     */
    abstract fun newLoadingDialog(activity: AppCompatActivity): Dialog

    /**
     * 对话框消失时，不用再主动调用dialog的dismiss()方法
     */
    abstract fun onDismiss()

    /**
     * 对话框显示时，不用再主动调用dialog的show()方法
     */
    abstract fun onShow()

    override fun onProgress(progress: Int) {

    }


    /**
     * @param loadingType 对话框类型（任务），是否可手动取消
     * @param message     对话框的消息
     */
    internal fun setupTask(
        activity: BasicActivity,
        taskJob: TaskJob,
        loadingType: LoadingType = LoadingType.VISIBLE,
        message: String? = "",
        progressId: String?,
    ) {
        // 如果是不可见任务，则不需要弹出对话框，任务的生命周期不与对话框绑定
        if (loadingType == LoadingType.INVISIBLE) {
            return
        }

        mMessage = message
        mLoadingType = loadingType
        mProgressId = progressId
        taskJob.setupTaskLoading(this)
        mTaskJob = taskJob
        mViewModel = activity.loadingViewModel
        mViewModel?.addLoadingDialog(this)
        createDialog(activity)
        showTask()
    }

    /**
     * Activity旋转，需要重新创建，否则ViewModel会一直持有旧Dialog实例-->Activity的引用，
     * 导致内存泄露，如果一段时间后调用dismiss，会闪退，所以应该发现旋转时马上dismiss
     */
    internal fun recreate(activity: BasicActivity) {
        if (mCanceled && mLoadingType == LoadingType.VISIBLE_ALONE) {
            cancelTaskAndDismiss()
            return
        }

        dismiss()
        createDialog(activity)
        showTask()
    }

    /**
     * 确保在主线程初始化对话框
     */
    private fun createDialog(activity: BasicActivity) {
        mActivity = activity

        // 创建自定义样式的Dialog
        mDialog = newLoadingDialog(activity)
        mDialog?.let { dialog ->
            dialog.setCanceledOnTouchOutside(false)
            if (mLoadingType == LoadingType.STICKY) {
                dialog.setCancelable(false)
            } else {
                dialog.setCancelable(true)
            }
            dialog.setOnCancelListener {
                if (mLoadingType != LoadingType.VISIBLE_ALONE) {
                    cancelTaskAndDismiss()
                }
                mCanceled = true
            }
        }
    }

    private fun showTask() {
        if (mTaskJob?.isActive() != true) {
            cancelTaskAndDismiss()
            return
        }
        show()
        if (mTaskJob?.isActive() != true) {
            cancelTaskAndDismiss()
        }
    }

    /**
     * 显示对话框
     */
    fun show() {
        mDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
            onShow()
        }
        if (mDialog == null) {
            cancelTaskAndDismiss()
        }
    }

    /**
     * 仅仅关闭对话框，不销毁任务进程
     */
    fun dismiss() {
        mDialog?.let {
            it.setOnCancelListener(null)
            it.dismiss()
            onDismiss()
        }
        mDialog = null
        mActivity = null
    }

    /**
     * 关闭对话框，同时取消任务
     */
    internal fun cancelTaskAndDismiss() {
        mProgressId?.let { ProgressManager.unregister(it) }
        dismiss()

        mViewModel?.removeLoadingDialog(this)
        mViewModel = null

        mTaskJob?.cancel()
        mTaskJob = null
    }
}
