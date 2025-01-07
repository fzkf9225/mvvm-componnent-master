package pers.fz.mvvm.inter;


import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;

/**
 * Created by fz on 2025/1/7 9:20
 * describe : 接口请求错误重试接口，需要重写这个实现无感刷新token等作用
 */
public interface RetryFlowService extends Function<Flowable<Throwable>, Publisher<?>> {
    void setMaxRetryCount(int maxRetryCount);
}
