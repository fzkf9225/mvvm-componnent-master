package io.coderf.arklab.common.base;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.widget.customview.IndicatorTabLayout;

/**
 *  tabLayout+viewpager侧滑fragment布局封装
 *  底部可以自定义指示器宽度高度颜色
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/5/28 15:45
 */
public abstract class BaseIndicatorViewPagerFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends BaseFragment<VM, VDB> {
    protected ViewPager2 mBaseViewPager;
    protected IndicatorTabLayout tabLayout;
    protected ConstraintLayout clTab;
    protected BaseViewPagerAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.base_indicator_tablayout_viewpager;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBaseViewPager = binding.getRoot().findViewById(R.id.viewpager);
        tabLayout = binding.getRoot().findViewById(R.id.tabLayout);
        clTab = binding.getRoot().findViewById(R.id.cl_tab);
        adapter = createAdapter();
        mBaseViewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, mBaseViewPager, tabConfigurationStrategy).attach();
        mBaseViewPager.setCurrentItem(0, true);
    }

    protected BaseViewPagerAdapter createAdapter() {
        return new BaseViewPagerAdapter(getChildFragmentManager(), getLifecycle(), getPagers());
    }

    public BaseViewPagerAdapter getAdapter() {
        return adapter;
    }

    public ViewPager2 getViewPager() {
        return mBaseViewPager;
    }

    public IndicatorTabLayout getTabLayout() {
        return tabLayout;
    }

    public ConstraintLayout getLayoutTab() {
        return clTab;
    }

    public void setOverScrollMode(int scrollMode) {
        tabLayout.setOverScrollMode(scrollMode);
    }

    protected int getCurrentItem() {
        return mBaseViewPager.getCurrentItem();
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    protected TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy = (tab, position) -> tab.setText(adapter.getPagerInfo()[position].getTitle());

    protected abstract PagerInfo[] getPagers();

}
