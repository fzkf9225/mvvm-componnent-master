package pers.fz.mvvm.base;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import pers.fz.mvvm.R;

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

    public void setOverScrollMode(int scrollMode) {
        tabLayout.setOverScrollMode(scrollMode);
    }

    protected int getCurrentItem() {
        return mBaseViewPager.getCurrentItem();
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    protected TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy = (tab, position) -> tab.setText(adapter.getPagerInfo()[position].title);

    protected abstract PagerInfo[] getPagers();

    public static class PagerInfo {
        /**
         * tab上的title
         */
        private final String title;
        /**
         * 页面page路由
         */
        private final Fragment toFragment;

        public PagerInfo(String title, Fragment toFragment) {
            this.title = title;
            this.toFragment = toFragment;
        }

        public String getTitle() {
            return title;
        }

        public Fragment getFragment() {
            return toFragment;
        }
    }

    public static class BaseViewPagerAdapter extends FragmentStateAdapter {
        private final PagerInfo[] mInfoList;

        public BaseViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, PagerInfo[] mInfoList) {
            super(fragmentManager, lifecycle);
            this.mInfoList = mInfoList;
        }

        public PagerInfo[] getPagerInfo() {
            return mInfoList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            PagerInfo info = mInfoList[position];
            return info.toFragment;
        }

        @Override
        public int getItemCount() {
            return mInfoList.length;
        }
    }


}
