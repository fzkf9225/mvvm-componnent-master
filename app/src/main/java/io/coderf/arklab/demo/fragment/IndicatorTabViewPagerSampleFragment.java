package io.coderf.arklab.demo.fragment;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseIndicatorViewPagerFragment;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.databinding.BaseIndicatorTablayoutViewpagerBinding;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.config.TabLayoutDemoApplier;

/**
 * BaseIndicatorViewPagerFragment 示例：Canvas 绘制圆角指示条 Tab。
 */
@AndroidEntryPoint
public class IndicatorTabViewPagerSampleFragment
        extends BaseIndicatorViewPagerFragment<EmptyViewModel, BaseIndicatorTablayoutViewpagerBinding> {

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        TabLayoutDemoApplier.applyIndicator(getTabLayout());
    }

    @Override
    protected PagerInfo[] getPagers() {
        return new PagerInfo[]{
                new PagerInfo("视频", ViewPagerSampleFragment.newInstance(0)),
                new PagerInfo("图文", ViewPagerSampleFragment.newInstance(1)),
                new PagerInfo("直播", ViewPagerSampleFragment.newInstance(2))
        };
    }
}
