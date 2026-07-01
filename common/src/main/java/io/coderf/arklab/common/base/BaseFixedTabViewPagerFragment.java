package io.coderf.arklab.common.base;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.databinding.ViewDataBinding;
import androidx.viewpager2.widget.ViewPager2;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.PagerInfo;
import io.coderf.arklab.common.widget.customview.CustomTabLayoutMediator;

/**
 * 自定义 TabLayout（固定宽度底部指示条）+ ViewPager2 Fragment 基类。
 *
 * <p>布局：{@link R.layout#base_fixed_tablayout_viewpager}</p>
 * <p>通过 {@link io.coderf.arklab.common.widget.customview.CustomTabLayoutMediator} 与 ViewPager2 联动。</p>
 *
 * <p>Tab 样式可通过布局文件中自定义属性配置，例如：</p>
 * <ul>
 *   <li>{@code indicatorFixedWidth} — 指示条固定宽度</li>
 *   <li>{@code tabIndicatorColor} / {@code tabIndicatorHeight} — 指示条颜色与高度</li>
 *   <li>{@code selectedTextSize} / {@code unselectedTextSize} — 选中/未选中文字大小</li>
 *   <li>{@code selectedTextBold} — 选中态加粗</li>
 * </ul>
 */
public abstract class BaseFixedTabViewPagerFragment<VM extends BaseViewModel, VDB extends ViewDataBinding>
        extends BaseFragment<VM, VDB> {

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
    protected void initView(Bundle savedInstanceState) {
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

    public io.coderf.arklab.common.widget.customview.TabLayout getTabLayout() {
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

    protected CustomTabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy =
            (tab, position) -> tab.setText(adapter.getPagerInfo()[position].getTitle());

    protected abstract PagerInfo[] getPagers();
}
