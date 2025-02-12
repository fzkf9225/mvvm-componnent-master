package pers.fz.mvvm.base;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import pers.fz.mvvm.R;
import pers.fz.mvvm.bean.PagerInfo;

/**
 * Created by fz on 2021/2/7 14:13
 * describe:自定义toolbar样式baseActivity
 */
public abstract class BaseViewPagerActivity<VM extends BaseViewModel, VDB extends ViewDataBinding> extends BaseActivity<VM, VDB> {
    protected ViewPager2 mBaseViewPager;
    protected TabLayout tabLayout;
    protected LinearLayout llTab;
    protected BaseViewPagerAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.base_tablayout_viewpager;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mBaseViewPager = binding.getRoot().findViewById(R.id.viewpager);
        tabLayout = binding.getRoot().findViewById(R.id.tabLayout);
        llTab = binding.getRoot().findViewById(R.id.ll_tab);
        adapter = new BaseViewPagerAdapter(getSupportFragmentManager(), this.getLifecycle(), getPagers());
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

    protected TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy = (tab, position) -> tab.setText(adapter.getPagerInfo()[position].getTitle());

    protected abstract PagerInfo[] getPagers();

}
