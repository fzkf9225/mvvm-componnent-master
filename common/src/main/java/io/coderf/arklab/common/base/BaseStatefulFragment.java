package io.coderf.arklab.common.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

/**
 * 与 {@link BaseStatefulActivity} 对应的 Fragment 基类：仅在首次创建时调用 {@link #initData(Bundle)}。
 * <p>
 * 老 Fragment 可继续继承 {@link BaseFragment}，新项目或需要防重复加载的页面可改用本类。
 */
public abstract class BaseStatefulFragment<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseFragment<VM, VDB> {

    @Override
    protected boolean shouldRunInitData(@Nullable Bundle savedInstanceState) {
        return savedInstanceState == null;
    }
}
