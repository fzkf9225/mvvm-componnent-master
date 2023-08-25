package com.casic.titan.demo.fragment;

import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.ViewPagerSampleFragmentBinding;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.base.BaseViewPagerFragment;

/**
 * Created by fz on 2023/8/17 10:57
 * describe :
 */

@AndroidEntryPoint
public class ViewPagerSampleFragment extends BaseFragment<BaseViewModel, ViewPagerSampleFragmentBinding> {
    @Override
    protected int getLayoutId() {
        return R.layout.view_pager_sample_fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

    }

    @Override
    protected void initData(Bundle bundle) {
        binding.tvViewPagerSample.setText("这是第" + bundle.getInt("page", 0) + "个页面");
    }

    public static ViewPagerSampleFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt("page", page);
        ViewPagerSampleFragment fragment = new ViewPagerSampleFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
