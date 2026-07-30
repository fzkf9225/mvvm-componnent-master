package io.coderf.arklab.user.impl;

import org.reactivestreams.Publisher;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.api.ConstantsHelper;
import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.userapi.bean.UserInfo;
import io.coderf.arklab.user.api.UserAccountHelper;
import io.coderf.arklab.user.api.UserApiService;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import retrofit2.HttpException;

/**
 * Created by fz on 2020/9/9 14:11
 * describe:请求失败，重试机制，当请求过期时利用Function方法重新请求刷新token方法替换请求token，然后再重新请求
 * 设置3次重试，每次间隔1秒,但仅适用于用户登录过期刷新token和无权限刷新用户菜单时使用
 * <p>
 * 演示 case：多播（{@code share()}）+ Single-Flight，避免多个独立请求并发 401 时各自刷 token 形成竞态。
 * 服务端 refresh_token 通常只能消费一次：先成功者拿到新 token，后发起者仍携带旧 refresh_token 会被判定无效。
 */
@Singleton
public class RetryServiceImpl implements RetryService {
    /**
     * 最大出错重试次数
     */
    private int maxRetries = ConstantsHelper.RETRY_WHEN_MAX_COUNT;
    /**
     * 当前出错重试次数（多请求并发时共享，需同步保护）
     */
    private final AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * Single-Flight 专用锁，仅用于「创建 / 复用 / 清空」{@link #refreshFlight} 指针，不在锁内等待网络。
     */
    private final Object refreshFlightLock = new Object();

    /**
     * 当前正在进行的 refresh Observable。
     * <p>
     * Single-Flight 策略：
     * <ul>
     *   <li>第一个进入的调用创建 refresh 请求并通过 {@code share()} 多播；</li>
     *   <li>后续并发调用直接返回同一 Observable，不再重复打 refresh 接口；</li>
     *   <li>refresh 结束（成功或失败）后在 {@code doOnTerminate} 中清空，允许下一轮 refresh。</li>
     * </ul>
     * 为何不在 synchronized 内等待网络：锁内只做指针交换，网络订阅与等待在锁外由 Rx 调度完成。
     */
    private volatile Observable<UserInfo> refreshFlight;

    @Inject
    UserApiService userApiService;

    @Inject
    public RetryServiceImpl() {
    }

    /**
     * @param maxRetries 最大重试次数
     */
    public RetryServiceImpl(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Observable<?> handleObservableError(Observable<? extends Throwable> observable) {
        LogUtil.logger(ApiRetrofit.TAG, "-----------------RetryService Observable-------------");
        return observable.flatMap(this::handleThrowable);
    }

    @Override
    public Publisher<?> handleFlowableError(Flowable<Throwable> flowable) {
        LogUtil.logger(ApiRetrofit.TAG, "-----------------RetryService Flowable-------------");
        return flowable.flatMap(this::handleFlowableThrowable);
    }

    private ObservableSource<?> handleThrowable(Throwable throwable) throws Exception {
        if (shouldRetry(throwable)) {
            return refresh(userApiService);
        }
        return Observable.error(throwable);
    }

    private Publisher<?> handleFlowableThrowable(Throwable throwable) throws Exception {
        if (shouldRetry(throwable)) {
            return refreshFlow(userApiService);
        }
        return Flowable.error(throwable);
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof BaseException) {
            BaseException baseException = (BaseException) throwable;
            int count = retryCount.incrementAndGet();
            LogUtil.logger(ApiRetrofit.TAG, "第 " + count + " 次重试，" + "baseException：" + baseException);
            boolean isLoginPastOrNoPermission = true; // 改成实际逻辑
            if (count <= maxRetries && isLoginPastOrNoPermission) {
                return true;
            }
        } else if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int count = retryCount.incrementAndGet();
            LogUtil.logger(ApiRetrofit.TAG, "第 " + count + " 次重试，" + "httpException：" + httpException);
            if (count <= maxRetries && 401 == httpException.code()) {
                return true;
            }
        }
        resetRetryState();
        LogUtil.logger(ApiRetrofit.TAG, "不满足重试条件！");
        return false;
    }

    /**
     * Observable 链路的 token 刷新入口，走 Single-Flight 复用逻辑。
     */
    private Observable<UserInfo> refresh(UserApiService userApiService) {
        return acquireSharedRefreshFlight(userApiService);
    }

    /**
     * Flowable 链路的 token 刷新入口，与 {@link #refresh} 共用同一次 Single-Flight，
     * 避免 Observable / Flowable 两套请求各刷一次 token。
     */
    private Flowable<UserInfo> refreshFlow(UserApiService userApiService) {
        return acquireSharedRefreshFlight(userApiService)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    /**
     * Single-Flight 核心：全局同一时刻只允许一次 refresh 网络请求。
     * <pre>
     * 请求A 401 ──┐
     *              ├──► 共享 refreshFlight（share 多播）──► 成功 ──► A 重试原接口
     * 请求B 401 ──┘                                    └──► B 重试原接口
     * </pre>
     */
    private Observable<UserInfo> acquireSharedRefreshFlight(UserApiService userApiService) {
        synchronized (refreshFlightLock) {
            Observable<UserInfo> inFlight = refreshFlight;
            if (inFlight != null) {
                LogUtil.logger(ApiRetrofit.TAG, "refresh single-flight: reuse in-flight refresh");
                return inFlight;
            }

            Observable<UserInfo> flight = doRefreshToken(userApiService)
                    .doOnTerminate(() -> {
                        // refresh 成功或失败后都释放 flight，供下一轮 token 过期时使用
                        synchronized (refreshFlightLock) {
                            refreshFlight = null;
                        }
                    })
                    // 多个 retryWhen 订阅者共享同一次网络请求与写 token 副作用
                    .share();

            refreshFlight = flight;
            LogUtil.logger(ApiRetrofit.TAG, "refresh single-flight: start new refresh");
            return flight;
        }
    }

    /**
     * 真正发起 refresh 网络请求；仅由 {@link #acquireSharedRefreshFlight} 在「新建 flight」时调用一次。
     */
    private Observable<UserInfo> doRefreshToken(UserApiService userApiService) {
        UserAccountHelper.saveLoginPast(false);
        return userApiService.refreshToken(UserAccountHelper.getRefreshToken())
                .flatMap(tokenBean -> {
                    UserAccountHelper.setToken(tokenBean.getAccess_token());
                    UserAccountHelper.setRefreshToken(tokenBean.getRefresh_token());
                    return userApiService.getUserInfo();
                })
                .doOnNext(userInfo -> {
                    UserAccountHelper.saveLoginState(userInfo, true);
                    resetRetryState();
                });
    }

    private void resetRetryState() {
        retryCount.set(0);
    }

    @Override
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetries = maxRetryCount;
    }
}
