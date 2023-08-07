package pers.fz.mvvm.base;

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

import pers.fz.mvvm.R;
import pers.fz.mvvm.bean.base.PageBean;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.log.ToastUtils;
import pers.fz.mvvm.util.networkTools.NetworkStateUtil;
import pers.fz.mvvm.wight.empty.EmptyLayout;
import pers.fz.mvvm.wight.recyclerview.FullyLinearLayoutManager;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;
import pers.fz.mvvm.wight.recyclerview.TxSlideRecyclerView;

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
    protected int mCurrentPage = 1;
    public BaseRecyclerViewAdapter<T, ?> adapter;

    protected final Observer<PageBean<T>> observer = (Observer<PageBean<T>>) responseBean -> {
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
            getRecyclerView().addItemDecoration(new RecycleViewDivider(getActivity(), LinearLayoutManager.HORIZONTAL, 1,
                    ContextCompat.getColor(requireActivity(), R.color.h_line_color)));
        }
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);

        emptyLayout.setOnEmptyLayoutClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);

        if (NetworkStateUtil.isConnected(getActivity())) {
            setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING);
        } else {
            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
        }
    }

    protected void requestData() {

    }

    /**
     * 给列表添加数据
     *
     * @param listData
     */
    protected void setListData(List<T> listData) {
        try {
            boolean isRefresh = refreshLayout.getState() == RefreshState.Refreshing || (emptyLayout.getErrorState() == EmptyLayout.NETWORK_LOADING && mCurrentPage == 1) ||
                    emptyLayout.getErrorState() == EmptyLayout.NETWORK_LOADING_RERESH;
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
                setRecyclerViewVisibility(EmptyLayout.NODATA);
            } else {
                setRecyclerViewVisibility(EmptyLayout.HIDE_LAYOUT);
            }
        } catch (Exception e) {
            LogUtil.show(TAG,"| BasePresenterRecyclerViewFragment解析数据:" + e);
            e.printStackTrace();
            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
            ToastUtils.showShort(getActivity(), BaseException.PARSE_ERROR_MSG);
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
        return new FullyLinearLayoutManager(getActivity());
    }

    protected boolean hideRecycleViewDivider() {
        return false;
    }

    public void setCusTomDecoration(RecycleViewDivider recycleViewDivider) {
        getRecyclerView().addItemDecoration(recycleViewDivider);
    }

    protected abstract BaseRecyclerViewAdapter<T, ?> getRecyclerAdapter();


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
            if (refreshLayout.getState() == RefreshState.Refreshing || emptyLayout.getErrorState() == EmptyLayout.NETWORK_LOADING ||
                    emptyLayout.getErrorState() == EmptyLayout.NETWORK_LOADING_RERESH || refreshLayout.getState() == RefreshState.Loading) {
                setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR);
            }
            onRefreshFinish(false);
            onLoadFinish(false);
            if (errorService == null||model==null) {
                return;
            }
            if (!errorService.isLogin(model.getCode())) {
                errorService.toLogin(requireContext(),loginLauncher);
                return;
            }
            if (!errorService.hasPermission(model.getCode())) {
                errorService.toNoPermission(requireContext());
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

    public int getEmptyType() {
        return emptyLayout.getErrorState();
    }

    protected void setRecyclerViewVisibility(int emptyType) {
        if (emptyLayout == null || getRecyclerView() == null) {
            return;
        }
        emptyLayout.setErrorType(emptyType);
        switch (emptyType) {
            case EmptyLayout.LOADING_ERROR:
                getRecyclerView().setVisibility(View.GONE);
                onRefreshFinish(false);
                onLoadFinish(false);
                break;
            case EmptyLayout.NETWORK_LOADING:
                emptyLayout.setVisibility(View.VISIBLE);
                getRecyclerView().setVisibility(View.GONE);
                break;
            //刷新,加载
            case EmptyLayout.NETWORK_LOADING_RERESH:
            case EmptyLayout.NETWORK_LOADING_LOADMORE:
                emptyLayout.setVisibility(View.GONE);
                getRecyclerView().setVisibility(View.VISIBLE);
                break;
            case EmptyLayout.NODATA:
                onRefreshFinish(true);
                onLoadFinish(true);
                getRecyclerView().setVisibility(View.GONE);
                break;
            case EmptyLayout.HIDE_LAYOUT:
                onRefreshFinish(true);
                onLoadFinish(true);
                emptyLayout.setVisibility(View.GONE);
                getRecyclerView().setVisibility(View.VISIBLE);
                break;
            default:
                break;
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
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING_RERESH);
        onRefresh();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        mCurrentPage++;
        requestData();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mCurrentPage = 1;
        requestData();
    }

    public void onRefresh() {
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING);
        onRefresh(refreshLayout);
    }

}
