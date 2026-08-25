package io.coderf.arklab.common.base;

import androidx.databinding.ViewDataBinding;

import io.coderf.arklab.core.ui.delegate.FirstCreateOnlyInitData;

/**
 * 与 {@link BaseStatefulActivity} 对应的 Fragment 基类：仅在首次创建时调用 {@link #initData}。
 * <p>
 * 通过 {@link FirstCreateOnlyInitData} 策略实现；老 Fragment 可继续继承 {@link BaseFragment}。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/8/25 13:13
 */
public abstract class BaseStatefulFragment<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseFragment<VM, VDB> {

    {
        // 实例初始化块：在构造后、onCreateView 前设置策略
        initDataPolicy = FirstCreateOnlyInitData.INSTANCE;
    }
}
