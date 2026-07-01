package io.coderf.arklab.demo.fragment;

import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseFragment;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.HiltSingletonCounter;
import io.coderf.arklab.demo.databinding.FragmentHiltDemoBinding;

/**
 * 演示 Fragment 字段注入（@AndroidEntryPoint + @Inject）。
 */
@AndroidEntryPoint
public class HiltDemoFragment extends BaseFragment<EmptyViewModel, FragmentHiltDemoBinding> {

    @Inject
    @Named("env_prod")
    String prodEnv;

    @Inject
    HiltSingletonCounter singletonCounter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_hilt_demo;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        binding.tvFragmentInject.setText(
                "Fragment @Inject\n"
                        + "env_prod=" + prodEnv + "\n"
                        + "singletonCounter=" + singletonCounter
        );
    }

    @Override
    protected void initData(Bundle bundle) {
    }

    public String getInjectSummary() {
        return "Fragment注入成功: env=" + prodEnv + ", counter=" + singletonCounter.getCount();
    }
}
