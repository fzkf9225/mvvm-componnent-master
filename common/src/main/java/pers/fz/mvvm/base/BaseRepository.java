package pers.fz.mvvm.base;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.IRepository;
import pers.fz.mvvm.util.common.CollectionUtil;

/**
 * created by fz on 2025/6/24 15:38
 * describe:
 */
public abstract class BaseRepository<BV extends BaseView> implements IRepository<BV> {
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected final List<Subscription> subscriptionList = new ArrayList<>();
    protected BV baseView;
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

