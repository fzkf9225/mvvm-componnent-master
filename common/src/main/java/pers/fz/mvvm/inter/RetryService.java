package pers.fz.mvvm.inter;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;

/**
 * Created by fz on 2023/5/17 10:43
 * describe :
 */
public interface RetryService extends Function<Observable<? extends Throwable>, Observable<?>> {
    void setMaxRetryCount(int maxRetryCount);
}
