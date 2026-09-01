package io.coderf.arklab.common.repository;

import org.reactivestreams.Subscription;

import io.coderf.arklab.common.base.BaseView;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * IRepository 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/6/18 14:32
 */
public interface IRepository<BV extends BaseView> {

    void addDisposable(Disposable disposable);

    void addSubscription(Subscription subscription);

    void setBaseView(BV baseView);

    BV getBaseView();

    void remove();

    void clear();
}
