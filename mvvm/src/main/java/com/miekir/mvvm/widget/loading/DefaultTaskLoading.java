package com.miekir.mvvm.widget.loading;

import android.app.Dialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.miekir.mvvm.R;

/*使用：
val loading = DefaultTaskLoading().apply {
    newLoadingDialog(this@MainActivity).apply {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}
loading.show()
loading.dismiss()
*/
/**
 * @author 詹子聪
 * @date 2021-11-30 19:59
 */
public class DefaultTaskLoading extends TaskLoading {
    private CircleView lv_circle;
    private TextView tv_loading;

    @NonNull
    @Override
    public Dialog newLoadingDialog(@NonNull AppCompatActivity activity) {
        // 首先得到整个View
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_mvp_loading_view, null);
        // 获取整个布局
        LinearLayout dialogLayout = (LinearLayout) dialogView.findViewById(R.id.dialog_view);
        // 页面中的LoadingView
        lv_circle = (CircleView) dialogView.findViewById(R.id.lv_circle);
        // 页面中显示文本
        tv_loading = (TextView) dialogView.findViewById(R.id.tv_loading);
        if (mDialogData != null && !TextUtils.isEmpty(mDialogData.getTitle())) {
            tv_loading.setText(mDialogData.getTitle());
        } else {
            tv_loading.setVisibility(View.GONE);
        }
        Dialog dialog = new Dialog(activity, R.style.LoadingDialog);
        dialog.setContentView(dialogLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        // 必须复制，否则无法弹出弹窗
        mDialog = dialog;
        return dialog;
    }

    @Override
    public void onShow() {
        if (mDialogData != null && !TextUtils.isEmpty(mDialogData.getTitle()) && tv_loading != null) {
            tv_loading.setText(mDialogData.getTitle());
        }

        if (lv_circle != null) {
            lv_circle.startAnim();
        }
    }

    @Override
    public void onDismiss() {
        if (lv_circle != null) {
            lv_circle.stopAnim();
        }
        lv_circle = null;
    }
}
