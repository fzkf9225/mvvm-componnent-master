package io.coderf.arklab.common.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.adapter.PagingFooterAdapter;
import io.coderf.arklab.common.listener.PagingAdapterListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.viewmodel.BasePagingViewModel;
import io.coderf.arklab.common.widget.empty.EmptyLayout;
import io.coderf.arklab.common.widget.feedback.SkeletonLayout;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * 列表分页 Fragment 基类，默认使用 {@link EmptyLayout} 展示加载/空态/错误；
 * 子类可通过 {@link #enableSkeletonLoading()} 开启骨架屏首刷加载（默认关闭，保持原有行为）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/11/17
 */
public abstract class BasePagingFragment<VM extends BasePagingViewModel, VDB extends ViewDataBinding, T> extends BaseFragment<VM, VDB>
        implements PagingAdapterListener<T>, EmptyLayout.OnEmptyLayoutClickListener, SwipeRefreshLayout.OnRefreshListener{
    protected RecyclerView mRecyclerView;
    protected EmptyLayout emptyLayout;
    @Nullable
    protected SkeletonLayout skeletonLayout;
    protected SwipeRefreshLayout refreshLayout;
    public BasePagingAdapter<T, ?> adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.paging_recycler_view;
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mRecyclerView = binding.getRoot().findViewById(R.id.mRecyclerview);
        emptyLayout = binding.getRoot().findViewById(R.id.mEmptyLayout);
        skeletonLayout = binding.getRoot().findViewById(R.id.mSkeletonLayout);
        refreshLayout = binding.getRoot().findViewById(R.id.swipeRefreshLayout);
        if (enableSkeletonLoading() && skeletonLayout != null) {
            configureSkeletonLayout(skeletonLayout);
        }
        adapter = getRecyclerAdapter();
        getRecyclerView().setLayoutManager(createLayoutManager());
        if (!hideRecycleViewDivider()) {
            getRecyclerView().addItemDecoration(createDivider());
        }
        adapter.setOnAdapterListener(this);
        // 添加 footer
        mRecyclerView.setAdapter(createdHeaderFootAdapter());
        emptyLayout.setOnEmptyLayoutClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        // 监听加载状态
        adapter.addLoadStateListener(loadStateListener);
        setViewState(EmptyLayout.State.NETWORK_LOADING);
    }

    protected ConcatAdapter createdHeaderFootAdapter() {
        ConcatAdapter mainWithFooter = adapter.withLoadStateFooter(
                new PagingFooterAdapter(() -> adapter.retry(), adapter));
        RecyclerView.Adapter<? extends RecyclerView.ViewHolder> header = adapter.getPagingHeaderAdapter();
        if (header != null) {
            ConcatAdapter.Config config = new ConcatAdapter.Config.Builder()
                    .setIsolateViewTypes(true)
                    .build();
            return new ConcatAdapter(config, header, mainWithFooter);
        }
        return mainWithFooter;
    }

    protected Function1<CombinedLoadStates, Unit> loadStateListener = loadStates -> {
        // 处理下拉刷新逻辑
        if (loadStates.getRefresh() instanceof LoadState.NotLoading) {
            refreshLayout.setRefreshing(false);
            if (adapter.getItemCount() == 0) {
                setViewState(EmptyLayout.State.NO_DATA);
            } else {
                setViewState(EmptyLayout.State.HIDE_LAYOUT);
            }
        } else if (loadStates.getRefresh() instanceof LoadState.Loading) {
//            refreshLayout.setRefreshing(true);
        } else if (loadStates.getRefresh() instanceof LoadState.Error) {
            refreshLayout.setRefreshing(false);
            if (shouldShowEmptyLayoutOnRefreshError()) {
                setViewState(EmptyLayout.State.LOADING_ERROR);
            }
        }
        return null;
    };

    /**
     * 仅在「尚无列表数据」且空布局处于首刷/重试加载态时展示错误页，避免有数据时遮挡列表观感。
     */
    protected boolean shouldShowEmptyLayoutOnRefreshError() {
        return adapter != null
                && adapter.getItemCount() == 0
                && isInitialLoadingVisible();
    }

    /**
     * 是否处于首刷/重试加载展示态（含骨架屏模式）。
     */
    protected boolean isInitialLoadingVisible() {
        if (enableSkeletonLoading() && skeletonLayout != null
                && skeletonLayout.getVisibility() == View.VISIBLE) {
            return true;
        }
        return emptyLayout != null && emptyLayout.isLoading();
    }

    /**
     * 是否启用骨架屏替代 EmptyLayout 的首刷加载动画，默认 false（沿用 EmptyLayout）。
     */
    protected boolean enableSkeletonLoading() {
        return false;
    }

    /**
     * 骨架屏样式配置，仅在 {@link #enableSkeletonLoading()} 为 true 时生效。
     */
    protected void configureSkeletonLayout(@NonNull SkeletonLayout skeleton) {
        skeleton.setRowCount(6);
    }

    protected final Observer<? super PagingData<T>> observer = responseBean -> adapter.submitData(getLifecycle(), responseBean);

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireContext());
    }

    protected RecyclerView.ItemDecoration createDivider() {
        return new RecycleViewDivider(requireContext(),
                LinearLayoutManager.VERTICAL,
                DensityUtil.dp2px(requireActivity(), 1),
                ContextCompat.getColor(requireContext(), R.color.h_line_color));
    }

    protected boolean hideRecycleViewDivider() {
        return false;
    }

    protected abstract BasePagingAdapter<T, ?> getRecyclerAdapter();

    @Override
    public void onAuthSuccess(@Nullable Bundle data) {
        super.onAuthSuccess(data);
        setViewState(EmptyLayout.State.NETWORK_LOADING);
        onRefresh();
    }

    @Override
    public void onItemClick(View view, T item, int position) {

    }

    @Override
    public void onItemLongClick(View view, T item, int position) {

    }

    @Override
    public void onErrorCode(BaseResponse model) {
        try {
            boolean refreshError = refreshLayout.isRefreshing()
                    || isInitialLoadingVisible();
            if (refreshError && shouldShowEmptyLayoutOnRefreshError()) {
                setViewState(EmptyLayout.State.LOADING_ERROR);
            }
            refreshLayout.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onErrorCode(model);
    }

    public EmptyLayout.State getEmptyType() {
        return emptyLayout.getCurrentState();
    }

    protected void setViewState(EmptyLayout.State emptyType) {
        if (emptyLayout == null || getRecyclerView() == null) {
            return;
        }
        if (enableSkeletonLoading() && skeletonLayout != null) {
            if (emptyType == EmptyLayout.State.NETWORK_LOADING
                    || emptyType == EmptyLayout.State.NETWORK_LOADING_REFRESH) {
                skeletonLayout.showSkeleton();
                emptyLayout.setState(EmptyLayout.State.HIDE_LAYOUT);
                return;
            }
            skeletonLayout.hideSkeleton();
        }
        emptyLayout.setState(emptyType);
    }

    @Override
    public void onEmptyLayoutClick(View v) {
        setViewState(EmptyLayout.State.NETWORK_LOADING_REFRESH);
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onRefresh() {
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.removeLoadStateListener(loadStateListener);
        }
    }
}
