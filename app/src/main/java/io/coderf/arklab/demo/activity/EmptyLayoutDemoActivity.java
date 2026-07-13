package io.coderf.arklab.demo.activity;



import android.os.Build;

import android.os.Bundle;

import android.os.Handler;

import android.os.Looper;

import android.view.View;



import dagger.hilt.android.AndroidEntryPoint;

import io.coderf.arklab.common.base.BaseActivity;

import io.coderf.arklab.common.viewmodel.EmptyViewModel;

import io.coderf.arklab.common.widget.empty.EmptyLayout;

import io.coderf.arklab.demo.R;

import io.coderf.arklab.demo.bean.UseCase;

import io.coderf.arklab.demo.databinding.ActivityEmptyLayoutDemoBinding;



/**

 * EmptyLayout 用法演示：加载样式切换、骨架屏行数、空态/错误/重试。

 *

 * @author fz

 * @version 1.0

 * @since 1.0

 * @created 2026/7/13

 */

@AndroidEntryPoint

public class EmptyLayoutDemoActivity extends BaseActivity<EmptyViewModel, ActivityEmptyLayoutDemoBinding> {



    private static final long MOCK_LOAD_DELAY_MS = 1500L;



    private final Handler handler = new Handler(Looper.getMainLooper());

    private UseCase useCase;

    private Runnable pendingLoadFinish;



    @Override

    protected int getLayoutId() {

        return R.layout.activity_empty_layout_demo;

    }



    @Override

    public String setTitleBar() {

        return "EmptyLayout 演示";

    }



    @Override

    public void initView(Bundle savedInstanceState) {

        binding.demoEmptyLayout.setOnEmptyLayoutClickListener(this::onEmptyLayoutClick);

        binding.btnSkeletonLoading.setOnClickListener(v -> simulateSkeletonFirstLoad(6));

        binding.btnSkeleton4Rows.setOnClickListener(v -> simulateSkeletonFirstLoad(4));

        binding.btnSkeleton8Rows.setOnClickListener(v -> simulateSkeletonFirstLoad(8));

        binding.btnSpinnerLoading.setOnClickListener(v -> simulateFirstLoad(false));

        binding.btnSkeletonRefresh.setOnClickListener(v -> simulateRefreshLoad(6));

        binding.btnNoData.setOnClickListener(v -> showNoData());

        binding.btnError.setOnClickListener(v -> showError());

        binding.btnHide.setOnClickListener(v -> showContent());

        updateStateLabel();

        // 进入页面默认展示 6 行骨架屏首刷

        simulateSkeletonFirstLoad(6);

    }



    @Override

    public void initData(Bundle bundle) {

        if (bundle == null) {

            return;

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            useCase = bundle.getParcelable("args", UseCase.class);

        } else {

            useCase = bundle.getParcelable("args");

        }

        if (useCase != null) {

            toolbarBind.getToolbarConfig().setTitle(useCase.getName());

        }

    }



    private void onEmptyLayoutClick(View v) {

        EmptyLayout.State state = binding.demoEmptyLayout.getCurrentState();

        if (state == EmptyLayout.State.NETWORK_LOADING_REFRESH) {

            showToast("点击重试：骨架屏刷新（" + binding.demoEmptyLayout.getSkeletonRowCount() + " 行）");

            simulateRefreshLoad(binding.demoEmptyLayout.getSkeletonRowCount());

        } else if (state == EmptyLayout.State.LOADING_ERROR

                || state == EmptyLayout.State.NO_DATA_ENABLE_CLICK) {

            showToast("点击重试：重新加载");

            if (binding.demoEmptyLayout.isSkeletonLoadingEnabled()) {

                simulateSkeletonFirstLoad(binding.demoEmptyLayout.getSkeletonRowCount());

            } else {

                simulateFirstLoad(false);

            }

        }

    }



    /** 骨架屏首刷：指定占位行数。 */

    private void simulateSkeletonFirstLoad(int rowCount) {

        cancelPendingLoad();

        applySkeletonConfig(rowCount);

        binding.mockListContent.setVisibility(View.GONE);

        binding.demoEmptyLayout.setState(EmptyLayout.State.NETWORK_LOADING);

        updateStateLabel();

        scheduleLoadSuccess(false);

    }



    /** 转圈首刷。 */

    private void simulateFirstLoad(boolean skeleton) {

        if (skeleton) {

            simulateSkeletonFirstLoad(6);

            return;

        }

        cancelPendingLoad();

        binding.demoEmptyLayout.setLoadingStyle(EmptyLayout.LoadingStyle.DEFAULT);

        binding.mockListContent.setVisibility(View.GONE);

        binding.demoEmptyLayout.setState(EmptyLayout.State.NETWORK_LOADING);

        updateStateLabel();

        scheduleLoadSuccess(false);

    }



    /** 骨架屏重试刷新：保留列表，覆盖展示指定行数骨架屏。 */

    private void simulateRefreshLoad(int rowCount) {

        cancelPendingLoad();

        applySkeletonConfig(rowCount);

        binding.mockListContent.setVisibility(View.VISIBLE);

        binding.demoEmptyLayout.setState(EmptyLayout.State.NETWORK_LOADING_REFRESH);

        updateStateLabel();

        scheduleLoadSuccess(true);

    }



    private void applySkeletonConfig(int rowCount) {

        binding.demoEmptyLayout.setSkeletonLoadingEnabled(true);

        binding.demoEmptyLayout.setSkeletonRowCount(rowCount);

        binding.demoEmptyLayout.setSkeletonShimmerEnabled(true);

    }



    private void scheduleLoadSuccess(boolean keepListVisible) {

        pendingLoadFinish = () -> {

            binding.demoEmptyLayout.setState(EmptyLayout.State.HIDE_LAYOUT);

            binding.mockListContent.setVisibility(View.VISIBLE);

            updateStateLabel();

            showToast(keepListVisible ? "刷新完成" : "首刷加载完成");

        };

        handler.postDelayed(pendingLoadFinish, MOCK_LOAD_DELAY_MS);

    }



    private void showNoData() {

        cancelPendingLoad();

        binding.mockListContent.setVisibility(View.GONE);

        binding.demoEmptyLayout.setNoDataContent("暂无列表数据，可点击下方重试");

        binding.demoEmptyLayout.setState(EmptyLayout.State.NO_DATA_ENABLE_CLICK);

        updateStateLabel();

    }



    private void showError() {

        cancelPendingLoad();

        binding.mockListContent.setVisibility(View.GONE);

        binding.demoEmptyLayout.setState(EmptyLayout.State.LOADING_ERROR);

        binding.demoEmptyLayout.setErrorMessage("网络异常，请点击重试");

        updateStateLabel();

    }



    private void showContent() {

        cancelPendingLoad();

        binding.demoEmptyLayout.setState(EmptyLayout.State.HIDE_LAYOUT);

        binding.mockListContent.setVisibility(View.VISIBLE);

        updateStateLabel();

    }



    private void cancelPendingLoad() {

        if (pendingLoadFinish != null) {

            handler.removeCallbacks(pendingLoadFinish);

            pendingLoadFinish = null;

        }

    }



    private void updateStateLabel() {

        EmptyLayout emptyLayout = binding.demoEmptyLayout;

        String style = emptyLayout.isSkeletonLoadingEnabled() ? "skeleton" : "spinner";

        binding.tvCurrentState.setText(getString(

                R.string.empty_layout_demo_state_fmt,

                emptyLayout.getCurrentState().name(),

                style,

                emptyLayout.isSkeletonLoadingActive(),

                emptyLayout.getSkeletonRowCount()));

    }



    @Override

    protected void onDestroy() {

        cancelPendingLoad();

        super.onDestroy();

    }

}


