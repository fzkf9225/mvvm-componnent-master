package pers.fz.mvvm.util.media;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * Created by fz on 2023/4/25 19:24
 * describe :
 */
public class MediaLifecycleObserver implements DefaultLifecycleObserver {
    private Context mContext;
    public MediaLifecycleObserver(Context mContext){
        this.mContext = mContext;
    }
    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
    }
}
