package com.miekir.mvvm.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * 瀑布流布局均分分割线（两列）
 * 如果发生重新排列，导致分割线没能及时刷新，则考虑使用padding+左右相等的space
 */
public class StaggeredDecoration extends RecyclerView.ItemDecoration {
    private int mSpace;
    private int mHalfSpace;

    public StaggeredDecoration(int space) {
        this.mSpace = space;
        this.mHalfSpace = space / 2;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 让分割线一致
        int position = parent.getChildAdapterPosition(view);
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
        int spanIndex = lp.getSpanIndex();

        if (position == 0 || position == 1) {
            top = mSpace;
        } else {
            top = 0;
        }
        bottom = mSpace;

        if (spanIndex == 0) {
            // 在左侧
            left = mSpace;
            right = mHalfSpace;
        } else {
            // 在右侧
            left = mHalfSpace;
            right = mSpace;
        }

        //view.setPadding(left, top, right, bottom);
//        TextView tv_index = view.findViewById(R.id.tv_index);
//        if (tv_index != null) {
//            tv_index.setText(String.valueOf(position) + " + " + spanIndex);
//        }

        outRect.set(left, top, right, bottom);
    }
}