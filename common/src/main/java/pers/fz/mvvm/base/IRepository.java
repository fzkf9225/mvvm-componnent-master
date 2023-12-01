package pers.fz.mvvm.base;

import androidx.lifecycle.MutableLiveData;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.bean.RequestConfigEntity;

/**
 * Created by fz on 2023/12/1 8:54
 * describe :
 */
public interface IRepository {

    <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer);
    <T> Observable<T> sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity);

    void addDisposable(Disposable disposable);

    void removeDisposable();
    <BV extends BaseView> void setBaseView(BV baseView);
    Function<Observable<? extends Throwable>, Observable<?>> retryWhen();
}
