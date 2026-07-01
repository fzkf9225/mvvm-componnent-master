package io.coderf.arklab.common.widget.customview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * 将自定义 {@link TabLayout} 与 {@link ViewPager2} 联动。
 *
 * <p>API 与 Material {@code TabLayoutMediator} 类似，用于 {@link TabLayout} 这类
 * 非 Material 官方组件的 ViewPager2 绑定场景。</p>
 *
 * <pre>
 * new CustomTabLayoutMediator(tabLayout, viewPager, (tab, position) ->
 *     tab.setText(titles[position])).attach();
 * </pre>
 */
public class CustomTabLayoutMediator {

    public interface TabConfigurationStrategy {
        void onConfigureTab(@NonNull TabLayout.Tab tab, int position);
    }

    @NonNull
    private final TabLayout tabLayout;
    @NonNull
    private final ViewPager2 viewPager;
    private final boolean autoRefresh;
    @NonNull
    private final TabConfigurationStrategy tabConfigurationStrategy;
    @Nullable
    private TabLayoutOnPageChangeCallback onPageChangeCallback;
    @Nullable
    private TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> onTabSelectedListener;
    @Nullable
    private RecyclerView.AdapterDataObserver adapterDataObserver;
    private boolean attached;

    public CustomTabLayoutMediator(
            @NonNull TabLayout tabLayout,
            @NonNull ViewPager2 viewPager,
            @NonNull TabConfigurationStrategy tabConfigurationStrategy) {
        this(tabLayout, viewPager, true, tabConfigurationStrategy);
    }

    public CustomTabLayoutMediator(
            @NonNull TabLayout tabLayout,
            @NonNull ViewPager2 viewPager,
            boolean autoRefresh,
            @NonNull TabConfigurationStrategy tabConfigurationStrategy) {
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
        this.autoRefresh = autoRefresh;
        this.tabConfigurationStrategy = tabConfigurationStrategy;
    }

    public void attach() {
        if (attached) {
            throw new IllegalStateException("CustomTabLayoutMediator is already attached");
        }
        if (viewPager.getAdapter() == null) {
            throw new IllegalStateException(
                    "CustomTabLayoutMediator attached before ViewPager2 has an adapter");
        }
        attached = true;
        populateTabs();

        onTabSelectedListener = new TabLayout.BaseOnTabSelectedListener<TabLayout.Tab>() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
            }
        };
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        onPageChangeCallback = new TabLayoutOnPageChangeCallback(tabLayout);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        if (autoRefresh) {
            adapterDataObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    populateTabs();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    populateTabs();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    populateTabs();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    populateTabs();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    populateTabs();
                }
            };
            viewPager.getAdapter().registerAdapterDataObserver(adapterDataObserver);
        }

        syncSelectedTab(viewPager.getCurrentItem());
    }

    public void detach() {
        if (!attached) {
            return;
        }
        if (onTabSelectedListener != null) {
            tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
            onTabSelectedListener = null;
        }
        if (onPageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
            onPageChangeCallback = null;
        }
        if (adapterDataObserver != null && viewPager.getAdapter() != null) {
            viewPager.getAdapter().unregisterAdapterDataObserver(adapterDataObserver);
            adapterDataObserver = null;
        }
        attached = false;
    }

    private void populateTabs() {
        tabLayout.removeAllTabs();
        RecyclerView.Adapter<?> adapter = viewPager.getAdapter();
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < adapter.getItemCount(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tabConfigurationStrategy.onConfigureTab(tab, i);
            tabLayout.addTab(tab, false);
        }
        syncSelectedTab(viewPager.getCurrentItem());
    }

    private void syncSelectedTab(int position) {
        if (position >= 0 && position < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                tab.select();
            }
        }
    }

    private static class TabLayoutOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        @NonNull
        private final TabLayout tabLayout;
        private int previousScrollState = ViewPager2.SCROLL_STATE_IDLE;
        private int scrollState = ViewPager2.SCROLL_STATE_IDLE;

        TabLayoutOnPageChangeCallback(@NonNull TabLayout tabLayout) {
            this.tabLayout = tabLayout;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            previousScrollState = scrollState;
            scrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position >= tabLayout.getTabCount()) {
                return;
            }
            // 滑动过程中只更新指示条，不切换选中态样式（加粗/字号变化会导致文字宽度改变，指示条偏移）
            tabLayout.setScrollPosition(position, positionOffset, false, true);
        }

        @Override
        public void onPageSelected(int position) {
            if (position < 0 || position >= tabLayout.getTabCount()) {
                return;
            }
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                tabLayout.selectTab(tab, true);
            }
        }
    }
}
