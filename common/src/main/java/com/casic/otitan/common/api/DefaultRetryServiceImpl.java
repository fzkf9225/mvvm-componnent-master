package com.casic.otitan.common.api;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import com.casic.otitan.common.inter.RetryService;
import com.casic.otitan.common.utils.log.LogUtil;

/**
 * Created by fz on 2023/5/18 9:48
 * describe :
 */
public class DefaultRetryServiceImpl implements RetryService {
    // 最大可重试次数
    protected int maxRetries = ConstantsHelper.RETRY_WHEN_MAX_COUNT;
    // 当前已重试次数
    protected int currentRetryCount = 0;
    // 重试等待时间(毫秒)
    protected int waitRetryTime = 1000;
    // 每次重试等待时间增量(毫秒)
    protected int waitRetryIncrement = 500;

    @Inject
    public DefaultRetryServiceImpl() {
    }

    public DefaultRetryServiceImpl(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public DefaultRetryServiceImpl(int maxRetries, int waitRetryTime) {
        this.maxRetries = maxRetries;
        this.waitRetryTime = waitRetryTime;
    }

    public DefaultRetryServiceImpl(int maxRetries, int waitRetryTime, int waitRetryIncrement) {
        this.maxRetries = maxRetries;
        this.waitRetryTime = waitRetryTime;
        this.waitRetryIncrement = waitRetryIncrement;
    }

    @Override
    public Observable<?> handleObservableError(Observable<? extends Throwable> throwableObservable) {
        return throwableObservable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
            LogUtil.e(throwable);
            LogUtil.show(ApiRetrofit.TAG, "发生网络异常，判断是否需要重试");

            if (shouldRetry(throwable)) {
                currentRetryCount++;
                int currentWaitTime = calculateWaitTime();
                LogUtil.show(ApiRetrofit.TAG, "准备重试，当前次数: " + currentRetryCount + ", 等待时间: " + currentWaitTime + "ms");
                return Observable.timer(currentWaitTime, TimeUnit.MILLISECONDS);
            }
            return Observable.error(throwable);
        });
    }

    @Override
    public Publisher<?> handleFlowableError(Flowable<Throwable> throwableFlowable) {
        return throwableFlowable.flatMap((Function<Throwable, Publisher<?>>) throwable -> {
            LogUtil.e(throwable);
            LogUtil.show(ApiRetrofit.TAG, "发生网络异常，判断是否需要重试");

            if (shouldRetry(throwable)) {
                currentRetryCount++;
                int currentWaitTime = calculateWaitTime();
                LogUtil.show(ApiRetrofit.TAG, "准备重试，当前次数: " + currentRetryCount + ", 等待时间: " + currentWaitTime + "ms");
                return Flowable.timer(currentWaitTime, TimeUnit.MILLISECONDS);
            }
            return Flowable.error(throwable);
        });
    }

    /**
     * 判断是否应该重试
     */
    protected boolean shouldRetry(Throwable throwable) {
        // 这里可以根据实际需求添加更多的异常类型判断
        // 示例中保持简单逻辑，所有异常都重试
        return currentRetryCount < maxRetries;
    }

    /**
     * 计算当前应该等待的时间
     * 基础等待时间 + (重试次数 * 每次增量)
     */
    protected int calculateWaitTime() {
        return waitRetryTime + (currentRetryCount * waitRetryIncrement);
    }

    @Override
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetries = maxRetryCount;
    }

    public void setWaitRetryTime(int waitRetryTime) {
        this.waitRetryTime = waitRetryTime;
    }

    public void setWaitRetryIncrement(int waitRetryIncrement) {
        this.waitRetryIncrement = waitRetryIncrement;
    }
}

