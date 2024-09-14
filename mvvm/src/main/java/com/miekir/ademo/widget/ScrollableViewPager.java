//package com.miekir.mvvm.widget;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//
//import androidx.viewpager.widget.ViewPager;
//
///**
// * @deprecated 已过时，使用ViewPager2代替
// */
//public class ScrollableViewPager extends ViewPager {
//
//    private boolean scrollable = false;
//
//
//    public ScrollableViewPager(Context context) {
//        super(context);
//    }
//
//    public ScrollableViewPager(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if (!scrollable) {
//            return false;
//        }
//        return super.onTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (!scrollable) {
//            return false;
//        }
//        return super.onInterceptTouchEvent(ev);
//    }
//
//    public boolean isScrollable() {
//        return scrollable;
//    }
//
//    public void setScrollable(boolean scrollable) {
//        this.scrollable = scrollable;
//    }
//}