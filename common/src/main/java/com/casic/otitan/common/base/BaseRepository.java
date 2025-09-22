package com.casic.otitan.common.base;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import com.casic.otitan.common.inter.RetryService;
import com.casic.otitan.common.repository.IRepository;
import com.casic.otitan.common.utils.common.CollectionUtil;

/**
 * created by fz on 2025/6/24 15:38
 * describe:
 */
public abstract class BaseRepository<BV extends BaseView> implements IRepository<BV> {
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected final List<Subscription> subscriptionList = new ArrayList<>();
    protected BV baseView;
    /**
     * 请求错误时重试服务，这个优先级最高，这里的 > ApiRetrofit中设置的retryService（这里的相当于默认配置，大家共用一个retryService）  > 不设置
     * 这个相当于单独的retryService配置只在当前中生效
     */
    protected RetryService retryService;

    public BaseRepository() {
    }

    public BaseRepository(RetryService retryService) {
        this.retryService = retryService;
    }


    public BaseRepository(BV baseView) {
        this.baseView = baseView;
    }

    public BaseRepository(RetryService retryService, BV baseView) {
        this.retryService = retryService;
        this.baseView = baseView;
    }

    @Override
    public BV getBaseView() {
        return baseView;
    }

    public void setBaseView(BV baseView) {
        this.baseView = baseView;
    }

    /**
     * 这个可以不设置，在创建ApiRetrofit的时候设置，ApiRetrofit中的setRetryService事通用的逻辑
     * 这里针对单个需要定制的才需要调用这个方法，
     * @param retryService 重试服务
     */
    public void setRetryService(RetryService retryService) {
        this.retryService = retryService;
    }

    @Override
    public void addSubscription(Subscription subscription) {
        subscriptionList.add(subscription);
    }

    @Override
    public void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void remove() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public void removeSubscription(Subscription subscription) {
        subscriptionList.forEach(item -> {
            if (item == subscription) {
                subscriptionList.remove(item);
            }
        });
    }

    @Override
    public void clear() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        if (CollectionUtil.isNotEmpty(subscriptionList)) {
            subscriptionList.forEach(Subscription::cancel);
            subscriptionList.clear();
        }
    }
}

