package pers.fz.mvvm.base;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pers.fz.mvvm.bean.PagerInfo;

/**
 * created by fz on 2025/2/12 9:16
 * describe:
 */
public class BaseViewPagerAdapter extends FragmentStateAdapter {
    private final PagerInfo[] mInfoList;
    private final FragmentManager fragmentManager;

    public BaseViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, PagerInfo[] mInfoList) {
        super(fragmentManager, lifecycle);
        this.fragmentManager = fragmentManager;
        this.mInfoList = mInfoList;
    }

    public PagerInfo[] getPagerInfo() {
        return mInfoList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PagerInfo info = mInfoList[position];
        return info.getFragment();
    }

    @Override
    public int getItemCount() {
        return mInfoList.length;
    }

    public Fragment getItem(int position){
        return fragmentManager.findFragmentByTag("f$position");
    }

    public Fragment getFragment(int position){
        return mInfoList[position].getFragment();
    }

    public String getTitle(int position){
        return mInfoList[position].getTitle();
    }

}

