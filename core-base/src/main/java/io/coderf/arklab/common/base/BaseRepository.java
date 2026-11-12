package io.coderf.arklab.common.base;

import androidx.annotation.Nullable;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.core.request.LoadingCancelPolicy;
import io.coderf.arklab.core.request.RequestUi;
import io.coderf.arklab.core.utils.CollectionUtil;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Repository 基类。
 * <p>
 * 请求相关 UI 只通过 {@link #getRequestUi()} 触发；由 {@link BaseViewModel} 注入 {@link NetworkRequestUiHost}。
 * 不持有页面引用。旧栈鉴权重试挂在 {@code ApiRetrofit.Builder#setRetryService}，按 ApiService 实例生效。
 * <p>
 * 与遮罩 Loading 绑定且策略非 {@link LoadingCancelPolicy#None} 的 Rx 订阅，
 * 请额外 {@link #addCancellableDisposable}/{@link #addCancellableSubscription}，
 * 以便 {@link #cancelCancellableRequests()} 响应用户取消。
 *
 * @author fz
 * @version 2.1
 * @since 1.0
 * @updated 2026/11/12
 */
public abstract class BaseRepository implements IRepository {
    /** 页面级 Rx 订阅总表（ViewModel onCleared 时 clear）。 */
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    /** Flowable Subscription 总表。 */
    protected final List<Subscription> subscriptionList = new ArrayList<>();

    /**
     * 可被用户取消 Loading 时一并 dispose 的 Disposable 组。
     * 与 {@link #compositeDisposable} 可同时持有同一 Disposable（二次 dispose 安全）。
     */
    private CompositeDisposable cancellableDisposables = new CompositeDisposable();

    /**
     * 可被用户取消 Loading 时一并 cancel 的 Subscription 组。
     */
    private final List<Subscription> cancellableSubscriptions = new ArrayList<>();

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
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    /**
     * 登记可被用户取消的 Disposable（通常在 LoadingCancelPolicy != None 时调用）。
     *
     * @param disposable 本次请求订阅
     */
    public void addCancellableDisposable(@Nullable Disposable disposable) {
        if (disposable == null) {
            return;
        }
        if (cancellableDisposables == null || cancellableDisposables.isDisposed()) {
            cancellableDisposables = new CompositeDisposable();
        }
        cancellableDisposables.add(disposable);
    }

    /**
     * 登记可被用户取消的 Flowable Subscription。
     *
     * @param subscription 本次请求订阅
     */
    public void addCancellableSubscription(@Nullable Subscription subscription) {
        if (subscription == null) {
            return;
        }
        cancellableSubscriptions.add(subscription);
    }

    /**
     * 按取消策略决定是否把本次订阅加入可取消组。
     *
     * @param disposable 本次 Disposable
     * @param policy     Loading 取消策略
     */
    public void trackCancellableIfNeeded(
            @Nullable Disposable disposable,
            @Nullable LoadingCancelPolicy policy
    ) {
        if (disposable == null || policy == null || policy == LoadingCancelPolicy.None) {
            return;
        }
        addCancellableDisposable(disposable);
    }

    /**
     * 按取消策略决定是否把本次 Subscription 加入可取消组。
     *
     * @param subscription 本次 Subscription
     * @param policy       Loading 取消策略
     */
    public void trackCancellableIfNeeded(
            @Nullable Subscription subscription,
            @Nullable LoadingCancelPolicy policy
    ) {
        if (subscription == null || policy == null || policy == LoadingCancelPolicy.None) {
            return;
        }
        addCancellableSubscription(subscription);
    }

    /**
     * 取消所有与可取消 Loading 绑定的 Rx 请求。
     * 由 {@link BaseViewModel#cancelUiRequests()} 调用。
     */
    public void cancelCancellableRequests() {
        if (cancellableDisposables != null && !cancellableDisposables.isDisposed()) {
            cancellableDisposables.clear();
        }
        if (CollectionUtil.isNotEmpty(cancellableSubscriptions)) {
            for (Subscription subscription : new ArrayList<>(cancellableSubscriptions)) {
                try {
                    subscription.cancel();
                } catch (Throwable ignored) {
                    // 忽略已取消等情况
                }
            }
            cancellableSubscriptions.clear();
        }
    }

    @Override
    public void remove() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public void removeSubscription(Subscription subscription) {
        subscriptionList.remove(subscription);
        cancellableSubscriptions.remove(subscription);
    }

    @Override
    public void clear() {
        cancelCancellableRequests();
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
