package io.coderf.arklab.demo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseFragment;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.theme.ThemeUtils;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.adapter.UseCaseAdapter;
import io.coderf.arklab.demo.databinding.FragmentHomeBinding;
import io.coderf.arklab.demo.enums.UseCaseEnum;
import io.coderf.arklab.demo.viewmodel.HomeFragmentViewModel;
import io.coderf.arklab.user.api.UserAccountHelper;

/**
 * HomeFragment 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/4/28
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
        ThemeUtils.setupStatusBarAuto(requireActivity(),
                ContextCompat.getColor(requireContext(), io.coderf.arklab.common.R.color.default_background));
    }

    @Override
    protected void initData(Bundle bundle) {
        binding.setIsLogin(UserAccountHelper.isLogin());
        binding.setToken(UserAccountHelper.isLogin() ? "已登录" : "暂未登录");
        useCaseAdapter = new UseCaseAdapter(UseCaseEnum.toUseCaseList());
        useCaseAdapter.setOnItemClickListener(this);
        binding.mRecyclerViewUseCase.addItemDecoration(
                new RecycleViewDivider(getContext(),
                        LinearLayoutManager.VERTICAL, DensityUtil.dp2px(getContext(), 8),
                        0x00000000)
        );
        binding.mRecyclerViewUseCase.setAdapter(useCaseAdapter);
    }

    @Override
    public void onAuthSuccess(Bundle bundle) {
        super.onAuthSuccess(bundle);

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