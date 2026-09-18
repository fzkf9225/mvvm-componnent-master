package io.coderf.arklab.common.base;

import androidx.annotation.Nullable;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.common.utils.common.CollectionUtil;
import io.coderf.arklab.core.request.RequestUi;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Repository 基类。
 * <p>
 * 请求相关 UI 只通过 {@link #getRequestUi()} 触发；由 {@link BaseViewModel} 注入 {@link NetworkRequestUiHost}。
 * 不持有页面引用。旧栈鉴权重试挂在 {@code ApiRetrofit.Builder#setRetryService}，按 ApiService 实例生效。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/9/18
 */
public abstract class BaseRepository implements IRepository {
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected final List<Subscription> subscriptionList = new ArrayList<>();

    @Nullable
    private RequestUi requestUi;

    public BaseRepository() {
    }

    public void setRequestUi(@Nullable RequestUi requestUi) {
        this.requestUi = requestUi;
    }

    @Nullable
    public RequestUi getRequestUi() {
        return requestUi;
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
