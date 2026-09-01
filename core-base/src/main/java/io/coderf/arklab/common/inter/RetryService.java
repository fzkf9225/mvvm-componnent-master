package io.coderf.arklab.common.inter;


import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

/**
 * 接口请求错误重试接口，需要重写这个实现无感刷新token等作用
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/17 10:43
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
