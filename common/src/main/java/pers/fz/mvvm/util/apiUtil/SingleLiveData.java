package pers.fz.mvvm.util.apiUtil;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * Created by fz on 2023/6/16 10:05
 * describe :
 */
public class SingleLiveData<T> extends MutableLiveData<T> {
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

