package io.coderf.arklab.user.module;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.common.inter.FlowRetryService;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.core.request.TokenRefresher;
import io.coderf.arklab.user.impl.FlowRetryServiceImpl;
import io.coderf.arklab.user.impl.RetryServiceImpl;

import javax.inject.Singleton;

/**
 * 必须绑定为 {@link Singleton}：Single-Flight 依赖进程内唯一实例，
 * 否则各 Repository 拿到不同 RetryService，并发 401 仍会各自刷 token。
 *
 * <p>新版网络栈的 TokenRefresher <b>不</b>写入进程全局 Holder，而是在
 * {@code ApiRetrofit.Builder#setFlowRetryService}/{@code setTokenRefresher} 时挂到
 * <b>当前 ApiService 实例</b>上；未配置的其它 ApiService 不会鉴权重试。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/30 15:24
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class RetryModule {

    @Binds
    @Singleton
    abstract RetryService bindRetryService(RetryServiceImpl retryServiceImpl);

    @Binds
    @Singleton
    abstract FlowRetryService bindFlowRetryService(FlowRetryServiceImpl flowRetryServiceImpl);

    /**
     * 可供显式注入或 {@code Builder.setTokenRefresher}；与 {@link FlowRetryService} 同一实现，保证 Single-Flight。
     */
    @Binds
    @Singleton
    abstract TokenRefresher bindTokenRefresher(FlowRetryServiceImpl flowRetryServiceImpl);
}
