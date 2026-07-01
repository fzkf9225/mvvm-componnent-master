package io.coderf.arklab.demo.activity;

import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityHiltBinding;
import io.coderf.arklab.demo.fragment.HiltDemoFragment;
import io.coderf.arklab.demo.viewmodel.TestHiltViewModel;

@AndroidEntryPoint
public class HiltActivity extends BaseActivity<TestHiltViewModel, ActivityHiltBinding> {

    /** 演示 Activity 字段注入（继承 BaseActivity 已注入 ErrorService，此处补充 @Named）。 */
    @Inject
    @Named("env_dev")
    String activityDevEnv;

    @Inject
    ErrorService injectedErrorService;

    private HiltDemoFragment hiltDemoFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_hilt;
    }

    @Override
    public String setTitleBar() {
        return "Hilt依赖注入";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.setHiltViewModel(mViewModel);
        binding.setActivityInjectInfo(
                "Activity @Inject\n"
                        + "@Named(env_dev)=" + activityDevEnv + "\n"
                        + "ErrorService=" + (injectedErrorService != null ? injectedErrorService.getClass().getSimpleName() : "null")
        );
        if (savedInstanceState == null) {
            hiltDemoFragment = new HiltDemoFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_hilt_demo, hiltDemoFragment)
                    .commit();
        } else {
            hiltDemoFragment = (HiltDemoFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_hilt_demo);
        }
    }

    @Override
    public void initData(Bundle bundle) {

    }
}
