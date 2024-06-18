package pers.fz.mvvm.repository;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by fz on 2024/6/18 14:32
 * describe :
 */
public interface IRepository {

    void addDisposable(Disposable disposable);

    void removeDisposable();

}
