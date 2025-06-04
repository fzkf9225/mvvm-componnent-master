package com.casic.titan.demo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.casic.titan.demo.R;
import com.casic.titan.demo.adapter.UseCaseAdapter;
import com.casic.titan.demo.databinding.FragmentHomeBinding;
import com.casic.titan.demo.enums.UseCaseEnum;
import com.casic.titan.demo.viewmodel.HomeFragmentViewModel;
import com.casic.titan.usercomponent.api.UserAccountHelper;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.theme.ThemeUtils;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * created by fz on 2023/4/28
 * describe：
 */
@AndroidEntryPoint
public class HomeFragment extends BaseFragment<HomeFragmentViewModel, FragmentHomeBinding> implements BaseRecyclerViewAdapter.OnItemClickListener {
    private UseCaseAdapter useCaseAdapter;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ThemeUtils.setupStatusBar(requireActivity(), ContextCompat.getColor(requireContext(), pers.fz.mvvm.R.color.default_background),false);
    }

    @Override
    protected void initData(Bundle bundle) {
        binding.setIsLogin(UserAccountHelper.isLogin());
        binding.setToken(UserAccountHelper.isLogin() ? "已登录" : "暂未登录");
        useCaseAdapter = new UseCaseAdapter(UseCaseEnum.toUseCaseList());
        useCaseAdapter.setOnItemClickListener(this);
        binding.mRecyclerViewUseCase.addItemDecoration(
                new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(getContext(), 8),
                        0x00000000)
        );
        binding.mRecyclerViewUseCase.setAdapter(useCaseAdapter);
    }

    @Override
    public void onLoginSuccessCallback(Bundle bundle) {
        super.onLoginSuccessCallback(bundle);

    }

    @Override
    public void onItemClick(View view, int position) {
        if (Activity.class.isAssignableFrom(useCaseAdapter.getList().get(position).getClx())) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("args", useCaseAdapter.getList().get(position));
            if (useCaseAdapter.getList().get(position).getArgs() != null) {
                bundle.putAll(useCaseAdapter.getList().get(position).getArgs());
            }
            startActivity(useCaseAdapter.getList().get(position).getClx(), bundle);
            return;
        }
    }
}