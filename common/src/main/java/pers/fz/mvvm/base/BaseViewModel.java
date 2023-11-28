package pers.fz.mvvm.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonParseException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.api.RetryWhenNetworkException;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.util.log.LogUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.HttpException;

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:baseViewMode封装
 */
public class BaseViewModel<V extends BaseView> extends AndroidViewModel {
    protected final String TAG = this.getClass().getSimpleName();

    //离开页面，是否取消网络
    private CompositeDisposable compositeDisposable;
    /**
     * 如果开启，同一url还在请求网络时，不会
     */
    public ArrayList<String> onNetTags;

    private String dialogMessage = "正在加载，请稍后...";
    protected V baseView;
    @Inject
    public RetryService retryService;

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    public void setBaseView(V baseView) {
        this.baseView = baseView;
    }

    public V getBaseView() {
        return baseView;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        removeDisposable();
    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
    }

    private void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    private void removeDisposable() {
        if (compositeDisposable != null && compositeDisposable.size() > 0) {
            compositeDisposable.dispose();
        }
    }

    //把统一操作全部放在这，不会重连
    protected <T> Disposable observe(Observable<T> observable, boolean isShowDialog, final MutableLiveData<T> liveData) {
        return observe(observable, isShowDialog, true, null, null, liveData);
    }

    //把统一操作全部放在这，不会重连
    protected <T> Disposable observe(Observable<T> observable, final MutableLiveData<T> liveData) {
        return observe(observable, true, true, null, null, liveData);
    }

    //把统一操作全部放在这，不会重连
    protected <T> Disposable observe(Observable<T> observable, Object requestParams, final MutableLiveData<T> liveData) {
        return observe(observable, true, true, null, requestParams, liveData);
    }

    //把统一操作全部放在这，不会重连
    protected <T> Disposable observe(Observable<T> observable, Object requestParams, Consumer<T> consumer) {
        return observe(observable, true, true, null, requestParams, consumer);
    }

    //把统一操作全部放在这，不会重连
    protected <T> Disposable observe(Observable<T> observable, final MutableLiveData<T> liveData, String toastMsg) {
        return observe(observable, true, true, toastMsg, null, liveData);
    }

    protected <T> Disposable observe(Observable<T> observable, boolean isShowDialog, boolean isShowToast, String toastMsg, Object requestParams, @NotNull final MutableLiveData<T> liveData) {
        return observe(observable, isShowDialog, isShowToast, toastMsg, requestParams, liveData, null);
    }

    protected <T> Disposable observe(Observable<T> observable, boolean isShowDialog, boolean isShowToast, String toastMsg, Object requestParams, @NotNull Consumer<T> consumer) {
        return observe(observable, isShowDialog, isShowToast, toastMsg, requestParams, null, consumer);
    }

    /**
     * 请求
     *
     * @param observable    请求被观察者
     * @param isShowDialog  是否展示请求dialog
     * @param isShowToast   是否展示错误文字提示
     * @param toastMsg      错误文字提示内容
     * @param requestParams 请求参数或请求的tag标记等，为了再错误回调的时候根据这个参数判断他的请求数据或那一条请求
     * @param liveData      返回成功数据
     * @param <T>           返回成功数据泛型
     * @return Disposable
     */
    protected <T> Disposable observe(Observable<T> observable, boolean isShowDialog, boolean isShowToast, String toastMsg, Object requestParams,
                                     final MutableLiveData<T> liveData, Consumer<T> consumer) {
        return observable.subscribeOn(Schedulers.io())
                .retryWhen(getRetryWhen())
                .doOnSubscribe(disposable -> {
                    addDisposable(disposable);
                    if (baseView != null && isShowDialog) {
                        baseView.showLoading(dialogMessage);
                    }
                })
                .doFinally(() -> {
                    if (baseView != null && isShowDialog) {
                        baseView.hideLoading();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer == null ? (liveData::setValue) : consumer, e -> {
                    LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|系统异常: " + e);

                    if (baseView != null && isShowDialog) {
                        baseView.hideLoading();
                    }
                    BaseException be = null;

                    if (e != null) {
                        if (e instanceof BaseException) {
                            be = (BaseException) e;
                            //回调到view层 处理 或者根据项目情况处理
                            if (baseView != null) {
                                if (isShowToast) {
                                    if (!TextUtils.isEmpty(toastMsg)) {
                                        baseView.showToast(toastMsg);
                                    } else {
                                        baseView.showToast(be.getErrorMsg());
                                    }
                                }
                                baseView.onErrorCode(new BaseModelEntity<>(be.getErrorCode(), be.getErrorMsg(), requestParams));
                                return;
                            }
                        } else {
                            if (e instanceof HttpException) {
                                //   HTTP错误
                                be = new BaseException(e.getMessage(), e, BaseException.BAD_NETWORK);
                            } else if (e instanceof ConnectException
                                    || e instanceof UnknownHostException) {
                                //   连接错误
                                be = new BaseException(BaseException.CONNECT_ERROR_MSG, e, BaseException.CONNECT_ERROR);
                            } else if (e instanceof InterruptedIOException) {
                                //  连接超时
                                be = new BaseException(BaseException.CONNECT_TIMEOUT_MSG, e, BaseException.CONNECT_TIMEOUT);
                            } else if (e instanceof JsonParseException
                                    || e instanceof JSONException
                                    || e instanceof ParseException) {
                                //  解析错误
                                be = new BaseException(BaseException.PARSE_ERROR_MSG, e, BaseException.PARSE_ERROR);
                            } else {
                                be = new BaseException(e.getMessage(), e, BaseException.OTHER);
                            }
                        }
                    } else {
                        be = new BaseException(BaseException.OTHER_MSG, BaseException.OTHER);
                    }
                    LogUtil.show(ApiRetrofit.TAG, "BaseViewModel|异常消息: " + be.getErrorMsg());
                    if (baseView != null) {
                        baseView.onErrorCode(new BaseModelEntity<>(be.getErrorCode(), be.getErrorMsg(), requestParams));
                        if (isShowToast) {
                            if (!TextUtils.isEmpty(toastMsg)) {
                                baseView.showToast(toastMsg);
                            } else {
                                baseView.showToast(be.getErrorMsg());
                            }
                        }
                    }
                });
    }

    public Function<Observable<? extends Throwable>, Observable<?>> getRetryWhen() {
        return retryService == null ? new RetryWhenNetworkException(ConstantsHelper.RETRY_WHEN_MAX_COUNT) : retryService;
    }

    public void startActivity(Context context, Class<?> toClx, Bundle bundle) {
        Intent intent = new Intent(context, toClx);
        ContextCompat.startActivity(context, intent, bundle);
    }

    public void startActivity(Context context, Class<?> toClx) {
        startActivity(context, toClx, null);
    }
}
