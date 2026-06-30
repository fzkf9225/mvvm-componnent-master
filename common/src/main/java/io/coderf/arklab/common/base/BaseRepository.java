package io.coderf.arklab.common.base;

import androidx.annotation.Nullable;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.common.utils.common.CollectionUtil;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Repository 基类。
 * <p>
 * <b>请求相关 UI（加载、Toast、onErrorCode）</b>：只通过 {@link #getRequestUi()} 非空引用触发；
 * 由 {@link BaseViewModel#attachRepositoryRequestUi()} 在创建 Repository 后注入，业务侧不要与 {@link #baseView} 混用两套。
 * <p>
 * <b>{@link #baseView}</b>：仍随 {@link IRepository} 存在，表示「当前绑定的页面」，供尚未改造的代码读取；
 * 新代码请勿在子类里对 {@code baseView} 调用 showLoading / onErrorCode，应统一走 {@link RequestUiCallback}。
 */
public abstract class BaseRepository<BV extends BaseView> implements IRepository<BV> {
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected final List<Subscription> subscriptionList = new ArrayList<>();
    protected BV baseView;
    /**
     * 请求错误时重试服务，这个优先级最高，这里的 > ApiRetrofit中设置的retryService（这里的相当于默认配置，大家共用一个retryService）  > 不设置
     * 这个相当于单独的retryService配置只在当前中生效
     */
    protected RetryService retryService;

    /**
     * 由 ViewModel 注入；Repository 内所有 sendRequest / Flow 请求的 UI 反馈只使用此对象（可为 null，例如无 UI 的后台仓库）。
     */
    @Nullable
    private RequestUiCallback requestUi;

    public BaseRepository() {
    }

    public BaseRepository(RetryService retryService) {
        this.retryService = retryService;
    }


    public BaseRepository(BV baseView) {
        this.baseView = baseView;
    }

    public BaseRepository(RetryService retryService, BV baseView) {
        this.retryService = retryService;
        this.baseView = baseView;
    }

    @Override
    public BV getBaseView() {
        return baseView;
    }

    public void setBaseView(BV baseView) {
        this.baseView = baseView;
    }

    /**
     * 由 {@link BaseViewModel} 调用；若自行 new Repository 且需要加载框，须在发起请求前调用一次。
     */
    public void setRequestUi(@Nullable RequestUiCallback requestUi) {
        this.requestUi = requestUi;
    }

    @Nullable
    public RequestUiCallback getRequestUi() {
        return requestUi;
    }

    /**
     * 这个可以不设置，在创建ApiRetrofit的时候设置，ApiRetrofit中的setRetryService事通用的逻辑
     * 这里针对单个需要定制的才需要调用这个方法，
     * @param retryService 重试服务
     */
    public void setRetryService(RetryService retryService) {
        this.retryService = retryService;
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
        subscriptionList.forEach(item -> {
            if (item == subscription) {
                subscriptionList.remove(item);
            }
        });
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
