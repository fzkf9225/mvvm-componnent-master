package io.coderf.arklab.demo.module;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;

/**
 * ViewModel 作用域仓库 Module。
 * KtDemoPagingRepositoryImpl / DemoFlowPagingRepositoryImpl 已用 {@code @Inject} 构造器，无需再 {@code @Provides}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/13
 */
@Module
@InstallIn(ViewModelComponent.class)
public class AppRepositoryModule {
}
