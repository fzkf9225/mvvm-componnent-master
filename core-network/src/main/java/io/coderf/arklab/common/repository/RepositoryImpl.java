package io.coderf.arklab.common.repository;


import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import org.reactivestreams.Subscription;

import io.coderf.arklab.common.api.BaseApiService;
import io.coderf.arklab.common.api.ErrorConsumer;
import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.bean.ApiRequestOptions;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.core.request.LoadingCancelPolicy;
import io.coderf.arklab.core.request.RequestUi;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlinx.coroutines.flow.MutableStateFlow;

/**
 * <p>
 * Rx 存量：Loading / 错误仍经 {@link BaseRepository#getRequestUi()}。
 * 框架已删除 {@code ensureRepository} 自动注入；请改用已过时的
 * {@link io.coderf.arklab.common.base.BaseRxViewModel#subscribeRequest}（UI 由 ViewModel Host 管理），
 * 或自行 {@code setRequestUi(viewModel.getNetworkRequestUiHost())}。
 * <p>
 * <b>迁移提示</b>：新代码请使用 {@link io.coderf.arklab.core.network.BaseNetworkRepository#request}
 * + ViewModel {@code launchRequest} / {@code collectSuccess}。
 *
 * @deprecated 请改用 {@link io.coderf.arklab.core.network.BaseNetworkRepository}
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/21 8:57
 */
@Deprecated
public abstract class RepositoryImpl<API extends BaseApiService> extends BaseRepository {

    protected API apiService;

    public RepositoryImpl() {
        super();
    }

    public RepositoryImpl(API apiService) {
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
        return sendRequest(observable, apiRequestOptions, liveData, null, null, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(observable, apiRequestOptions, null, stateFlow, null, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, apiRequestOptions, null, null, consumer, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(observable, ApiRequestOptions.getDefault(), liveData, null, null, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(observable, ApiRequestOptions.getDefault(), null, stateFlow, null, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, ApiRequestOptions.getDefault(), null, null, consumer, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, MutableStateFlow<T> stateFlow, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (consumer == null && liveData != null) {
            return sendRequest(observable, apiRequestOptions)
                    .subscribe(liveData::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        } else if (consumer == null && stateFlow != null) {

            return sendRequest(observable, apiRequestOptions)
                    .subscribe(stateFlow::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        } else {
            return sendRequest(observable, apiRequestOptions)
                    .subscribe(consumer == null ? t -> {

                            } : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        }
    }

    public <T> Observable<T> sendRequest(Observable<T> observable, ApiRequestOptions apiRequestOptions) {
        RetryService retryService = resolveRetryService();
        if (retryService != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(retryService::handleObservableError)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> onRxSubscribe(disposable, apiRequestOptions))
                    .doFinally(() -> onRxFinally(apiRequestOptions));
        } else {
            return observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> onRxSubscribe(disposable, apiRequestOptions))
                    .doFinally(() -> onRxFinally(apiRequestOptions));
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
        return sendRequest(flowable, apiRequestOptions, liveData, null, null, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(flowable, apiRequestOptions, null, stateFlow, null, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer) {
        return sendRequest(flowable, apiRequestOptions, null, null, consumer, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(flowable, ApiRequestOptions.getDefault(), liveData, null, null, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(flowable, ApiRequestOptions.getDefault(), null, stateFlow, null, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, @NotNull Consumer<T> consumer) {
        return sendRequest(flowable, ApiRequestOptions.getDefault(), null, consumer, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(flowable, apiRequestOptions)
                .subscribe(consumer == null ? (liveData::setValue) : consumer,
                        throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, MutableStateFlow<T> stateFlow, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (consumer == null && liveData != null) {
            return sendRequest(flowable, apiRequestOptions)
                    .subscribe(liveData::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        } else if (consumer != null && stateFlow != null) {
            return sendRequest(flowable, apiRequestOptions)
                    .subscribe(stateFlow::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        } else {
            return sendRequest(flowable, apiRequestOptions)
                    .subscribe(consumer == null ? t -> {

                            } : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        }
    }

    public <T> Flowable<T> sendRequest(Flowable<T> flowable, ApiRequestOptions apiRequestOptions) {
        RetryService retryService = resolveRetryService();
        if (retryService != null) {
            return flowable.subscribeOn(Schedulers.io())
                    .retryWhen(throwableObservable ->
                            retryService.handleFlowableError(throwableObservable.cast(Throwable.class)))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(subscription -> onFlowableSubscribe(subscription, apiRequestOptions))
                    .doFinally(() -> onRxFinally(apiRequestOptions));
        } else {
            return flowable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(subscription -> onFlowableSubscribe(subscription, apiRequestOptions))
                    .doFinally(() -> onRxFinally(apiRequestOptions));
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
        return sendRequest(single, apiRequestOptions, liveData, null, null, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(single, apiRequestOptions, null, stateFlow, null, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, @NotNull Consumer<T> consumer) {
        return sendRequest(single, apiRequestOptions, null, null, consumer, new ErrorConsumer(getRequestUi(), apiRequestOptions));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(single, ApiRequestOptions.getDefault(), liveData, null, null, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull MutableStateFlow<T> stateFlow) {
        return sendRequest(single, ApiRequestOptions.getDefault(), null, stateFlow, null, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Single<T> single, @NotNull Consumer<T> consumer) {
        return sendRequest(single, ApiRequestOptions.getDefault(), null, null, consumer, new ErrorConsumer(getRequestUi(), ApiRequestOptions.getDefault()));
    }

    public <T> Disposable sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions, MutableLiveData<T> liveData, MutableStateFlow<T> stateFlow, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (consumer == null && liveData != null) {
            return sendRequest(single, apiRequestOptions)
                    .subscribe(liveData::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        } else if (consumer == null && stateFlow != null) {
            return sendRequest(single, apiRequestOptions)
                    .subscribe(stateFlow::setValue,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        } else {
            return sendRequest(single, apiRequestOptions)
                    .subscribe(consumer == null ? t -> {

                            } : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(getRequestUi(), apiRequestOptions));
        }
    }


    public <T> Single<T> sendRequest(Single<T> single, ApiRequestOptions apiRequestOptions) {
        RetryService retryService = resolveRetryService();
        if (retryService != null) {
            return single.subscribeOn(Schedulers.io())
                    .retryWhen(throwableObservable ->
                            retryService.handleFlowableError(throwableObservable.cast(Throwable.class)))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> onRxSubscribe(disposable, apiRequestOptions))
                    .doFinally(() -> onRxFinally(apiRequestOptions));
        } else {
            return single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> onRxSubscribe(disposable, apiRequestOptions))
                    .doFinally(() -> onRxFinally(apiRequestOptions));
        }
    }

    /**
     * Observable / Single 订阅时：登记 Disposable、按策略登记可取消组、展示 Loading。
     */
    private void onRxSubscribe(@Nullable Disposable disposable, @Nullable ApiRequestOptions options) {
        if (disposable != null) {
            addDisposable(disposable);
        }
        ApiRequestOptions opts = options != null ? options : ApiRequestOptions.getDefault();
        LoadingCancelPolicy policy = opts.getLoadingCancelPolicy();
        if (opts.isShowDialog()) {
            trackCancellableIfNeeded(disposable, policy);
            RequestUi ui = getRequestUi();
            if (ui != null) {
                ui.showLoading(
                        opts.getDialogMessage(),
                        opts.isEnableDynamicEllipsis(),
                        policy
                );
            }
        }
    }

    /**
     * Flowable 订阅时：登记 Subscription、按策略登记可取消组、展示 Loading。
     */
    private void onFlowableSubscribe(@Nullable Subscription subscription, @Nullable ApiRequestOptions options) {
        if (subscription != null) {
            addSubscription(subscription);
        }
        ApiRequestOptions opts = options != null ? options : ApiRequestOptions.getDefault();
        LoadingCancelPolicy policy = opts.getLoadingCancelPolicy();
        if (opts.isShowDialog()) {
            trackCancellableIfNeeded(subscription, policy);
            RequestUi ui = getRequestUi();
            if (ui != null) {
                ui.showLoading(
                        opts.getDialogMessage(),
                        opts.isEnableDynamicEllipsis(),
                        policy
                );
            }
        }
    }

    /**
     * 请求结束（成功 / 失败 / dispose）时按引用计数隐藏 Loading。
     */
    private void onRxFinally(@Nullable ApiRequestOptions options) {
        ApiRequestOptions opts = options != null ? options : ApiRequestOptions.getDefault();
        if (!opts.isShowDialog()) {
            return;
        }
        RequestUi ui = getRequestUi();
        if (ui != null) {
            ui.hideLoading();
        }
    }

    /**
     * 旧栈鉴权重试只认当前 ApiService 在 Module 里挂到 Builder 上的配置。
     */
    @Nullable
    private RetryService resolveRetryService() {
        if (apiService == null || apiService.getRetrofit() == null) {
            return null;
        }
        return apiService.getRetrofit().getBuilder().getRetryService();
    }

}
