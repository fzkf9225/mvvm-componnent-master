package pers.fz.mvvm.repository;

import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.api.ErrorConsumer;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.inter.RetryService;

/**
 * Created by fz on 2023/12/1 10:19
 * describe :
 */
public class RepositoryImpl<BV extends BaseView> implements IRepository {
    /**
     * 离开页面，是否取消网络
     */
    private CompositeDisposable compositeDisposable;

    protected BV baseView;

    private RetryService retryService;

    public RepositoryImpl(RetryService retryService, BV baseView) {
        this.retryService = retryService;
        this.baseView = baseView;
    }

    public RepositoryImpl(BV baseView) {
        this.baseView = baseView;
    }

    public void setRetryService(RetryService retryService) {
        this.retryService = retryService;
    }

    @Override
    public void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void removeDisposable() {
        if (compositeDisposable != null && compositeDisposable.size() > 0) {
            //默认取消所有订阅，但不会导致正在进行的任务终止，而是等待它们完成，仅仅只是取消订阅关系而已
            compositeDisposable.clear();
            //默认取消所有订阅，并取消所有正在进行的任务
//            compositeDisposable.dispose();
        }
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (retryService != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(retryService)
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        } else {
            return observable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(consumer == null ? (liveData::setValue) : consumer,
                            throwableConsumer != null ? throwableConsumer : new ErrorConsumer(baseView, requestConfigEntity));
        }
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData, Consumer<Throwable> throwableConsumer) {
        return sendRequest(observable, requestConfigEntity, liveData, null, throwableConsumer);
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        return sendRequest(observable, requestConfigEntity, null, consumer, throwableConsumer);
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(observable, requestConfigEntity, liveData, null, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, requestConfigEntity, null, consumer, new ErrorConsumer(baseView, requestConfigEntity));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull MutableLiveData<T> liveData) {
        return sendRequest(observable, RequestConfigEntity.getDefault(), liveData, null, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Disposable sendRequest(Observable<T> observable, @NotNull Consumer<T> consumer) {
        return sendRequest(observable, RequestConfigEntity.getDefault(), null, consumer, new ErrorConsumer(baseView, RequestConfigEntity.getDefault()));
    }

    public <T> Observable<T> sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity) {
        if (retryService != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(retryService)
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return observable.subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> {
                        addDisposable(disposable);
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.showLoading(requestConfigEntity.getDialogMessage());
                        }
                    })
                    .doFinally(() -> {
                        if (baseView != null && requestConfigEntity.isShowDialog()) {
                            baseView.hideLoading();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

}
