package io.coderf.arklab.demo.fragment;

import android.os.Bundle;

import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ViewPagerSampleFragmentBinding;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseFragment;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;

/**
 * Created by fz on 2023/8/17 10:57
 * describe :
 */

@AndroidEntryPoint
public class ViewPagerSampleFragment extends BaseFragment<EmptyViewModel, ViewPagerSampleFragmentBinding> {
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
