package com.casic.otitan.common.fragment;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseFragment;
import com.casic.otitan.common.databinding.EmptyFragmentBinding;
import com.casic.otitan.common.viewmodel.EmptyViewModel;
import com.casic.otitan.common.widget.empty.EmptyLayout;

/**
 * Created by fz on 2023/7/13 9:35
 * describe :空页面
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
