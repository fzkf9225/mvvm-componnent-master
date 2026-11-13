package io.coderf.arklab.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;

import io.coderf.arklab.common.base.BaseViewModel;

/**
 * HomeFragmentViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/4/27 14:58
 */
public class HomeFragmentViewModel extends BaseViewModel implements DefaultLifecycleObserver {

    public HomeFragmentViewModel(@NonNull Application application) {
        super(application);
    }

}
