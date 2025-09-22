package com.casic.otitan.demo.fragment;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.base.BaseViewPagerFragment;
import com.casic.otitan.common.bean.PagerInfo;
import com.casic.otitan.common.databinding.BaseTablayoutViewpagerBinding;
import com.casic.otitan.common.viewmodel.EmptyViewModel;

/**
 * Created by fz on 2023/8/17 11:01
 * describe :
 */
@AndroidEntryPoint
public class ViewPagerTabSampleFragment extends BaseViewPagerFragment<EmptyViewModel, BaseTablayoutViewpagerBinding> {
    @Override
    protected PagerInfo[] getPagers() {
        return new PagerInfo[]{
                new PagerInfo("北京",ViewPagerSampleFragment.newInstance(0)),
                new PagerInfo("合肥",ViewPagerSampleFragment.newInstance(1)),
                new PagerInfo("上海",ViewPagerSampleFragment.newInstance(2))
        };
    }
}
