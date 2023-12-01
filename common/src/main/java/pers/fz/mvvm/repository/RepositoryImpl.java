package pers.fz.mvvm.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.Api;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.ErrorConsumer;
import pers.fz.mvvm.base.IRepository;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.inter.RetryService;

/**
 * Created by fz on 2023/12/1 10:19
 * describe :
 */
public class RepositoryImpl implements IRepository {
    /**
     * 离开页面，是否取消网络
     */
    private CompositeDisposable compositeDisposable;

    protected BaseView baseView;

    private RetryService retryService;

    public RepositoryImpl(RetryService retryService) {
        this.retryService = retryService;
    }

    public void setRetryService(RetryService retryService) {
        this.retryService = retryService;
    }

    @Override
    public <BV extends BaseView> void setBaseView(BV baseView) {
        this.baseView = baseView;
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
            compositeDisposable.dispose();
        }
    }

    @Override
    public <T> Disposable sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity, MutableLiveData<T> liveData, Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
        if (retryWhen() != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(retryWhen())
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

    @Override
    public <T> Observable<T> sendRequest(Observable<T> observable, RequestConfigEntity requestConfigEntity) {
        if (retryWhen() != null) {
            return observable.subscribeOn(Schedulers.io())
                    .retryWhen(retryWhen())
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

    @Override
    public Function<Observable<? extends Throwable>, Observable<?>> retryWhen() {
        return retryService;
    }
}
