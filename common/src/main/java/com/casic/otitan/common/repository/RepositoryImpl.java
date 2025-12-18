package com.casic.otitan.common.repository;


import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlinx.coroutines.flow.MutableStateFlow;

import com.casic.otitan.common.api.BaseApiService;
import com.casic.otitan.common.api.ErrorConsumer;
import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.bean.ApiRequestOptions;
import com.casic.otitan.common.inter.RetryService;

/**
 * Created by fz on 2023/12/1 10:19
 * describe :
 */
public abstract class RepositoryImpl<API extends BaseApiService, BV extends BaseView> extends BaseRepository<BV> {

    protected API apiService;

    public RepositoryImpl() {
        super();
    }

    public RepositoryImpl(RetryService retryService) {
        super(retryService);
    }

    public RepositoryImpl(BV baseView) {
        super(baseView);
    }

    public RepositoryImpl(RetryService retryService, BV baseView) {
        super(retryService, baseView);
    }

    public RepositoryImpl(API apiService) {
        this.apiService = apiService;
    }

    public RepositoryImpl(BV baseView, API apiService) {
        super(baseView);
        this.apiService = apiService;
    }

    public RepositoryImpl(RetryService retryService, API apiService) {
        super(retryService);
        this.apiService = apiService;
    }

    public RepositoryImpl(RetryService retryService, BV baseView, API apiService) {
        super(retryService, baseView);
        this.apiService = apiService;
    }

    public void setApiService(API apiService) {
        this.apiService = apiService;
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull MutableLiveData<T> liveData, Consumer<Throwable> throwableConsumer) {
        return sendRequest(observable, apiRequestOptions, liveData, null, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow, Consumer<Throwable> throwableConsumer) {
        return sendRequest(observable, apiRequestOptions, null, stateFlow, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(observable, apiRequestOptions, null, null, consumer, throwableConsumer);
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(observable, apiRequestOptions, liveData, null, null, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(observable, apiRequestOptions, null, stateFlow, null, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, apiRequestOptions, null, null, consumer, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(observable, ApiRequestOptions.getDefault(), liveData, null, null, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(observable, ApiRequestOptions.getDefault(), null, stateFlow, null, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, ApiRequestOptions.getDefault(), null, null, consumer, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, MutableStateFlow<T> stateFlow, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (consumer == null && liveData != null) {
            return sendRequest(observable, apiRequestOptions)
                    .subscribe(liveData::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        } else if (consumer == null && stateFlow != null) {

            return sendRequest(observable, apiRequestOptions)
                    .subscribe(stateFlow::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        } else {
            return sendRequest(observable, apiRequestOptions)
                    .subscribe(consumer == null ? t -> {

                            } : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        }
    }

    public <T> Observable<T> sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions) {
        if (retryService != null || apiService.getRetrofit().getBuilder().getRetryService() != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(throwableObservable -> retryService != null ?
                            retryService.handleObservableError(throwableObservable) :
                            apiService.getRetrofit().getBuilder().getRetryService().handleObservableError(throwableObservable))
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.showLoading(apiRequestOptions.getDialogMessage(), apiRequestOptions.isEnableDynamicEllipsis());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return observable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.showLoading(apiRequestOptions.getDialogMessage(), apiRequestOptions.isEnableDynamicEllipsis());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull MutableLiveData<T> liveData, Consumer<Throwable> throwableConsumer) {
        return sendRequest(flowable, apiRequestOptions, liveData, null, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow, Consumer<Throwable> throwableConsumer) {
        return sendRequest(flowable, apiRequestOptions, null, stateFlow, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(flowable, apiRequestOptions, null, null, consumer, throwableConsumer);
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(flowable, apiRequestOptions, liveData, null, null, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(flowable, apiRequestOptions, null, stateFlow, null, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer) {
        return sendRequest(flowable, apiRequestOptions, null, null, consumer, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(flowable, ApiRequestOptions.getDefault(), liveData, null, null, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(flowable, ApiRequestOptions.getDefault(), null, stateFlow, null, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull Consumer<T> consumer) {
        return sendRequest(flowable, ApiRequestOptions.getDefault(), null, consumer, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(flowable, apiRequestOptions)
                .subscribe(consumer == null ? (liveData::setValue) : consumer,
                        throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, MutableStateFlow<T> stateFlow, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (consumer == null && liveData != null) {
            return sendRequest(flowable, apiRequestOptions)
                    .subscribe(liveData::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        } else if (consumer != null && stateFlow != null) {
            return sendRequest(flowable, apiRequestOptions)
                    .subscribe(stateFlow::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        } else {
            return sendRequest(flowable, apiRequestOptions)
                    .subscribe(consumer == null ? t -> {

                            } : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        }
    }

    public <T> Flowable<T> sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions) {
        if (retryService != null || apiService.getRetrofit().getBuilder().getRetryService() != null) {
            return flowable.subscribeOn(Schedulers.io())
                    .retryWhen(throwableObservable ->
                            retryService != null ?
                                    retryService.handleFlowableError(throwableObservable.cast(Throwable.class)) :
                                    apiService.getRetrofit().getBuilder().getRetryService().handleFlowableError(throwableObservable.cast(Throwable.class)))
                    .doOnSubscribe(disposable -> {
                        addSubscription(disposable);
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.showLoading(apiRequestOptions.getDialogMessage(), apiRequestOptions.isEnableDynamicEllipsis());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return flowable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addSubscription(disposable);
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.showLoading(apiRequestOptions.getDialogMessage(), apiRequestOptions.isEnableDynamicEllipsis());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }


    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull MutableLiveData<T> liveData, Consumer<Throwable> throwableConsumer) {
        return sendRequest(single, apiRequestOptions, liveData, null, null, throwableConsumer);
    }


    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow, Consumer<Throwable> throwableConsumer) {
        return sendRequest(single, apiRequestOptions, null, stateFlow, null, throwableConsumer);
    }


    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(single, apiRequestOptions, null, null, consumer, throwableConsumer);
    }

    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(single, apiRequestOptions, liveData, null, null, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(single, apiRequestOptions, null, stateFlow, null, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer) {
        return sendRequest(single, apiRequestOptions, null, null, consumer, new ErrorConsumer(baseView, apiRequestOptions));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(single, ApiRequestOptions.getDefault(), liveData, null, null, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(single, ApiRequestOptions.getDefault(), null, stateFlow, null, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull Consumer<T> consumer) {
        return sendRequest(single, ApiRequestOptions.getDefault(), null, null, consumer, new ErrorConsumer(baseView, ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, MutableStateFlow<T> stateFlow, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (consumer == null && liveData != null) {
            return sendRequest(single, apiRequestOptions)
                    .subscribe(liveData::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        } else if (consumer == null && stateFlow != null) {
            return sendRequest(single, apiRequestOptions)
                    .subscribe(stateFlow::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        } else {
            return sendRequest(single, apiRequestOptions)
                    .subscribe(consumer == null ? t -> {

                            } : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, apiRequestOptions));
        }
    }


    public <T> Single<T> sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions) {
        if (retryService != null || apiService.getRetrofit().getBuilder().getRetryService() != null) {
            return single.subscribeOn(Schedulers.io())
                    .retryWhen(throwableObservable ->
                            retryService != null ?
                                    retryService.handleFlowableError(throwableObservable.cast(Throwable.class)) :
                                    apiService.getRetrofit().getBuilder().getRetryService().handleFlowableError(throwableObservable.cast(Throwable.class)))
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.showLoading(apiRequestOptions.getDialogMessage(), apiRequestOptions.isEnableDynamicEllipsis());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return single.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.showLoading(apiRequestOptions.getDialogMessage(), apiRequestOptions.isEnableDynamicEllipsis());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && apiRequestOptions.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

}
