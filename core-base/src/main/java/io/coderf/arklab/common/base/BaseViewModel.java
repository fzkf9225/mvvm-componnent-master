package io.coderf.arklab.common.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.core.request.RequestUi;
import io.coderf.arklab.core.request.RequestUiHost;

/**
 * Create by fz on 2020/3/19 0019
 * describe:baseViewMode封装
 * <p>
 * <b>请求 UI（长期方案）</b>：默认持有 {@link NetworkRequestUiHost}，经
 * {@link #attachRepositoryRequestUi()} 注入旧 {@link BaseRepository} 与新 {@link RequestUiHost}。
 * 页面由 {@link BaseActivity}/{@link BaseFragment} 调用 {@link NetworkRequestUiBinder#bind} 订阅 LiveData，
 * 不再把请求 loading/toast/error 直连到 {@link BaseView}。
 * <p>
 * {@link #baseView} 仍可绑定，仅供遗留非请求 UI；新代码请勿在 Repository 内对 baseView 调 showLoading 等。
 */
public abstract class BaseViewModel<IR extends IRepository<BV>, BV extends BaseView> extends BaseViewViewModel<BV> {

    protected IR iRepository;

    /**
     * 请求 UI 状态宿主，与 ViewModel 同生命周期；页面重建只重新 bind，不重建 Host。
     */
    @NonNull
    private final NetworkRequestUiHost networkRequestUiHost = new NetworkRequestUiHost();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (iRepository != null) {
            iRepository.clear();
        }
    }

    protected abstract IR createRepository();

    /**
     * 绑定当前页面并装配 Repository。
     * <p>
     * 配置变更后 Activity/Fragment 重建、ViewModel 仍存活：须再次调用本方法刷新 {@link #baseView}，
     * 并由页面侧重新 {@link NetworkRequestUiBinder#bind}。Repository 仅首次创建。
     */
    public void createRepository(BV baseView) {
        this.baseView = baseView;
        if (iRepository == null) {
            iRepository = createRepository();
        }
        if (iRepository != null) {
            iRepository.setBaseView(baseView);
        }
        attachRepositoryRequestUi();
    }

    /**
     * 页面销毁时解除对已销毁页面的引用。
     * <p>
     * <b>不</b>清空 Repository 上的 RequestUi：Host 仍存活于 ViewModel，进行中的请求可继续 post 状态；
     * 新页面 bind 后会继续收到后续事件。Repository 与请求的最终清理在 {@link #onCleared()}。
     */
    public void unbindView() {
        this.baseView = null;
        if (iRepository != null) {
            iRepository.setBaseView(null);
        }
        // 保持 networkRequestUiHost 注入，避免页面销毁瞬间 in-flight 请求丢失 UI 通道
    }

    /**
     * 向旧 {@link BaseRepository} 注入 {@link RequestUiCallback}；
     * 向新 {@link RequestUiHost} 直接注入本 Host（其已实现 {@link RequestUi}）。
     */
    protected void attachRepositoryRequestUi() {
        RequestUiCallback callback = provideRequestUiCallback();
        if (iRepository instanceof BaseRepository) {
            ((BaseRepository<?>) iRepository).setRequestUi(callback);
        }
        if (iRepository instanceof RequestUiHost) {
            RequestUi requestUi = (callback instanceof RequestUi)
                    ? (RequestUi) callback
                    : networkRequestUiHost;
            ((RequestUiHost) iRepository).setRequestUi(requestUi);
        }
    }

    /**
     * 提供给 Repository 的 UI 回调。默认返回 {@link #networkRequestUiHost}。
     * 无 UI 场景可重写为 {@code null} 或自定义实现。
     */
    @Nullable
    protected RequestUiCallback provideRequestUiCallback() {
        return networkRequestUiHost;
    }

    /**
     * 请求 UI 状态宿主，供页面 bind 或业务自行 observe。
     */
    @NonNull
    public NetworkRequestUiHost getNetworkRequestUiHost() {
        return networkRequestUiHost;
    }

    public IR getIRepository() {
        return iRepository;
    }

}
