package io.coderf.arklab.common.repository;

import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Repository 生命周期契约（订阅清理）。请求 UI 走 {@link io.coderf.arklab.common.inter.RequestUiCallback}，
 * 不持有页面引用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/6/18 14:32
 * @updated 2026/9/12
 */
public interface IRepository {

    void addDisposable(Disposable disposable);

    void addSubscription(Subscription subscription);

    void remove();

    void clear();
}
