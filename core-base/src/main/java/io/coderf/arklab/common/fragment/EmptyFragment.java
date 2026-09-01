package io.coderf.arklab.common.fragment;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseFragment;
import io.coderf.arklab.common.databinding.EmptyFragmentBinding;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.widget.empty.EmptyLayout;

/**
 * 空页面
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/7/13 9:35
 */
@AndroidEntryPoint
public class EmptyFragment extends BaseFragment<EmptyViewModel, EmptyFragmentBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.empty_fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        binding.mEmptyLayout.setState(EmptyLayout.State.NETWORK_LOADING);
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    public EmptyLayout getEmptyLayout() {
        return binding.mEmptyLayout;
    }
}
