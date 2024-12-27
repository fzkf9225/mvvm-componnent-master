package pers.fz.mvvm.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

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

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.PagingFooterAdapter;
import pers.fz.mvvm.listener.PagingAdapterListener;
import pers.fz.mvvm.viewmodel.BasePagingViewModel;
import pers.fz.mvvm.wight.empty.EmptyLayout;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2017/11/17.
 * 列表式fragment的BaseRecyclerViewFragment封装
 */
public abstract class BasePagingFragment<VM extends BasePagingViewModel, VDB extends ViewDataBinding, T> extends BaseFragment<VM, VDB>
        implements PagingAdapterListener<T>, EmptyLayout.OnEmptyLayoutClickListener, SwipeRefreshLayout.OnRefreshListener{
    protected RecyclerView mRecyclerView;
    protected EmptyLayout emptyLayout;
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
        refreshLayout = binding.getRoot().findViewById(R.id.swipeRefreshLayout);
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
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING);
    }

    protected ConcatAdapter createdHeaderFootAdapter(){
        return adapter.withLoadStateFooter(new PagingFooterAdapter(() -> adapter.retry()));
    }

    protected Function1<CombinedLoadStates, Unit> loadStateListener = loadStates -> {
        // 处理下拉刷新逻辑
        if (loadStates.getRefresh() instanceof LoadState.NotLoading) {
            refreshLayout.setRefreshing(false);
            if (adapter.getItemCount() == 0) {
                setRecyclerViewVisibility(EmptyLayout.NO_DATA);
            } else {
                setRecyclerViewVisibility(EmptyLayout.HIDE_LAYOUT);
            }
        } else if (loadStates.getRefresh() instanceof LoadState.Loading) {
//            refreshLayout.setRefreshing(true);
        } else if (loadStates.getRefresh() instanceof LoadState.Error) {
            LoadState.Error state = (LoadState.Error) loadStates.getRefresh();
            refreshLayout.setRefreshing(false);
//            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
        }
        return null;
    };

    protected final Observer<? super PagingData<T>> observer = responseBean -> adapter.submitData(getLifecycle(), responseBean);

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireContext());
    }

    protected RecyclerView.ItemDecoration createDivider() {
        return new RecycleViewDivider(requireContext(), LinearLayoutManager.HORIZONTAL, 1,
                ContextCompat.getColor(requireContext(), R.color.h_line_color));
    }

    protected boolean hideRecycleViewDivider() {
        return false;
    }

    protected abstract BasePagingAdapter<T, ?> getRecyclerAdapter();

    @Override
    protected void onLoginSuccessCallback(Bundle bundle) {
        super.onLoginSuccessCallback(bundle);
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING);
        onRefresh();
    }

    @Override
    public void onItemClick(View view, T item, int position) {

    }

    @Override
    public void onItemLongClick(View view, T item, int position) {

    }

    @Override
    public void onErrorCode(BaseModelEntity model) {
        try {
            if (mViewModel != null) {
                setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
            }
            if (errorService == null || model == null) {
                return;
            }
            if (!errorService.isLoginPast(model.getCode())) {
                errorService.toLogin(requireContext(), loginLauncher);
                return;
            }
            if (!errorService.hasPermission(model.getCode())) {
                errorService.toNoPermission(requireContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getEmptyType() {
        return emptyLayout.getErrorState();
    }

    protected void setRecyclerViewVisibility(int emptyType) {
        if (emptyLayout == null || getRecyclerView() == null) {
            return;
        }
        emptyLayout.setErrorType(emptyType);
    }

    @Override
    public void onEmptyLayoutClick(View v) {
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING_REFRESH);
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
