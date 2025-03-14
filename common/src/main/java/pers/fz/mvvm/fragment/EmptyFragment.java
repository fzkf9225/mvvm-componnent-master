package pers.fz.mvvm.fragment;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.databinding.EmptyFragmentBinding;
import pers.fz.mvvm.viewmodel.EmptyViewModel;
import pers.fz.mvvm.wight.empty.EmptyLayout;

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
        binding.mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
    }

    @Override
    protected void initData(Bundle bundle) {

    }
}
