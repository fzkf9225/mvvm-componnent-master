package pers.fz.mvvm.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.PagingFooterAdapter;
import pers.fz.mvvm.inter.PagingView;
import pers.fz.mvvm.listener.PagingAdapterListener;
import pers.fz.mvvm.viewmodel.PagingViewModel;
import pers.fz.mvvm.wight.empty.EmptyLayout;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2017/11/17.
 * 列表式fragment的BaseRecyclerViewFragment封装
 */
public abstract class BaseSmartPagingFragment<VM extends PagingViewModel, VDB extends ViewDataBinding, T> extends BaseFragment<VM, VDB>
        implements PagingAdapterListener<T>, EmptyLayout.OnEmptyLayoutClickListener, OnRefreshListener, PagingView {
    private RecyclerView mRecyclerView;
    protected EmptyLayout emptyLayout;
    protected SmartRefreshLayout refreshLayout;
    public BasePagingAdapter<T, ?> adapter;

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
        adapter = getRecyclerAdapter();
        getRecyclerView().setLayoutManager(createLayoutManager());
        if (!hideRecycleViewDivider()) {
            getRecyclerView().addItemDecoration(createDivider());
        }
        adapter.setOnAdapterListener(this);
        mRecyclerView.setAdapter(adapter.withLoadStateFooter(new PagingFooterAdapter(() -> adapter.retry())));

        emptyLayout.setOnEmptyLayoutClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        // 监听加载状态
        adapter.addLoadStateListener(loadStateListener);
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING);
    }

    Function1<CombinedLoadStates, Unit> loadStateListener = loadStates -> {
        // 处理下拉刷新逻辑
        if (loadStates.getRefresh() instanceof LoadState.NotLoading) {
            refreshLayout.finishRefresh(true);
            if (adapter.getRealItemCount() == 0) {
                setRecyclerViewVisibility(EmptyLayout.NO_DATA);
            } else {
                setRecyclerViewVisibility(EmptyLayout.HIDE_LAYOUT);
            }
        } else if (loadStates.getRefresh() instanceof LoadState.Loading) {

        } else if (loadStates.getRefresh() instanceof LoadState.Error) {
            LoadState.Error state = (LoadState.Error) loadStates.getRefresh();
            refreshLayout.finishRefresh(false);
            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
        }
        return null;
    };

    protected final Observer<? super PagingData<T>> observer = responseBean -> adapter.submitData(getLifecycle(), responseBean);

    protected void requestData() {

    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    protected RecyclerView.ItemDecoration createDivider() {
        return new RecycleViewDivider(requireContext(), LinearLayoutManager.HORIZONTAL, 1,
                ContextCompat.getColor(requireActivity(), R.color.h_line_color));
    }

    protected boolean hideRecycleViewDivider() {
        return false;
    }

    protected abstract BasePagingAdapter<T, ?> getRecyclerAdapter();

    @Override
    protected void onLoginSuccessCallback(Bundle bundle) {
        super.onLoginSuccessCallback(bundle);
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING);
        adapter.refresh();
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
            if (refreshLayout.getState() == RefreshState.Refreshing || emptyLayout.getErrorState() == EmptyLayout.NETWORK_LOADING ||
                    emptyLayout.getErrorState() == EmptyLayout.NETWORK_LOADING_REFRESH || refreshLayout.getState() == RefreshState.Loading) {
                setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
            }
            onRefreshFinish(false);
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

    protected void onRefreshFinish(boolean isSuccess) {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh(isSuccess);
            //传入false表示刷新失败
        }
    }

    @Override
    public void onEmptyLayoutClick(View v) {
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING_REFRESH);
        adapter.refresh();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
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
