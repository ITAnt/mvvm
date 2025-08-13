package com.miekir.mvvm.widget.loading

import android.app.Dialog
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.miekir.mvvm.core.view.base.BasicActivity
import com.miekir.mvvm.task.TaskJob
import com.miekir.mvvm.task.progress.ProgressObserver

/**
 * @author 詹子聪
 * @date 2021-11-30 19:47
 */
abstract class TaskLoading: ProgressObserver {
    @JvmField
    var mDialogData: DialogData? = null

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
}
