package io.coderf.arklab.common.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.adapter.PagingFooterAdapter;
import io.coderf.arklab.common.listener.PagingAdapterListener;
import io.coderf.arklab.core.utils.DensityUtil;
import io.coderf.arklab.common.viewmodel.BasePagingViewModel;
import io.coderf.arklab.common.widget.empty.EmptyLayout;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * SmartRefresh + Paging3 列表 Fragment 基类，默认使用 {@link EmptyLayout} 展示加载/空态/错误；
 * 子类可通过 {@link #enableSkeletonLoading()} 或 XML {@code app:loadingStyle="skeleton"} 开启骨架屏（默认关闭）。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @created 2017/11/17
 * @updated 2026/11/20 不满一屏时不展示「暂无更多数据」
 */
public abstract class BaseSmartPagingFragment<VM extends BasePagingViewModel, VDB extends ViewDataBinding, T> extends BaseFragment<VM, VDB>
        implements PagingAdapterListener<T>, EmptyLayout.OnEmptyLayoutClickListener, OnRefreshListener {
    protected RecyclerView mRecyclerView;
    protected EmptyLayout emptyLayout;
    protected SmartRefreshLayout refreshLayout;
    public BasePagingAdapter<T, ?> adapter;
    private PagingFooterAdapter pagingFooter;
    private boolean noMoreHintWatchInstalled;
    private boolean noMoreHintSyncPosted;
    private int noMoreHintMeasureRetries;

    @Override
    protected int getLayoutId() {
        return R.layout.base_smart_paging;
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mRecyclerView = binding.getRoot().findViewById(R.id.mRecyclerview);
        emptyLayout = binding.getRoot().findViewById(R.id.mEmptyLayout);
        refreshLayout = binding.getRoot().findViewById(R.id.smartFreshLayout);
        applyEmptyLayoutLoadingConfig();
        adapter = getRecyclerAdapter();
        getRecyclerView().setLayoutManager(createLayoutManager());
        if (!hideRecycleViewDivider()) {
            getRecyclerView().addItemDecoration(createDivider());
        }
        adapter.setOnAdapterListener(this);
        mRecyclerView.setAdapter(createdHeaderFootAdapter());
        installNoMoreHintWatch();

        emptyLayout.setOnEmptyLayoutClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        // 监听加载状态
        adapter.addLoadStateListener(loadStateListener);
        setViewState(EmptyLayout.State.NETWORK_LOADING);
    }

    /**
     * 组装 Header? + Paging + LoadState Footer（{@link BasePagingAdapter#withLoadStateFooter}）。
     */
    protected ConcatAdapter createdHeaderFootAdapter() {
        PagingFooterAdapter footer = new PagingFooterAdapter(
                () -> adapter.retry(), adapter, emptyLayout.getConfig());
        footer.setLoadMoreEnabled(mViewModel.isLoadMoreEnabled());
        footer.setHideEndHintUntilOverflow(hideNoMoreHintWhenNotFullScreen());
        pagingFooter = footer;
        RecyclerView.Adapter<? extends RecyclerView.ViewHolder> header = adapter.getPagingHeaderAdapter();
        ConcatAdapter mainWithFooter = adapter.withLoadStateFooter(footer);
        if (header != null) {
            ConcatAdapter.Config config = new ConcatAdapter.Config.Builder()
                    .setIsolateViewTypes(true)
                    .build();
            return new ConcatAdapter(config, header, mainWithFooter);
        }
        return mainWithFooter;
    }

    protected Function1<CombinedLoadStates, Unit> loadStateListener = loadStates -> {
        LoadState refresh = loadStates.getRefresh();
        LoadState append = loadStates.getAppend();
        if (refresh instanceof LoadState.Loading) {
            if (pagingFooter != null) {
                pagingFooter.setContentOverflows(false);
            }
        } else if (refresh instanceof LoadState.NotLoading) {
            refreshLayout.finishRefresh();
            if (adapter.getContentItemCount() == 0) {
                setViewState(EmptyLayout.State.NO_DATA);
            } else {
                setViewState(EmptyLayout.State.HIDE_LAYOUT);
            }
        } else if (refresh instanceof LoadState.Error) {
            refreshLayout.finishRefresh(false);
            if (shouldShowEmptyLayoutOnRefreshError()) {
                setViewState(EmptyLayout.State.LOADING_ERROR);
            }
        }

//        if (append instanceof LoadState.Loading) {
//            // 加载更多时可以显示一个加载中的 UI
//        } else if (append instanceof LoadState.NotLoading) {
//            if (adapter.getRealItemCount() == 0) {
//                setViewState(EmptyLayout.NO_DATA);
//            } else {
//                setViewState(EmptyLayout.HIDE_LAYOUT);
//            }
//        } else if (append instanceof LoadState.Error) {
//            refreshLayout.finishLoadMore(false);
//        }
        scheduleNoMoreHintSync();
        return null;
    };

    /**
     * 仅在「尚无列表数据」且空布局处于首刷/重试加载态时展示错误页，避免有数据时遮挡列表观感。
     */
    protected boolean shouldShowEmptyLayoutOnRefreshError() {
        return adapter != null
                && adapter.getContentItemCount() == 0
                && isInitialLoadingVisible();
    }

    /**
     * 是否处于首刷/重试加载展示态（含骨架屏模式）。
     */
    protected boolean isInitialLoadingVisible() {
        return emptyLayout != null && emptyLayout.isLoading();
    }

    protected boolean enableSkeletonLoading() {
        return false;
    }

    protected void applyEmptyLayoutLoadingConfig() {
        if (emptyLayout != null && enableSkeletonLoading()) {
            emptyLayout.setSkeletonLoadingEnabled(true);
            configureEmptyLayoutSkeleton(emptyLayout);
        }
    }

    protected void configureEmptyLayoutSkeleton(@NonNull EmptyLayout layout) {
        layout.setSkeletonRowCount(6);
    }

    /**
     * 收集必须绑视图生命周期。Fragment 生命周期在 onDestroyView 后仍然活着，
     * 用 {@code getLifecycle()} 会让旧 Adapter 在旋转或返回后继续收分页。
     */
    protected final Observer<? super PagingData<T>> observer = pagingData -> {
        if (pagingData == null || adapter == null || getView() == null) {
            return;
        }
        adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
    };

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    protected RecyclerView.ItemDecoration createDivider() {
        return new RecycleViewDivider(requireContext(),
                LinearLayoutManager.VERTICAL,
                DensityUtil.dp2px(requireActivity(), 1),
                ThemeAttrs.outlineVariant(requireActivity()));
    }

    protected boolean hideRecycleViewDivider() {
        return false;
    }

    /**
     * 内容不满一屏时，不展示底部「暂无更多数据」。
     *
     * <p>默认 true：条目加起来不到一屏时不占这一行；超过一屏后，这一行在列表末尾，
     * 往上滑到末尾就能看到。加载中、加载失败仍照常显示。
     * 某一页如果到了最后一页就要始终显示这行字，重写并返回 false。</p>
     */
    protected boolean hideNoMoreHintWhenNotFullScreen() {
        return true;
    }

    private final View.OnLayoutChangeListener noMoreHintLayoutListener =
            (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> scheduleNoMoreHintSync();

    private final RecyclerView.AdapterDataObserver noMoreHintDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            scheduleNoMoreHintSync();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            scheduleNoMoreHintSync();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            scheduleNoMoreHintSync();
        }
    };

    private final Runnable noMoreHintSync = () -> {
        noMoreHintSyncPosted = false;
        syncNoMoreHintWithViewport();
    };

    private void installNoMoreHintWatch() {
        if (noMoreHintWatchInstalled || pagingFooter == null || mRecyclerView == null || adapter == null) {
            return;
        }
        if (!pagingFooter.isHideEndHintUntilOverflow()) {
            return;
        }
        mRecyclerView.addOnLayoutChangeListener(noMoreHintLayoutListener);
        adapter.registerAdapterDataObserver(noMoreHintDataObserver);
        noMoreHintWatchInstalled = true;
    }

    private void removeNoMoreHintWatch() {
        if (mRecyclerView != null) {
            mRecyclerView.removeOnLayoutChangeListener(noMoreHintLayoutListener);
            mRecyclerView.removeCallbacks(noMoreHintSync);
        }
        if (noMoreHintWatchInstalled && adapter != null) {
            adapter.unregisterAdapterDataObserver(noMoreHintDataObserver);
        }
        noMoreHintWatchInstalled = false;
        noMoreHintSyncPosted = false;
        noMoreHintMeasureRetries = 0;
    }

    private void scheduleNoMoreHintSync() {
        if (pagingFooter == null || !pagingFooter.isHideEndHintUntilOverflow() || mRecyclerView == null) {
            return;
        }
        if (noMoreHintSyncPosted) {
            return;
        }
        noMoreHintSyncPosted = true;
        mRecyclerView.post(noMoreHintSync);
    }

    /**
     * 用「去掉底部状态行之后还能不能滚」判断有没有超过一屏。
     * 只在已经到最后一页时判断，避免「正在加载」那一行把自己算进高度。
     */
    private void syncNoMoreHintWithViewport() {
        RecyclerView rv = mRecyclerView;
        PagingFooterAdapter footer = pagingFooter;
        if (rv == null || footer == null || !footer.isHideEndHintUntilOverflow()) {
            return;
        }
        if (rv.getVisibility() != View.VISIBLE || !rv.isLaidOut() || rv.getHeight() == 0) {
            return;
        }
        LoadState state = footer.getLoadState();
        if (!(state instanceof LoadState.NotLoading) || !state.getEndOfPaginationReached()) {
            return;
        }
        if (footer.getItemCount() > 0 && visibleEndHintSize(rv, scrollsVertically(rv)) < 0
                && noMoreHintMeasureRetries < 2) {
            noMoreHintMeasureRetries++;
            scheduleNoMoreHintSync();
            return;
        }
        noMoreHintMeasureRetries = 0;
        footer.setContentOverflows(contentOverflowsWithoutEndHint(rv));
    }

    private boolean scrollsVertically(@NonNull RecyclerView rv) {
        RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
        return layoutManager == null || layoutManager.canScrollVertically();
    }

    private boolean canScrollContent(@NonNull RecyclerView rv, boolean vertical) {
        return vertical
                ? rv.canScrollVertically(1) || rv.canScrollVertically(-1)
                : rv.canScrollHorizontally(1) || rv.canScrollHorizontally(-1);
    }

    private boolean contentOverflowsWithoutEndHint(@NonNull RecyclerView rv) {
        boolean vertical = scrollsVertically(rv);
        if (pagingFooter == null || pagingFooter.getItemCount() == 0) {
            return canScrollContent(rv, vertical);
        }
        int hintSize = visibleEndHintSize(rv, vertical);
        if (hintSize < 0) {
            return canScrollContent(rv, vertical);
        }
        if (vertical) {
            return rv.computeVerticalScrollRange() - hintSize > rv.computeVerticalScrollExtent();
        }
        return rv.computeHorizontalScrollRange() - hintSize > rv.computeHorizontalScrollExtent();
    }

    /**
     * @return 底部状态行的高（或宽）；行已经插入但还没出现在屏幕上时返回 -1，说明内容已经超过一屏
     */
    private int visibleEndHintSize(@NonNull RecyclerView rv, boolean vertical) {
        RecyclerView.Adapter<?> root = rv.getAdapter();
        if (!(root instanceof ConcatAdapter) || pagingFooter == null) {
            return 0;
        }
        int position = adapterPositionOf(pagingFooter, (ConcatAdapter) root, 0);
        if (position == RecyclerView.NO_POSITION) {
            return 0;
        }
        RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(position);
        if (holder == null) {
            return -1;
        }
        return vertical ? holder.itemView.getHeight() : holder.itemView.getWidth();
    }

    private int adapterPositionOf(RecyclerView.Adapter<?> target, ConcatAdapter concat, int offset) {
        for (RecyclerView.Adapter<? extends RecyclerView.ViewHolder> child : concat.getAdapters()) {
            if (child == target) {
                return offset;
            }
            if (child instanceof ConcatAdapter) {
                int nested = adapterPositionOf(target, (ConcatAdapter) child, offset);
                if (nested != RecyclerView.NO_POSITION) {
                    return nested;
                }
            }
            offset += child.getItemCount();
        }
        return RecyclerView.NO_POSITION;
    }

    protected abstract BasePagingAdapter<T, ?> getRecyclerAdapter();

    @Override
    public void onAuthSuccess(Bundle bundle) {
        super.onAuthSuccess(bundle);
        setViewState(EmptyLayout.State.NETWORK_LOADING);
        adapter.refresh();
    }

    @Override
    public void onItemClick(View view, T item, int position) {

    }

    @Override
    public void onItemLongClick(View view, T item, int position) {

    }

    @Override
    public void showError(io.coderf.arklab.core.request.AppError error) {
        if (error instanceof io.coderf.arklab.core.request.AppError.Business) {
            try {
                boolean refreshError = refreshLayout.getState() == RefreshState.Refreshing
                        || isInitialLoadingVisible()
                        || refreshLayout.getState() == RefreshState.Loading;
                if (refreshError && shouldShowEmptyLayoutOnRefreshError()) {
                    setViewState(EmptyLayout.State.LOADING_ERROR);
                }
                onRefreshFinish(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.showError(error);
    }

    public EmptyLayout.State getEmptyType() {
        return emptyLayout.getCurrentState();
    }

    protected void setViewState(EmptyLayout.State emptyType) {
        if (emptyLayout == null || getRecyclerView() == null) {
            return;
        }
        emptyLayout.setState(emptyType);
        if (emptyType == EmptyLayout.State.LOADING_ERROR
                || emptyType == EmptyLayout.State.NO_DATA) {
            getRecyclerView().setVisibility(View.GONE);
        } else if (emptyType == EmptyLayout.State.NETWORK_LOADING) {
            emptyLayout.setVisibility(View.VISIBLE);
            getRecyclerView().setVisibility(View.GONE);
        } else if (emptyType == EmptyLayout.State.NETWORK_LOADING_REFRESH) {
            emptyLayout.setVisibility(View.VISIBLE);
        } else if (emptyType == EmptyLayout.State.HIDE_LAYOUT) {
            emptyLayout.setVisibility(View.GONE);
            getRecyclerView().setVisibility(View.VISIBLE);
        }
    }

    protected void onRefreshFinish(boolean isSuccess) {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh(isSuccess);
            //传入false表示刷新失败
        }
    }

    @Override
    public void onEmptyLayoutClick(View v) {
        setViewState(EmptyLayout.State.NETWORK_LOADING_REFRESH);
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onDestroyView() {
        removeNoMoreHintWatch();
        if (adapter != null) {
            adapter.removeLoadStateListener(loadStateListener);
        }
        pagingFooter = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.removeLoadStateListener(loadStateListener);
        }
    }
}
