package io.coderf.arklab.demo.fragment;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseStyledViewPagerFragment;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.databinding.BaseStyledTablayoutViewpagerBinding;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.config.TabLayoutDemoApplier;

/**
 * BaseStyledViewPagerFragment 示例：圆角背景选中态 Tab。
 */
@AndroidEntryPoint
public class StyledTabViewPagerSampleFragment
        extends BaseStyledViewPagerFragment<EmptyViewModel, BaseStyledTablayoutViewpagerBinding> {

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        TabLayoutDemoApplier.applyStyled(getTabLayout());
    }

    @Override
    protected PagerInfo[] getPagers() {
        return new PagerInfo[]{
                new PagerInfo("推荐", ViewPagerSampleFragment.newInstance(0)),
                new PagerInfo("热门", ViewPagerSampleFragment.newInstance(1)),
                new PagerInfo("最新", ViewPagerSampleFragment.newInstance(2))
        };
    }
}
