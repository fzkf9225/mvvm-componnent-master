package io.coderf.arklab.common.base;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.widget.customview.CustomTabLayoutMediator;

/**
 * 自定义 TabLayout（固定宽度底部指示条）+ ViewPager2 Activity 基类。
 *
 * <p>用法与 {@link BaseFixedTabViewPagerFragment} 相同，适用于 Activity 层封装。</p>
 */
public abstract class BaseFixedTabViewPagerActivity<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseActivity<VM, VDB> {

    protected ViewPager2 mBaseViewPager;
    protected io.coderf.arklab.common.widget.customview.TabLayout tabLayout;
    protected LinearLayout llTab;
    protected BaseViewPagerAdapter adapter;
    private CustomTabLayoutMediator tabLayoutMediator;

    @Override
    protected int getLayoutId() {
        return R.layout.base_fixed_tablayout_viewpager;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mBaseViewPager = binding.getRoot().findViewById(R.id.viewpager);
        tabLayout = binding.getRoot().findViewById(R.id.tabLayout);
        llTab = binding.getRoot().findViewById(R.id.ll_tab);
        adapter = createAdapter();
        mBaseViewPager.setAdapter(adapter);
        tabLayoutMediator = new CustomTabLayoutMediator(tabLayout, mBaseViewPager, tabConfigurationStrategy);
        tabLayoutMediator.attach();
        mBaseViewPager.setCurrentItem(0, false);
    }

    @Override
    protected void onDestroy() {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        super.onDestroy();
    }

    protected BaseViewPagerAdapter createAdapter() {
        return new BaseViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), getPagers());
    }

    public BaseViewPagerAdapter getAdapter() {
        return adapter;
    }

    public ViewPager2 getViewPager() {
        return mBaseViewPager;
    }

    public io.coderf.arklab.common.widget.customview.TabLayout getTabLayout() {
        return tabLayout;
    }

    public LinearLayout getLayoutTab() {
        return llTab;
    }

    protected int getCurrentItem() {
        return mBaseViewPager.getCurrentItem();
    }

    protected CustomTabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy =
            (tab, position) -> tab.setText(adapter.getPagerInfo()[position].getTitle());

    protected abstract PagerInfo[] getPagers();
}
