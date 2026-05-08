package io.coderf.arklab.common.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.impl.RequestUiAdapters;
import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.common.repository.IRepository;

/**
 * Create by fz on 2020/3/19 0019
 * describe:baseViewMode封装
 * <p>
 * <b>请求 UI 注入（单一入口）</b>：{@link #createRepository(BaseView)} 在装配 {@link #iRepository} 后会调用
 * {@link #attachRepositoryRequestUi()}，向 {@link BaseRepository} 写入 {@link RequestUiCallback}。
 * 默认 {@link #provideRequestUiCallback()} 使用 {@link RequestUiAdapters#fromBaseView(BaseView)}，行为与未拆分前一致。
 * 若希望加载/错误先进入 ViewModel 状态，请重写 {@link #provideRequestUiCallback()}（例如返回 {@link NetworkRequestUiHost}），
 * 并在页面使用 {@link NetworkRequestUiBinder#bind} 或自行 observe。
 */
public abstract class BaseViewModel<IR extends IRepository<BV>, BV extends BaseView> extends BaseViewViewModel<BV> {

    protected IR iRepository;

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

    public void createRepository(BV baseView) {
        this.baseView = baseView;
        iRepository = createRepository();
        if (iRepository != null && iRepository.getBaseView() == null) {
            iRepository.setBaseView(baseView);
        }
        attachRepositoryRequestUi();
    }

    /**
     * 向继承 {@link BaseRepository} 的仓库注入 {@link RequestUiCallback}。
     */
    protected void attachRepositoryRequestUi() {
        if (iRepository instanceof BaseRepository) {
            ((BaseRepository<?>) iRepository).setRequestUi(provideRequestUiCallback());
        }
    }

    /**
     * 提供给 Repository 的 UI 回调；不重写时等价于「页面 BaseView 直连」，与历史行为一致。
     */
    @Nullable
    protected RequestUiCallback provideRequestUiCallback() {
        return RequestUiAdapters.fromBaseView(baseView);
    }

    public IR getIRepository() {
        return iRepository;
    }

}
