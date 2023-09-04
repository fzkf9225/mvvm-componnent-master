package pers.fz.mvvm.livedata;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 避免livedata收到多次数据（在配置更改（如旋转）时、livedata被多个组件共享时）
 * 只有一个观察者会收到更改通知
 * @param <T>
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private int mVersion = 0;
    //被观察者的版本
    private int observerVersion = 0;

    // 观察者的版本
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        //每次订阅的时候，先把版本同步
        observerVersion = mVersion;
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if (mVersion != observerVersion) {
                    observer.onChanged(t);
                }
            }
        });
    }

    @MainThread
    public void setValue(T value) {
        mVersion++;
        super.setValue(value);
    }
}
