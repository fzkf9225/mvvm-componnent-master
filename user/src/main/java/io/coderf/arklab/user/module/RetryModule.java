package io.coderf.arklab.user.module;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.common.inter.FlowRetryService;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.user.impl.FlowRetryServiceImpl;
import io.coderf.arklab.user.impl.RetryServiceImpl;

import javax.inject.Singleton;

/**
 * 必须绑定为 {@link Singleton}：Single-Flight 依赖进程内唯一实例，
 * 否则各 Repository 拿到不同 RetryService，并发 401 仍会各自刷 token。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/30 15:24
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public abstract class RetryModule {
    @Binds
    @Singleton
    abstract RetryService bindRetryService(RetryServiceImpl retryServiceImpl);

    @Binds
    @Singleton
    abstract FlowRetryService bindFlowRetryService(FlowRetryServiceImpl flowRetryServiceImpl);
}
