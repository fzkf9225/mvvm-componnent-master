package io.coderf.arklab.common.base;

import androidx.annotation.Nullable;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.common.utils.common.CollectionUtil;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Repository 基类。
 * <p>
 * 请求相关 UI 只通过 {@link #getRequestUi()} 触发；由 {@link BaseViewModel} 注入 {@link NetworkRequestUiHost}。
 * 不持有页面引用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/12
 */
public abstract class BaseRepository implements IRepository {
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected final List<Subscription> subscriptionList = new ArrayList<>();
    /**
     * 请求错误时重试服务，优先级高于 ApiRetrofit 默认配置。
     */
    protected RetryService retryService;

    @Nullable
    private RequestUiCallback requestUi;

    public BaseRepository() {
    }

    public BaseRepository(RetryService retryService) {
        this.retryService = retryService;
    }

    public void setRequestUi(@Nullable RequestUiCallback requestUi) {
        this.requestUi = requestUi;
    }

    @Nullable
    public RequestUiCallback getRequestUi() {
        return requestUi;
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
        subscriptionList.remove(subscription);
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
        requestUi = null;
    }
}
