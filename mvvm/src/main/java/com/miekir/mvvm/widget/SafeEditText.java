package com.miekir.mvvm.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * 解决EditText导致的内存泄漏
 * Fragment最好不要写输入框，原因是EditText和Activity会相互持有引用，如果Fragment被销毁，但Activity还没被销毁，
 * 则EditText即使DetachedFromWindow了，仍然停留在内存中，导致内存泄漏，这个bug和手机厂商也有关系，有些厂商的ROM
 * 修复了，有些还是存在这种问题。
 * 做法：
 * 方案①：使用SafeEditText，缺点：侵入性强，必须使用自定义View，无法解决第三方SDK的内存泄漏，贸然使用getApplicationContext
 * 代替Activity的context，可能会导致UI问题，如软键盘无法弹出、字体颜色变化等，且虽然不报错，仍存在内存中，越积越多可能
 * 会导致内存溢出，不推荐。
 * 方案②：Fragment不要使用输入框，缺点：无法实现某些必须需要输入框的业务功能。
 * 方案③：切换Fragment时不销毁Fragment，只是hide 和 show，或设置offscreenPageLimit为总数。
 * 缺点：切换时如果需要重新初始化须手动调用，推荐。
 * 方案④：忽略内存泄漏警告，activity销毁时自然回收，activity一刻不销毁，就一直是泄漏状态。
 * 总结：要么不要在Fragment里用EditText，如果要用，则持有Fragment直到Activity销毁
 */
@SuppressLint({"AppCompatCustomView"})
public class SafeEditText extends EditText {
    public SafeEditText(Context context) {
        super(context.getApplicationContext(), null);
    }

    public SafeEditText(Context context, AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
    }

    public SafeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getApplicationContext(), attrs, defStyleAttr);
    }

    public SafeEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context.getApplicationContext(), attrs, defStyleAttr, defStyleRes);
    }
}