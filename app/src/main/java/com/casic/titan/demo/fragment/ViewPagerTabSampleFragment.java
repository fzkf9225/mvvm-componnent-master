package com.casic.titan.demo.fragment;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.base.BaseViewPagerFragment;
import pers.fz.mvvm.databinding.BaseTablayoutViewpagerBinding;

/**
 * Created by fz on 2023/8/17 11:01
 * describe :
 */
@AndroidEntryPoint
public class ViewPagerTabSampleFragment extends BaseViewPagerFragment<BaseViewModel, BaseTablayoutViewpagerBinding> {
    @Override
    protected PagerInfo[] getPagers() {
        return new PagerInfo[]{
                new PagerInfo("北京",ViewPagerSampleFragment.newInstance(0)),
                new PagerInfo("合肥",ViewPagerSampleFragment.newInstance(1)),
                new PagerInfo("上海",ViewPagerSampleFragment.newInstance(2))
        };
    }
}
