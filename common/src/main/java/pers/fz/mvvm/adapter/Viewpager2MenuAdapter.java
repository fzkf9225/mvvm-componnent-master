package pers.fz.mvvm.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import pers.fz.mvvm.bean.HomeMenuBean;
import pers.fz.mvvm.fragment.HomeMenuFragment;
import pers.fz.mvvm.listener.HomeMenuAdapterListener;
import pers.fz.mvvm.listener.PagingAdapterListener;


/**
 * Created by fz on 2024/11/22
 */
public class Viewpager2MenuAdapter<T extends HomeMenuBean> extends FragmentStateAdapter {
    private final List<List<T>> mInfoList;
    private final FragmentManager fragmentManager;
    private final int column;
    private final PagingAdapterListener<T> adapterListener;
    private final HomeMenuAdapterListener homeMenuAdapterListener;

    public Viewpager2MenuAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<List<T>> mInfoList, int column, PagingAdapterListener<T> adapterListener,
                                 HomeMenuAdapterListener homeMenuAdapterListener) {
        super(fragmentManager, lifecycle);
        this.fragmentManager = fragmentManager;
        this.mInfoList = mInfoList;
        this.column = column;
        this.adapterListener = adapterListener;
        this.homeMenuAdapterListener = homeMenuAdapterListener;
    }

    public List<List<T>> getPagerInfo() {
        return mInfoList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        List<T> info = mInfoList.get(position);
        return HomeMenuFragment.newInstance(info, column, adapterListener,homeMenuAdapterListener);
    }

    @Override
    public int getItemCount() {
        return mInfoList.size();
    }

    public Fragment getItem(int position) {
        return fragmentManager.findFragmentByTag("f" + position);
    }

}

