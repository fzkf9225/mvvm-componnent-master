package pers.fz.mvvm.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
import java.util.Objects;

import pers.fz.mvvm.bean.HomeMenuBean;
import pers.fz.mvvm.fragment.HomeMenuFragment;
import pers.fz.mvvm.widget.customview.HomeMenuView;


/**
 * Created by fz on 2024/11/22
 */
public class HomeMenuViewPager2Adapter<T extends HomeMenuBean> extends FragmentStateAdapter {
    private final List<List<T>> mInfoList;
    private final HomeMenuView homeMenuView;

    public HomeMenuViewPager2Adapter(@NonNull HomeMenuView homeMenuView, List<List<T>> mInfoList) {
        super(Objects.requireNonNull(homeMenuView.getFragmentManager()), Objects.requireNonNull(homeMenuView.getLifecycleOwner()).getLifecycle());
        this.homeMenuView = homeMenuView;
        this.mInfoList = mInfoList;
    }

    public List<List<T>> getPagerInfo() {
        return mInfoList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        List<T> info = mInfoList.get(position);
        return HomeMenuFragment.newInstance(info, homeMenuView);
    }

    @Override
    public int getItemCount() {
        return mInfoList.size();
    }

    public Fragment getItem(int position) {
        return Objects.requireNonNull(homeMenuView.getFragmentManager()).findFragmentByTag("f" + position);
    }

}

