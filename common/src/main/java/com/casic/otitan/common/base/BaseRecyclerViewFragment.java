package com.casic.otitan.common.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.casic.otitan.common.R;
import com.casic.otitan.common.bean.base.PageBean;
import com.casic.otitan.common.utils.log.LogUtil;
import com.casic.otitan.common.utils.network.NetworkStateUtil;
import com.casic.otitan.common.widget.empty.EmptyLayout;
import com.casic.otitan.common.widget.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2017/11/17.
 * 列表式fragment的BaseRecyclerViewFragment封装
 */
public abstract class BaseRecyclerViewFragment<VM extends BaseRecyclerViewModel, VDB extends ViewDataBinding, T> extends BaseFragment<VM, VDB> implements BaseRecyclerViewAdapter.OnItemClickListener,
        BaseRecyclerViewAdapter.OnItemLongClickListener, EmptyLayout.OnEmptyLayoutClickListener, OnRefreshListener, OnLoadMoreListener {
    private RecyclerView mRecyclerView;
    protected EmptyLayout emptyLayout;
    protected SmartRefreshLayout refreshLayout;
    private boolean isCanRefresh = true;
    private boolean isCanLoadMore = true;
    protected int mCurrentPage = 0;
    public BaseRecyclerViewAdapter<T, ?> adapter;

    protected final Observer<PageBean<T>> observer = responseBean -> {
        if (responseBean == null) {
            setListData(new ArrayList<>());
        } else {
            setListData(responseBean.getList());
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.smartrecyclerview;
    }

    @Override
    protected void initData(Bundle bundle) {
        mViewModel.getListLiveData().observe(this, observer);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mRecyclerView = binding.getRoot().findViewById(R.id.recyclerview);
        emptyLayout = binding.getRoot().findViewById(R.id.emptyLayout);
        refreshLayout = binding.getRoot().findViewById(R.id.smartFreshLayout);
        adapter = getRecyclerAdapter();
        getRecyclerView().setAdapter(adapter);
        getRecyclerView().setLayoutManager(initLayoutManager());
        if (!hideRecycleViewDivider()) {
            getRecyclerView().addItemDecoration(createDivider());
        }
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);

        emptyLayout.setOnEmptyLayoutClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);

        if (NetworkStateUtil.isConnected(requireContext())) {
            setRecyclerViewVisibility(EmptyLayout.State.NETWORK_LOADING);
        } else {
            setRecyclerViewVisibility(EmptyLayout.State.LOADING_ERROR);
        }
    }
    protected RecyclerView.ItemDecoration createDivider() {
        return new RecycleViewDivider(requireContext(), LinearLayoutManager.HORIZONTAL, 1,
                ContextCompat.getColor(requireActivity(), R.color.h_line_color));
    }

    protected void requestData() {

    }

    /**
     * 给列表添加数据
     *
     * @param listData
     */
    @SuppressLint("NotifyDataSetChanged")
    protected void setListData(List<T> listData) {
        try {
            boolean isRefresh = refreshLayout.getState() == RefreshState.Refreshing || (emptyLayout.getCurrentState() == EmptyLayout.State.NETWORK_LOADING && mCurrentPage == 1) ||
                    emptyLayout.getCurrentState() == EmptyLayout.State.NETWORK_LOADING_REFRESH;
            if (isRefresh) {
                onRefreshFinish(true);
                adapter.setList(listData);
            } else {
                if (listData == null || listData.isEmpty()) {
                    onLoadFinishNoData(true);
                } else {
                    onLoadFinish(true);
                }
                adapter.addAll(listData);
            }
            adapter.notifyDataSetChanged();
            if (adapter == null || adapter.getList() == null || adapter.getList().isEmpty()) {
                setRecyclerViewVisibility(EmptyLayout.State.NO_DATA);
            } else {
                setRecyclerViewVisibility(EmptyLayout.State.HIDE_LAYOUT);
            }
        } catch (Exception e) {
            LogUtil.show(TAG,"| BasePresenterRecyclerViewFragment解析数据:" + e);
            e.printStackTrace();
            setRecyclerViewVisibility(EmptyLayout.State.LOADING_ERROR);
            showToast(BaseException.PARSE_ERROR_MSG);
        }
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<T> rows) {
        adapter.addAll(rows);
        adapter.notifyDataSetChanged();
    }

    protected RecyclerView.LayoutManager initLayoutManager() {
        return new LinearLayoutManager(requireContext());
    }

    protected boolean hideRecycleViewDivider() {
        return false;
    }

    protected abstract BaseRecyclerViewAdapter<T, ?> getRecyclerAdapter();

    @Override
    public void onLoginSuccessCallback(Bundle bundle) {
        super.onLoginSuccessCallback(bundle);
        setRecyclerViewVisibility(EmptyLayout.State.NETWORK_LOADING);
        onRefresh();
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void onErrorCode(BaseResponse model) {
        try {
            if (refreshLayout.getState() == RefreshState.Refreshing || emptyLayout.getCurrentState() == EmptyLayout.State.NETWORK_LOADING ||
                    emptyLayout.getCurrentState() == EmptyLayout.State.NETWORK_LOADING_REFRESH || refreshLayout.getState() == RefreshState.Loading) {
                setRecyclerViewVisibility(EmptyLayout.State.LOADING_ERROR);
            }
            onRefreshFinish(false);
            onLoadFinish(false);
            if (errorService == null||model==null) {
                return;
            }
            if (errorService.isLoginPast(model.getCode())) {
                errorService.toLogin(requireContext(),authManager.getLauncher());
                return;
            }
            if (!errorService.hasPermission(model.getCode())) {
                errorService.toNoPermission(requireContext(),authManager.getLauncher());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setCanRefresh(boolean isCanRefresh) {
        this.isCanRefresh = isCanRefresh;
        refreshLayout.setEnableRefresh(isCanRefresh);
    }

    protected void setCanLoadMore(boolean isCanLoadMore) {
        this.isCanLoadMore = isCanLoadMore;
        refreshLayout.setEnableLoadMore(isCanLoadMore);
    }

    public boolean isLoadMore() {
        return isCanLoadMore;
    }

    public boolean isCanRefresh() {
        return isCanRefresh;
    }

    public EmptyLayout.State getEmptyType() {
        return emptyLayout.getCurrentState();
    }

    protected void setRecyclerViewVisibility(EmptyLayout.State emptyType) {
        if (emptyLayout == null || getRecyclerView() == null) {
            return;
        }
        emptyLayout.setState(emptyType);
        if (Objects.requireNonNull(emptyType) == EmptyLayout.State.LOADING_ERROR) {
            getRecyclerView().setVisibility(View.GONE);
            onRefreshFinish(false);
            onLoadFinish(false);
        } else if (emptyType == EmptyLayout.State.NETWORK_LOADING) {
            emptyLayout.setVisibility(View.VISIBLE);
            getRecyclerView().setVisibility(View.GONE);
            //刷新,加载
        } else if (emptyType == EmptyLayout.State.NETWORK_LOADING_REFRESH) {
            emptyLayout.setVisibility(View.GONE);
            getRecyclerView().setVisibility(View.VISIBLE);
        } else if (emptyType == EmptyLayout.State.NO_DATA) {
            onRefreshFinish(true);
            onLoadFinish(true);
            getRecyclerView().setVisibility(View.GONE);
        } else if (emptyType == EmptyLayout.State.HIDE_LAYOUT) {
            onRefreshFinish(true);
            onLoadFinish(true);
            emptyLayout.setVisibility(View.GONE);
            getRecyclerView().setVisibility(View.VISIBLE);
        }
    }

    protected void onRefreshFinish(boolean isSuccess) {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh(isSuccess);//传入false表示刷新失败
        }
    }

    protected void onLoadFinish(boolean isSuccess) {
        if (refreshLayout != null) {
            refreshLayout.finishLoadMore(isSuccess);//传入false表示刷新失败
        }
    }

    protected void onLoadFinishNoData(boolean isNoData) {
        if (refreshLayout != null) {
            refreshLayout.finishLoadMoreWithNoMoreData();//没有更多数据了
        }
    }

    @Override
    public void onEmptyLayoutClick(View v) {
        setRecyclerViewVisibility(EmptyLayout.State.NETWORK_LOADING_REFRESH);
        onRefresh();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        mCurrentPage++;
        requestData();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mCurrentPage = 0;
        requestData();
    }

    public void onRefresh() {
        setRecyclerViewVisibility(EmptyLayout.State.NETWORK_LOADING);
        onRefresh(refreshLayout);
    }

}
