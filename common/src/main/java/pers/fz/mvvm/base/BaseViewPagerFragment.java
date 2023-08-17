package pers.fz.mvvm.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.apiUtil.DensityUtil;

/**
 * Create by CherishTang on 2019/8/1
 * describe: tabLayout+viewpager侧滑fragment布局封装
 */
public abstract class BaseViewPagerFragment<VM extends BaseViewModel, VDB extends ViewDataBinding> extends BaseFragment<VM, VDB>{
    public ViewPager2 mBaseViewPager;
    protected TabLayout tabLayout;
    public LinearLayout llTab;

    public BaseViewPagerAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.base_tablayout_viewpager;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBaseViewPager = binding.getRoot().findViewById(R.id.viewpager);
        tabLayout = binding.getRoot().findViewById(R.id.tabLayout);
        llTab = binding.getRoot().findViewById(R.id.ll_tab);
        adapter = new BaseViewPagerAdapter(getChildFragmentManager(), this.getLifecycle(),getPagers());
        mBaseViewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, mBaseViewPager,
                (tab, position) -> tab.setText(adapter.getPagerInfo()[position].title)
        ).attach();
        mBaseViewPager.setCurrentItem(0, true);
    }

    public void setOverScrollMode(int scrollMode) {
        tabLayout.setOverScrollMode(scrollMode);
    }

    public int getCurrentItem() {
        return mBaseViewPager.getCurrentItem();
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    protected abstract PagerInfo[] getPagers();

    public static class PagerInfo {
        /**
         * tab上的title
         */
        private final String title;
        /**
         * 页面page路由
         */
        private final Fragment toClx;

        public PagerInfo(String title, Fragment toClx) {
            this.title = title;
            this.toClx = toClx;
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
            return info.toClx;
        }

        @Override
        public int getItemCount() {
            return mInfoList.length;
        }
    }


}
