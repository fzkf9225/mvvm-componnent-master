package io.coderf.arklab.common.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.core.request.RequestUi;
import io.coderf.arklab.core.request.RequestUiHost;

/**
 * ViewModel 基类（MVVM）。
 * <p>
 * <b>请求 UI</b>：默认持有 {@link NetworkRequestUiHost}，经 {@link #ensureRepository()} 注入 Repository。
 * 页面由 {@link BaseActivity}/{@link BaseFragment} 调用 {@link NetworkRequestUiBinder#bind} 订阅 LiveData。
 * 不持有 Activity/Fragment，业务导航请用 LiveData / SharedFlow 等状态下发。
 * <p>
 * <b>页面内容状态（可选）</b>：新页面可用 {@code io.coderf.arklab.core.ui.state.UiState} +
 * {@code UiStateHolder} 以单一 StateFlow 表达 Loading/Success/Empty/Error；
 * 与 RequestUi（遮罩 Loading / Toast）正交，旧 LiveData 路径不受影响。
 *
 * @author fz
 * @version 2.1
 * @since 1.0
 * @updated 2026/9/21
 */
public abstract class BaseViewModel<IR extends IRepository> extends BaseViewViewModel {

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
     * 首次创建 Repository 并注入请求 UI；配置变更后可再次调用（仅重新 attach RequestUi，不重建仓库）。
     */
    public void ensureRepository() {
        if (iRepository == null) {
            iRepository = createRepository();
        }
        attachRepositoryRequestUi();
    }

    /**
     * 向 {@link BaseRepository} / {@link RequestUiHost} 注入本 Host（实现 {@link RequestUi}）。
     */
    protected void attachRepositoryRequestUi() {
        RequestUi ui = provideRequestUi();
        if (iRepository instanceof BaseRepository) {
            ((BaseRepository) iRepository).setRequestUi(ui);
        }
        if (iRepository instanceof RequestUiHost) {
            ((RequestUiHost) iRepository).setRequestUi(ui != null ? ui : networkRequestUiHost);
        }
    }

    /**
     * 提供给 Repository 的请求 UI。默认返回 {@link #networkRequestUiHost}。
     * 无 UI 场景可重写为 {@code null} 或自定义实现。
     */
    @Nullable
    protected RequestUi provideRequestUi() {
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
