package pers.fz.mvvm.api;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/5/18 9:48
 * describe :
 */
public class RetryWhenFlowableException<T> implements Function<Flowable<? extends Throwable>, Flowable<T>> {
    private final String TAG = RetryWhenFlowableException.class.getSimpleName();
    // 可重试次数
    protected int maxConnectCount;
    // 当前已重试次数
    protected int currentRetryCount = 0;
    // 重试等待时间
    protected int waitRetryTime = 2000;
    protected final T t;
    public RetryWhenFlowableException(T t,int maxConnectCount) {
        this.maxConnectCount = maxConnectCount;
        this.t = t;
    }

    public RetryWhenFlowableException(T t,int maxConnectCount, int waitRetryTime) {
        this.maxConnectCount = maxConnectCount;
        this.waitRetryTime = waitRetryTime;
        this.t = t;
    }

    @Override
    public Flowable<T> apply(Flowable<? extends Throwable> flowable) throws Throwable {
        return flowable.flatMap((Function<Throwable, Publisher<T>>) throwable -> {
            // 输出异常信息
            LogUtil.e(TAG,"异常："+throwable);
            /*
             * 需求2：限制重试次数
             * 即，当已重试次数 < 设置的重试次数，才选择重试
             */
            if (currentRetryCount < maxConnectCount) {

                // 记录重试次数
                currentRetryCount++;
                LogUtil.show(TAG, "重试次数 = " + currentRetryCount);

                /*
                 * 需求2：实现重试
                 * 通过返回的Observable发送的事件 = Next事件，从而使得retryWhen（）重订阅，最终实现重试功能
                 * 需求3：延迟1段时间再重试
                 * 采用delay操作符 = 延迟一段时间发送，以实现重试间隔设置
                 * 需求4：遇到的异常越多，时间越长
                 * 在delay操作符的等待时间内设置 = 每重试1次，增多延迟重试时间0.5s
                 */
                // 设置等待时间
                LogUtil.show(TAG, "等待时间 =" + waitRetryTime);
                return Flowable.just(t).delay(waitRetryTime, TimeUnit.MILLISECONDS);
            } else {
                // 若重试次数已 > 设置重试次数，则不重试
                // 通过发送error来停止重试（可在观察者的onError（）中获取信息）
                return Flowable.error(new Throwable("重试次数已超过设置次数 = " + currentRetryCount + "，即 不再重试;" + throwable));
            }
        });
    }
}

