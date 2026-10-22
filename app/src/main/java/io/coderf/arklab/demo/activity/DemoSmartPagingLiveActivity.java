package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityDemoSmartPagingLiveBinding;
import io.coderf.arklab.demo.fragment.DemoSmartPagingLiveFragment;

/**
 * SmartPaging + 实时尾（LiveTail）演示：FAB 模拟向页尾追加同类型消息。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/22
 */
@AndroidEntryPoint
public class DemoSmartPagingLiveActivity extends BaseActivity<EmptyViewModel, ActivityDemoSmartPagingLiveBinding> {

    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_demo_smart_paging_live;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.fabAppend.setOnClickListener(v -> {
            DemoSmartPagingLiveFragment fragment = findTailFragment();
            if (fragment != null) {
                fragment.appendLiveItem();
            }
        });
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        if (useCase != null && toolbarBind != null && toolbarBind.getToolbarConfig() != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
    }

    private DemoSmartPagingLiveFragment findTailFragment() {
        Fragment navHost = getSupportFragmentManager()
                .findFragmentById(R.id.demo_smart_paging_tail_fragment_view);
        if (!(navHost instanceof NavHostFragment)) {
            return null;
        }
        Fragment primary = ((NavHostFragment) navHost).getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary instanceof DemoSmartPagingLiveFragment) {
            return (DemoSmartPagingLiveFragment) primary;
        }
        for (Fragment f : ((NavHostFragment) navHost).getChildFragmentManager().getFragments()) {
            if (f instanceof DemoSmartPagingLiveFragment) {
                return (DemoSmartPagingLiveFragment) f;
            }
        }
        return null;
    }
}
