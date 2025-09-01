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
import androidx.recyclerview.widget.ConcatAdapter;
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
import pers.fz.mvvm.listener.PagingAdapterListener;
import pers.fz.mvvm.viewmodel.BasePagingViewModel;
import pers.fz.mvvm.widget.empty.EmptyLayout;
import pers.fz.mvvm.widget.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2017/11/17.
 * 列表式fragment的BaseRecyclerViewFragment封装
 */
public abstract class BaseSmartPagingFragment<VM extends BasePagingViewModel, VDB extends ViewDataBinding, T> extends BaseFragment<VM, VDB>
        implements PagingAdapterListener<T>, EmptyLayout.OnEmptyLayoutClickListener, OnRefreshListener {
    protected RecyclerView mRecyclerView;
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
        mRecyclerView.setAdapter(createdHeaderFootAdapter());

        emptyLayout.setOnEmptyLayoutClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        // 监听加载状态
        adapter.addLoadStateListener(loadStateListener);
        setRecyclerViewVisibility(EmptyLayout.State.NETWORK_LOADING);
    }

    protected ConcatAdapter createdHeaderFootAdapter(){
        return adapter.withLoadStateFooter(new PagingFooterAdapter(() -> adapter.retry()));
    }

   protected Function1<CombinedLoadStates, Unit> loadStateListener = loadStates -> {
        LoadState refresh = loadStates.getRefresh();
        LoadState append = loadStates.getAppend();
        if (refresh instanceof LoadState.Loading) {

        } else if (refresh instanceof LoadState.NotLoading) {
            refreshLayout.finishRefresh();
            if (adapter.getItemCount() == 0) {
                setRecyclerViewVisibility(EmptyLayout.State.NO_DATA);
            } else {
                setRecyclerViewVisibility(EmptyLayout.State.HIDE_LAYOUT);
            }
        } else if (refresh instanceof LoadState.Error) {
            refreshLayout.finishRefresh(false);
//            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
        }

//        if (append instanceof LoadState.Loading) {
//            // 加载更多时可以显示一个加载中的 UI
//        } else if (append instanceof LoadState.NotLoading) {
//            if (adapter.getRealItemCount() == 0) {
//                setRecyclerViewVisibility(EmptyLayout.NO_DATA);
//            } else {
//                setRecyclerViewVisibility(EmptyLayout.HIDE_LAYOUT);
//            }
//        } else if (append instanceof LoadState.Error) {
//            refreshLayout.finishLoadMore(false);
//        }
        return null;
    };

    protected final Observer<? super PagingData<T>> observer = responseBean -> adapter.submitData(getLifecycle(), responseBean);

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
    public void onLoginSuccessCallback(Bundle bundle) {
        super.onLoginSuccessCallback(bundle);
        setRecyclerViewVisibility(EmptyLayout.State.NETWORK_LOADING);
        adapter.refresh();
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
            boolean refreshError = refreshLayout.getState() == RefreshState.Refreshing || emptyLayout.getCurrentState() == EmptyLayout.State.NETWORK_LOADING ||
                    emptyLayout.getCurrentState() == EmptyLayout.State.NETWORK_LOADING_REFRESH || refreshLayout.getState() == RefreshState.Loading;
            if (refreshError) {
                setRecyclerViewVisibility(EmptyLayout.State.LOADING_ERROR);
            }
            onRefreshFinish(false);
            if (errorService == null || model == null) {
                return;
            }
            if (errorService.isLoginPast(model.getCode())) {
                errorService.toLogin(requireContext(), authManager.getLauncher());
                return;
            }
            if (!errorService.hasPermission(model.getCode())) {
                errorService.toNoPermission(requireContext(), authManager.getLauncher());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EmptyLayout.State getEmptyType() {
        return emptyLayout.getCurrentState();
    }

    protected void setRecyclerViewVisibility(EmptyLayout.State emptyType) {
        if (emptyLayout == null || getRecyclerView() == null) {
            return;
        }
        emptyLayout.setState(emptyType);
    }

    protected void onRefreshFinish(boolean isSuccess) {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh(isSuccess);
            //传入false表示刷新失败
        }
    }

    @Override
    public void onEmptyLayoutClick(View v) {
        setRecyclerViewVisibility(EmptyLayout.State.NETWORK_LOADING_REFRESH);
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
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
