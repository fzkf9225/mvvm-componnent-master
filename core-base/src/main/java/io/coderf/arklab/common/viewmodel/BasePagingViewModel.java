package io.coderf.arklab.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseViewModel;

/**
 * 分页 ViewModel 基类（无 Repository 泛型）。
 * 仓库由子类构造注入或属性持有；首次拉数请在子类 init / 构造完成后调用 {@link #refreshData()}。
 *
 * @author fz
 * @version 2.1
 * @since 1.0
 * @created 2024/11/6 11:36
 * @updated 2026/11/16 增加 {@link #isLoadMoreEnabled()}，不分页列表关闭继续加载
 */
public abstract class BasePagingViewModel extends BaseViewModel {

    public BasePagingViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract void refreshData();

    /**
     * 是否允许继续加载下一页。
     *
     * <p>默认 true。后端不分页时重写为 false：数据源只请求一次（{@code nextKey} 恒为 null），
     * 列表底部不展示「暂无更多」。这是页面级常量，须在首次 {@link #refreshData()} 之前确定。</p>
     */
    public boolean isLoadMoreEnabled() {
        return true;
    }

}
