package io.coderf.arklab.demo.fragment;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseFixedTabViewPagerFragment;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.databinding.BaseFixedTablayoutViewpagerBinding;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.config.TabLayoutDemoApplier;

/**
 * BaseFixedTabViewPagerFragment 示例：固定宽度底部指示条 Tab。
 */
@AndroidEntryPoint
public class FixedTabViewPagerSampleFragment
        extends BaseFixedTabViewPagerFragment<EmptyViewModel, BaseFixedTablayoutViewpagerBinding> {

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        TabLayoutDemoApplier.applyFixed(getTabLayout());
    }

    @Override
    protected PagerInfo[] getPagers() {
        return new PagerInfo[]{
                new PagerInfo("关注", ViewPagerSampleFragment.newInstance(0)),
                new PagerInfo("发现", ViewPagerSampleFragment.newInstance(1)),
                new PagerInfo("附近", ViewPagerSampleFragment.newInstance(2))
        };
    }
}
