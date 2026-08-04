package io.coderf.arklab.common.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
import java.util.Objects;

import io.coderf.arklab.common.bean.GridMenuBean;
import io.coderf.arklab.common.fragment.GridMenuFragment;
import io.coderf.arklab.common.widget.customview.GridMenuView;


/**
 * Created by fz on 2024/11/22
 */
public class GridMenuViewPager2Adapter<T extends GridMenuBean> extends FragmentStateAdapter {
    private final List<List<T>> mInfoList;
    private final GridMenuView gridMenuView;

    public GridMenuViewPager2Adapter(@NonNull GridMenuView gridMenuView, List<List<T>> mInfoList) {
        super(Objects.requireNonNull(gridMenuView.getFragmentManager()), Objects.requireNonNull(gridMenuView.getLifecycleOwner()).getLifecycle());
        this.gridMenuView = gridMenuView;
        this.mInfoList = mInfoList;
    }

    public List<List<T>> getPagerInfo() {
        return mInfoList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        List<T> info = mInfoList.get(position);
        return GridMenuFragment.newInstance(info, gridMenuView);
    }

    @Override
    public int getItemCount() {
        return mInfoList.size();
    }

    public Fragment getItem(int position) {
        return Objects.requireNonNull(gridMenuView.getFragmentManager()).findFragmentByTag("f" + position);
    }

}
