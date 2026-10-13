package io.coderf.arklab.user.module;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.core.network.TokenRefresher;
import io.coderf.arklab.user.impl.FlowRetryServiceImpl;
import io.coderf.arklab.user.impl.RetryServiceImpl;

import javax.inject.Singleton;

/**
 * 必须绑定为 {@link Singleton}：Single-Flight 依赖进程内唯一实例，
 * 否则各 Repository 拿到不同 RetryService，并发 401 仍会各自刷 token。
 *
 * <p>鉴权刷新统一使用 {@link TokenRefresher}（旧 FlowRetryService 由框架自动适配），
 * 在 {@code ApiRetrofit.Builder#setTokenRefresher}/{@code setFlowRetryService} 时挂到
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

    /**
     * 鉴权刷新统一入口：业务只需实现 TokenRefresher；
     * 旧 Flow 栈由框架自动适配，无需再绑定 FlowRetryService。
     */
    @Binds
    @Singleton
    abstract TokenRefresher bindTokenRefresher(FlowRetryServiceImpl flowRetryServiceImpl);
}
