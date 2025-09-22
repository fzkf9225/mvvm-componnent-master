package com.casic.otitan.common.base;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.casic.otitan.common.R;
import com.casic.otitan.common.bean.PagerInfo;

/**
 * Create by CherishTang on 2019/8/1
 * describe: tabLayout+viewpager侧滑fragment布局封装
 */
public abstract class BaseViewPagerFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends BaseFragment<VM, VDB> {
    protected ViewPager2 mBaseViewPager;
    protected TabLayout tabLayout;
    protected LinearLayout llTab;
    protected BaseViewPagerAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.base_tablayout_viewpager;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBaseViewPager = binding.getRoot().findViewById(R.id.viewpager);
        tabLayout = binding.getRoot().findViewById(R.id.tabLayout);
        llTab = binding.getRoot().findViewById(R.id.ll_tab);
        adapter = new BaseViewPagerAdapter(getChildFragmentManager(), this.getLifecycle(), getPagers());
        mBaseViewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, mBaseViewPager, tabConfigurationStrategy).attach();
        mBaseViewPager.setCurrentItem(0, true);
    }

    public BaseViewPagerAdapter getAdapter() {
        return adapter;
    }

    public ViewPager2 getViewPager() {
        return mBaseViewPager;
    }

    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public LinearLayout getLayoutTab() {
        return llTab;
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
