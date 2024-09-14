package com.miekir.mvvm.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.miekir.common.R;

/**
 * 成功失败布局
 * @author : zzc
 * @date : 2021/12/16 10:16
 */
public class SuccessFailLayout extends FrameLayout {

    public SuccessFailLayout(@NonNull Context context) {
        this(context, null);
    }

    public SuccessFailLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // 不能放在post里获取，否则会获取不到
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SuccessFailLayout);
        int failViewLayoutId = ta.getResourceId(R.styleable.SuccessFailLayout_failLayout, -1);
        ta.recycle();

        post(() -> {
            int childCount = getChildCount();
            if (childCount != 1) {
                throw new IllegalStateException("SuccessFailLayout必须有且只有一个成功状态的子View");
            }
            successView = getChildAt(0);

            if (failViewLayoutId != -1) {
                setFailView(failViewLayoutId);
            }
        });
    }

    /**
     * 状态
     */
    public enum Status {
        SUCCESS,
        FAIL,
    }

    private View successView;

    /**
     * 失败布局
     */
    private View failView;

    public View getFailView() {
        return failView;
    }

    public void setFailView(@NonNull View view) {
        if (failView != null) {
            removeView(failView);
        }
        failView = view;
        failView.setVisibility(View.GONE);
        addView(failView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setFailView(@LayoutRes int layoutResId) {
        View view = LayoutInflater.from(getContext()).inflate(layoutResId, null);
        setFailView(view);
    }

    /**
     * 设置布局状态
     * @param status 状态
     */
    public void setStatus(Status status) {
        if (status == Status.SUCCESS) {
            if (successView != null) {
                successView.setVisibility(View.VISIBLE);
            }
            if (failView != null) {
                failView.setVisibility(View.GONE);
            }
        } else {
            if (successView != null) {
                successView.setVisibility(View.GONE);
            }
            if (failView != null) {
                failView.setVisibility(View.VISIBLE);
            }
        }
    }
}
