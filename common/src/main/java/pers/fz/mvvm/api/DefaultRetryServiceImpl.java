package pers.fz.mvvm.api;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/5/18 9:48
 * describe :
 */
public class DefaultRetryServiceImpl implements RetryService {
    private final String TAG = DefaultRetryServiceImpl.class.getSimpleName();
    // 可重试次数
    protected int maxRetries = ConstantsHelper.RETRY_WHEN_MAX_COUNT;
    // 当前已重试次数
    protected int currentRetryCount = 0;
    // 重试等待时间
    protected int waitRetryTime = 1000;

    @Inject
    public DefaultRetryServiceImpl() {
    }

    public DefaultRetryServiceImpl(int maxConnectCount) {
        this.maxRetries = maxConnectCount;
    }

    public DefaultRetryServiceImpl(int maxConnectCount, int waitRetryTime) {
        this.maxRetries = maxConnectCount;
        this.waitRetryTime = waitRetryTime;
    }

    @Override
    public Observable<?> apply(Observable<? extends Throwable> throwableObservable) throws Exception {
        // 参数Observable<Throwable>中的泛型 = 上游操作符抛出的异常，可通过该条件来判断异常的类型
        return throwableObservable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
            // 输出异常信息
            LogUtil.e(throwable);
            /**
             * 需求1：根据异常类型选择是否重试
             * 即，当发生的异常 = 网络异常 = IO异常 才选择重试
             */
//                if (throwable instanceof TimeoutException ) {
//                    FLog.d("属于IO异常，需重试");
            LogUtil.show(TAG, "属于网络异常，需重试");
            /**
             * 需求2：限制重试次数
             * 即，当已重试次数 < 设置的重试次数，才选择重试
             */
            if (currentRetryCount < maxRetries) {

                // 记录重试次数
                currentRetryCount++;
                LogUtil.show(TAG, "重试次数 = " + currentRetryCount);

                /**
                 * 需求2：实现重试
                 * 通过返回的Observable发送的事件 = Next事件，从而使得retryWhen（）重订阅，最终实现重试功能
                 *
                 * 需求3：延迟1段时间再重试
                 * 采用delay操作符 = 延迟一段时间发送，以实现重试间隔设置
                 *
                 * 需求4：遇到的异常越多，时间越长
                 * 在delay操作符的等待时间内设置 = 每重试1次，增多延迟重试时间0.5s
                 */
                // 设置等待时间
                LogUtil.show(TAG, "等待时间 =" + waitRetryTime);
                return Observable.just(1).delay(waitRetryTime, TimeUnit.MILLISECONDS);
            } else {
                // 若重试次数已 > 设置重试次数，则不重试
                // 通过发送error来停止重试（可在观察者的onError（）中获取信息）
                return Observable.error(throwable);
            }
        });
    }

    @Override
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetries = maxRetryCount;
    }
}

