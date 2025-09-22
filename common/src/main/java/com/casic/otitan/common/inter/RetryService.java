package com.casic.otitan.common.inter;


import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

/**
 * Created by fz on 2023/5/17 10:43
 * describe : 接口请求错误重试接口，需要重写这个实现无感刷新token等作用
 */
public interface RetryService{
    /**
     * 处理Observable类型的错误重试
     */
    Observable<?> handleObservableError(Observable<? extends Throwable> observable);

    /**
     * 处理Flowable类型的错误重试
     */
    Publisher<?> handleFlowableError(Flowable<Throwable> flowable);

    void setMaxRetryCount(int maxRetryCount);
}
