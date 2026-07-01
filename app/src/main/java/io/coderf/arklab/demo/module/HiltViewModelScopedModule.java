package io.coderf.arklab.demo.module;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import io.coderf.arklab.demo.bean.HiltViewModelScopedToken;

/**
 * ViewModelComponent 作用域演示：绑定对象生命周期与 ViewModel 一致，旋转屏幕后仍保留。
 */
@Module
@InstallIn(ViewModelComponent.class)
public class HiltViewModelScopedModule {

    @Provides
    static HiltViewModelScopedToken provideViewModelScopedToken() {
        return new HiltViewModelScopedToken();
    }
}
