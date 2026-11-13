package io.coderf.arklab.common.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.core.request.RequestUi;
import io.coderf.arklab.core.request.RequestUiHost;
import io.coderf.arklab.core.request.UiRequestTracker;
import kotlinx.coroutines.Job;

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
 * <p>
 * <b>Loading 取消</b>：协程请用 {@link BaseViewModelRequestsKt#launchUiRequest} 启动以便登记 Job；
 * 用户取消遮罩时调用 {@link #cancelUiRequests()}。
 *
 * @author fz
 * @version 2.2
 * @since 1.0
 * @updated 2026/11/12
 */
public abstract class BaseViewModel<IR extends IRepository> extends BaseViewViewModel {

    protected IR iRepository;

    /**
     * 请求 UI 状态宿主，与 ViewModel 同生命周期；页面重建只重新 bind，不重建 Host。
     */
    @NonNull
    private final NetworkRequestUiHost networkRequestUiHost = new NetworkRequestUiHost();

    /**
     * 与遮罩 Loading 绑定的协程 Job 登记表。
     */
    @NonNull
    private final UiRequestTracker uiRequestTracker = new UiRequestTracker();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        uiRequestTracker.clear();
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

    /**
     * UI 请求 Job 登记表（一般通过 {@code launchUiRequest} 间接使用）。
     */
    @NonNull
    public UiRequestTracker getUiRequestTracker() {
        return uiRequestTracker;
    }

    /**
     * 登记一个与遮罩 Loading 关联的协程 Job。
     * Kotlin 侧请优先使用扩展 {@code launchUiRequest}。
     *
     * @param job viewModelScope.launch 返回值
     */
    public void trackUiRequest(@NonNull Job job) {
        uiRequestTracker.track(job);
    }

    /**
     * 取消当前与遮罩 Loading 绑定的全部请求（协程 Job + Rx 可取消组），并强制关闭 Loading。
     * 由页面在 Loading Dialog OnCancel / 返回取消时调用。
     */
    public void cancelUiRequests() {
        uiRequestTracker.cancelAll();
        if (iRepository instanceof BaseRepository) {
            ((BaseRepository) iRepository).cancelCancellableRequests();
        }
        networkRequestUiHost.forceHideLoading();
    }

    public IR getIRepository() {
        return iRepository;
    }

}
