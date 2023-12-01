package pers.fz.mvvm.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.BasePagingAdapter;
import pers.fz.mvvm.adapter.PagingFooterAdapter;
import pers.fz.mvvm.inter.PagingView;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.networkTools.NetworkStateUtil;
import pers.fz.mvvm.viewmodel.PagingViewModel;
import pers.fz.mvvm.wight.empty.EmptyLayout;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2017/11/17.
 * 列表式fragment的BaseRecyclerViewFragment封装
 */
public abstract class BasePagingFragment<VM extends PagingViewModel, VDB extends ViewDataBinding, T> extends BaseFragment<VM, VDB> implements BasePagingAdapter.OnItemClickListener,
        BasePagingAdapter.OnItemLongClickListener, EmptyLayout.OnEmptyLayoutClickListener, SwipeRefreshLayout.OnRefreshListener, PagingView {
    private RecyclerView mRecyclerView;
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
        getRecyclerView().setAdapter(adapter);
        getRecyclerView().setLayoutManager(initLayoutManager());
        if (!hideRecycleViewDivider()) {
            getRecyclerView().addItemDecoration(new RecycleViewDivider(requireContext(), LinearLayoutManager.HORIZONTAL, 1,
                    ContextCompat.getColor(requireActivity(), R.color.h_line_color)));
        }
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        // 添加 footer
        mRecyclerView.setAdapter(adapter.withLoadStateFooter(new PagingFooterAdapter(() -> adapter.retry())));
        emptyLayout.setOnEmptyLayoutClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        // 监听加载状态
        adapter.addLoadStateListener(loadStates -> {
            // 处理下拉刷新逻辑
            if (loadStates.getRefresh() instanceof LoadState.NotLoading) {
                refreshLayout.setRefreshing(false);
                if (adapter.getRealItemCount() == 0) {
                    setRecyclerViewVisibility(EmptyLayout.NO_DATA);
                } else {
                    setRecyclerViewVisibility(EmptyLayout.HIDE_LAYOUT);
                }
            } else if (loadStates.getRefresh() instanceof LoadState.Loading) {
                refreshLayout.setRefreshing(true);
            } else if (loadStates.getRefresh() instanceof LoadState.Error) {
                LoadState.Error state = (LoadState.Error) loadStates.getRefresh();
                refreshLayout.setRefreshing(false);
                setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
            }
            return null;
        });
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING);
    }

    protected final Observer<? super PagingData<T>> observer = responseBean -> adapter.submitData(getLifecycle(), responseBean);

    protected void requestData() {

    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected RecyclerView.LayoutManager initLayoutManager() {
        return new LinearLayoutManager(getActivity());
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
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onErrorCode(BaseModelEntity model) {
        try {
            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
            if (errorService == null || model == null) {
                return;
            }
            if (!errorService.isLogin(model.getCode())) {
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
        adapter.refresh();
    }

    @Override
    public void onRefresh() {
        adapter.refresh();
    }
}
