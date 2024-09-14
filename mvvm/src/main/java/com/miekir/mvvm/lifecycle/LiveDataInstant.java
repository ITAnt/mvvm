package com.miekir.mvvm.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.miekir.mvvm.context.GlobalContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zzc
 * 非粘性的LiveData，发送即达
 */
public class LiveDataInstant<T> {
    /**
     * 监听者
     */
    private final List<Observer<? super T>> activityObserverList = new CopyOnWriteArrayList<>();

    /**
     * 最后一个发送的值
     */
    private T value;
    private T getValue() {
        return value;
    }

    /**
     * @param value 数据
     */
    public synchronized void postValue(T value) {
        this.value = value;
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Observer<? super T> observer : activityObserverList) {
                    observer.onChanged(value);
                }
            }
        });
    }

    /**
     * @param owner Activity生命周期
     * @param observer 监听者
     */
    public synchronized void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        LifecycleEventObserver activityEventObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    owner.getLifecycle().removeObserver(this);
                    activityObserverList.remove(observer);
                }
            }
        };
        owner.getLifecycle().addObserver(activityEventObserver);
        activityObserverList.add(observer);
    }

    /**
     * @param observer 永久监听者
     */
    public synchronized void observerForever(@NonNull Observer<? super T> observer) {
        activityObserverList.add(observer);
    }

    /**
     * @param observer 永久监听者
     */
    public synchronized void removeObserver(@NonNull Observer<? super T> observer) {
        activityObserverList.remove(observer);
    }

    /**
     * @return 是否有监听者
     */
    public boolean hasObservers() {
        return activityObserverList.size() > 0;
    }

    /**
     * @param observer 监听者
     * @return 是否存在某个监听者
     */
    public boolean hasObserver(@NonNull Observer<? super T> observer) {
        return activityObserverList.contains(observer);
    }
}
