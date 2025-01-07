package pers.fz.mvvm.repository;


import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.api.ErrorConsumer;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.inter.RetryFlowService;
import pers.fz.mvvm.inter.RetryService;

/**
 * Created by fz on 2023/12/1 10:19
 * describe :
 */
public class RepositoryImpl<BV extends BaseView> implements IRepository {
    /**
     * 离开页面，是否取消网络
     */
    private CompositeDisposable compositeDisposable;

    protected BV baseView;

    private RetryService retryService;

    private RetryFlowService retryFlowService;

    private final List<Subscription> subscriptionList = new ArrayList<Subscription>();

    private void addSubscription(Subscription subscription) {
        subscriptionList.add(subscription);
    }

    private void removeSubscription(Subscription subscription) {
        subscriptionList.forEach(item -> {
            if (item == subscription) {
                subscriptionList.remove(item);
            }
        });
    }

    private void clearSubscription() {
        subscriptionList.forEach(Subscription::cancel);
    }

    public RepositoryImpl(RetryService retryService, BV baseView) {
        this.retryService = retryService;
        this.baseView = baseView;
    }

    public RepositoryImpl(RetryFlowService retryFlowService, BV baseView) {
        this.retryFlowService = retryFlowService;
        this.baseView = baseView;
    }

    public RepositoryImpl(BV baseView) {
        this.baseView = baseView;
    }

    public void setRetryService(RetryService retryService) {
        this.retryService = retryService;
    }

    public void setRetryFlowService(RetryFlowService retryFlowService) {
        this.retryFlowService = retryFlowService;
    }

    @Override
    public void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void removeDisposable() {
        if (compositeDisposable != null && compositeDisposable.size() > 0) {
            //默认取消所有订阅，但不会导致正在进行的任务终止，而是等待它们完成，仅仅只是取消订阅关系而已
            compositeDisposable.clear();
            //默认取消所有订阅，并取消所有正在进行的任务
//            compositeDisposable.dispose();
        }

        if (!subscriptionList.isEmpty()) {
            clearSubscription();
        }
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (retryService != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(retryService)
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        } else {
            return observable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        }
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData, Consumer<Throwable> throwableConsumer) {
        return sendRequest(observable, requestConfigEntity, liveData, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(observable, requestConfigEntity, null, consumer, throwableConsumer);
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(observable, requestConfigEntity, liveData, null, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, requestConfigEntity, null, consumer, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(observable, RequestConfigEntity.getDefault(), liveData, null, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, RequestConfigEntity.getDefault(), null, consumer, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Observable<T> sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity) {
        if (retryService != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(retryService)
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return observable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public <T> Disposable sendRequest(Flowable<T> flowable, RequestConfigEntity requestConfigEntity, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (retryFlowService != null) {
            return flowable.subscribeOn(Schedulers.io())
                    .retryWhen(retryFlowService)
                    .doOnSubscribe(disposable -> {
                        addSubscription(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        } else {
            return flowable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addSubscription(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        }
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData, Consumer<Throwable> throwableConsumer) {
        return sendRequest(flowable, requestConfigEntity, liveData, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(flowable, requestConfigEntity, null, consumer, throwableConsumer);
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(flowable, requestConfigEntity, liveData, null, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer) {
        return sendRequest(flowable, requestConfigEntity, null, consumer, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(flowable, RequestConfigEntity.getDefault(), liveData, null, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull Consumer<T> consumer) {
        return sendRequest(flowable, RequestConfigEntity.getDefault(), null, consumer, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Flowable<T> sendRequest(Flowable<T> flowable, RequestConfigEntity requestConfigEntity) {
        if (retryFlowService != null) {
            return flowable.subscribeOn(Schedulers.io())
                    .retryWhen(retryFlowService)
                    .doOnSubscribe(disposable -> {
                        addSubscription(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return flowable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addSubscription(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public <T> Disposable sendRequest(Single<T> single, RequestConfigEntity requestConfigEntity, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (retryFlowService != null) {
            return single.subscribeOn(Schedulers.io())
                    .retryWhen(retryFlowService)
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        } else {
            return single.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        }
    }

    public <T> Disposable sendRequest(Single<T> single, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData, Consumer<Throwable> throwableConsumer) {
        return sendRequest(single, requestConfigEntity, liveData, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Single<T> single, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(single, requestConfigEntity, null, consumer, throwableConsumer);
    }

    public <T> Disposable sendRequest(Single<T> single, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(single, requestConfigEntity, liveData, null, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Single<T> single, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer) {
        return sendRequest(single, requestConfigEntity, null, consumer, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(single, RequestConfigEntity.getDefault(), liveData, null, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull Consumer<T> consumer) {
        return sendRequest(single, RequestConfigEntity.getDefault(), null, consumer, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Single<T> sendRequest(Single<T> single, RequestConfigEntity requestConfigEntity) {
        if (retryFlowService != null) {
            return single.subscribeOn(Schedulers.io())
                    .retryWhen(retryFlowService)
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return single.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


}
