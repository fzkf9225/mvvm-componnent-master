package io.coderf.arklab.common.base;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.widget.customview.StyledTabLayout;

/**
 * StyledTabLayout + ViewPager2 Activity 基类。
 *
 * <p>用法与 {@link BaseStyledViewPagerFragment} 相同，适用于 Activity 层封装。</p>
 */
public abstract class BaseStyledViewPagerActivity<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseActivity<VM, VDB> {

    protected ViewPager2 mBaseViewPager;
    protected StyledTabLayout tabLayout;
    protected LinearLayout llTab;
    protected BaseViewPagerAdapter adapter;
    private TabLayoutMediator tabLayoutMediator;

    @Override
    protected int getLayoutId() {
        return R.layout.base_styled_tablayout_viewpager;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mBaseViewPager = binding.getRoot().findViewById(R.id.viewpager);
        tabLayout = binding.getRoot().findViewById(R.id.tabLayout);
        llTab = binding.getRoot().findViewById(R.id.ll_tab);
        adapter = createAdapter();
        mBaseViewPager.setAdapter(adapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, mBaseViewPager, tabConfigurationStrategy);
        tabLayoutMediator.attach();
        tabLayout.applyStylesToAllTabs();
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

    public StyledTabLayout getTabLayout() {
        return tabLayout;
    }

    public LinearLayout getLayoutTab() {
        return llTab;
    }

    protected int getCurrentItem() {
        return mBaseViewPager.getCurrentItem();
    }

    protected TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy =
            (tab, position) -> tab.setText(adapter.getPagerInfo()[position].getTitle());

    protected abstract PagerInfo[] getPagers();
}
