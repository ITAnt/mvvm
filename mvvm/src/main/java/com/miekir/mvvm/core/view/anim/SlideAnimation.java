package com.miekir.mvvm.core.view.anim;

import android.app.Activity;

import com.miekir.mvvm.R;

/**
 * 打开时从右侧进入，退出时向右侧退出的动画
 * @author : zzc
 * @date : 2021/9/3 13:57
 */
public class SlideAnimation extends AbstractAnimHandler {

    @Override
    public void enterAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.mvp_slide_right_in, R.anim.mvp_slide_left_out);
        mStartAnimTime = System.currentTimeMillis();
    }

    @Override
    public void exitAnimation(Activity activity) {
        // 如果是startActivity后面紧接着finish，就不要执行finish动画了
        if (System.currentTimeMillis() - mStartAnimTime < PERIOD_START_FINISH) {
            return;
        }
        activity.overridePendingTransition(R.anim.mvp_slide_left_in, R.anim.mvp_slide_right_out);
    }
}

//@android:integer/config_activityShortDur
//@android:integer/config_activityDefaultDur
//or (in Java):
//
//android.R.integer.config_activityShortDur
//android.R.integer.config_activityDefaultDur
//
//From the sources:
//
//<!-- The duration (in milliseconds) of a short animation. -->
//<integer name="config_shortAnimTime">200</integer>
//
//<!-- The duration (in milliseconds) of a medium-length animation. -->
//<integer name="config_mediumAnimTime">400</integer>
//
//<!-- The duration (in milliseconds) of a long animation. -->
//<integer name="config_longAnimTime">500</integer>
//
//<!-- The duration (in milliseconds) of the activity open/close and fragment open/close animations. -->
//<integer name="config_activityShortDur">150</integer>
//<integer name="config_activityDefaultDur">220</integer>