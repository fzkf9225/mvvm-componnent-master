package io.coderf.arklab.demo.fragment;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseViewPagerFragment;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.databinding.BaseTablayoutViewpagerBinding;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;

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
