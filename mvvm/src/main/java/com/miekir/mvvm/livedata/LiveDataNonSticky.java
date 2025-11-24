package com.miekir.mvvm.livedata;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.miekir.mvvm.context.GlobalContext;

/**
 * @author zzc
 * 非粘性的LiveData，适合对性能（效率）要求不高或数据量较小的场景
 * 如果切换到后台期间，发送了多个值，只有在切到前台时，收到最新的一次值
 *
 * 原生LiveData特性（注意，是特性，而不是坑，因为LiveData设计出来就是用于界面数据恢复的，
 * 甚至postValue丢数据都不算坑，因为99%的界面都只需关注最后状态，如果需要所有状态都不丢失，使用setValue且UI可见才能解决）：
 * ① 先发送，后监听，仍然可以收到最后一次数据，最后一次数据表现为粘性；
 * ② 旋转屏幕导致Activity重建时，会导致再次收到最后一次数据，表现为“数据倒灌”；
 * ③ 非及时。多个Activity监听同一个LiveData，onResume的activity能及时接收到所有消息，而在后台的activity只有在onResume后才能收到消息（最后一个消息）
 *
 * 如果真说LiveData的坑，可能postValue丢数据算一个
 *
 * 解决思路：粘性的LiveData只会发送最后一次数据，如果我们对数据进行包裹，
 * 发送的消息增加时间戳，监听建立增加时间戳，发送晚于监听才触发回调，就可以避免落入粘性和“倒灌”的“陷阱”
 *
 * 坑：
 * ① 两个postValue太近会直接导致只接收到第二个value，使用setValue解决；
 * ② observe有延迟，如果observe之后立刻setValue，大概率会无法观察到；（已修复）
 * 如果不是对UI渲染有严格要求，请使用{@link LiveDataInstant}代替
 */
public class LiveDataNonSticky<T> {
    /**
     * 内部真实的LiveData
     */
    private final MutableLiveData<WrapValue<T>> liveData = new MutableLiveData<WrapValue<T>>();

    /**
     * 最后一个发送的值
     */
    private T value;
    private T getValue() {
        return value;
    }

    /**
     * 需要在主线程调用
     * @param value 数据
     */
    @MainThread
    public synchronized void postValue(T value) {
        this.value = value;
        // observe有延迟，如果observe之后立刻setValue，大概率会无法观察到，使用切换线程来保证监听完成
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WrapValue<T> wrapValue = new WrapValue<T>(value);
                liveData.setValue(wrapValue);
            }
        });
    }

    /**
     * 在外部使用者看来，似乎是owner -- observer配对，在内部，实质上是owner -- ObserverWrapper配对，ObserverWrapper持有observer，决定是否触发observer回调
     * @param owner 生命周期
     * @param observer 监听者
     */
    public synchronized void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        WrapObserver<T> wrapObserver = new WrapObserver<>(observer);
        liveData.observe(owner, wrapObserver);
    }

    /**
     * 监听者封装
     */
    static class WrapObserver<T> implements Observer<WrapValue<T>> {
        private final Observer<? super T> observer;
        private final long observeMillis;
        public WrapObserver(Observer<? super T> observer) {
            this.observer = observer;
            this.observeMillis = System.currentTimeMillis();
        }

        @Override
        public void onChanged(WrapValue<T> wrapValue) {
            // 消息不为空，且消息的发送时间晚于监听时间，才触发回调
            if (wrapValue != null && wrapValue.getSendMillis() > observeMillis) {
                observer.onChanged(wrapValue.getT());
            }
        }
    }

    /**
     * 带有时间戳的事件
     * @param <T>
     */
    static class WrapValue<T> {
        private T t;
        private final long sendMillis;

        public WrapValue(T t) {
            this.t = t;
            this.sendMillis = System.currentTimeMillis();
        }

        public long getSendMillis() {
            return sendMillis;
        }

        public T getT() {
            return t;
        }

        public void setT(T t) {
            this.t = t;
        }
    }
}


/*
在UI可见时，调用
liveData.postValue("a");
liveData.setValue("b");
会先收到"b"，后收到"a"

在UI不可见时，调用
liveData.postValue("a");
liveData.setValue("b");
当UI可见，只会收到"a"，因为setValue先执行，之后被postValue更新掉

在UI可见时，调用
liveData.setValue("a");
liveData.setValue("b");
liveData.setValue("c");
liveData.setValue("d");
liveData.setValue("e");
会按顺序收到"a"，"b"，"c"，"d"，"e"

在UI可见时，调用
liveData.postValue("a");
liveData.postValue("b");
liveData.postValue("c");
liveData.postValue("d");
liveData.postValue("e");
只会收到"e"

在UI可见时，调用
liveData.postValue("a");
liveData.postValue("b");
liveData.postValue("c");
liveData.postValue("d");
liveData.setValue("e");
会先收到"e"，后收到"d"

在UI不可见时，调用
liveData.setValue("a");
liveData.setValue("b");
liveData.setValue("c");
liveData.setValue("d");
liveData.setValue("e");
当UI可见之后，只会收到"e"

在UI不可见时，调用
liveData.postValue("a");
liveData.postValue("b");
liveData.postValue("c");
liveData.postValue("d");
liveData.postValue("e");
当UI可见之后，只会收到"e"

在UI不可见时，调用
liveData.setValue("a");
liveData.setValue("b");
liveData.setValue("c");
liveData.setValue("d");
liveData.postValue("e");
当UI可见之后，只会收到"e"

在UI不可见时，调用
liveData.postValue("a");
liveData.postValue("b");
liveData.postValue("c");
liveData.postValue("d");
liveData.setValue("e");
当UI可见之后，只会收到"d"
        ————————————————

版权声明：本文为博主原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

原文链接：https://blog.csdn.net/cpcpcp123/article/details/121960528*/
