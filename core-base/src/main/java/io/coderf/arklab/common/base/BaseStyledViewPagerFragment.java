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
 * StyledTabLayout + ViewPager2 Fragment 基类。
 *
 * <p>布局：{@link R.layout#base_styled_tablayout_viewpager}</p>
 * <p>子类只需实现 {@link #getPagers()} 返回 Tab 标题与 Fragment 映射。</p>
 *
 * <p>Tab 样式可通过布局文件中 {@code StyledTabLayout} 的自定义属性配置，例如：</p>
 * <ul>
 *   <li>{@code selectedTextColor} / {@code unselectedTextColor}</li>
 *   <li>{@code selectedTextSize} / {@code unselectedTextSize}</li>
 *   <li>{@code selectedTextBold} / {@code unselectedTextBold}</li>
 * </ul>
 */
public abstract class BaseStyledViewPagerFragment<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseFragment<VM, VDB> {

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
    protected void initView(Bundle savedInstanceState) {
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
    public void onDestroyView() {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        super.onDestroyView();
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

    public StyledTabLayout getTabLayout() {
        return tabLayout;
    }

    public LinearLayout getLayoutTab() {
        return llTab;
    }

    protected int getCurrentItem() {
        return mBaseViewPager.getCurrentItem();
    }

    @Override
    protected void initData(Bundle bundle) {
    }

    protected TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy =
            (tab, position) -> tab.setText(adapter.getPagerInfo()[position].getTitle());

    protected abstract PagerInfo[] getPagers();
}
