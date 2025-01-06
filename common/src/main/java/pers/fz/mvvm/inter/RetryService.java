package pers.fz.mvvm.inter;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;

/**
 * Created by fz on 2023/5/17 10:43
 * describe : 接口请求错误重试接口，需要重写这个实现无感刷新token等作用
 */
public interface RetryService extends Function<Observable<? extends Throwable>, Observable<?>> {
    void setMaxRetryCount(int maxRetryCount);
}
