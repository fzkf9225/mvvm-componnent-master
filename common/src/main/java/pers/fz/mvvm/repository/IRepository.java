package pers.fz.mvvm.repository;

import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.base.BaseView;

/**
 * Created by fz on 2024/6/18 14:32
 * describe :
 */
public interface IRepository<BV extends BaseView> {

    void addDisposable(Disposable disposable);

    void addSubscription(Subscription subscription);

    void setBaseView(BV baseView);

    BV getBaseView();

    void remove();

    void clear();
}
