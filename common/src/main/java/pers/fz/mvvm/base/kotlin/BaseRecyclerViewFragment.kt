package pers.fz.mvvm.base.kotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import kotlinx.coroutines.launch
import pers.fz.mvvm.R
import pers.fz.mvvm.base.BaseException
import pers.fz.mvvm.base.BaseModelEntity
import pers.fz.mvvm.base.BaseRecyclerViewAdapter
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.bean.base.PageBean
import pers.fz.mvvm.util.log.LogUtil
import pers.fz.mvvm.util.log.ToastUtils
import pers.fz.mvvm.util.networkTools.NetworkStateUtil
import pers.fz.mvvm.wight.empty.EmptyLayout
import pers.fz.mvvm.wight.empty.EmptyLayout.OnEmptyLayoutClickListener
import pers.fz.mvvm.wight.recyclerview.FullyLinearLayoutManager
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider

/**
 * Created by fz on 2017/11/17.
 * 列表式fragment的BaseRecyclerViewFragment封装
 */
abstract class BaseRecyclerViewFragment<BVM : BaseRecyclerViewModel<V, T>?, VDB : ViewDataBinding?, T,V:BaseView> :
    BaseFragment<BVM, VDB,V>(), BaseRecyclerViewAdapter.OnItemClickListener,
    BaseRecyclerViewAdapter.OnItemLongClickListener, OnEmptyLayoutClickListener, OnRefreshListener,
    OnLoadMoreListener {
    protected var recyclerView: RecyclerView? = null
        private set
    protected var emptyLayout: EmptyLayout? = null
    protected var refreshLayout: SmartRefreshLayout? = null
    var isCanRefresh = true
        protected set(isCanRefresh) {
            field = isCanRefresh
            refreshLayout!!.setEnableRefresh(isCanRefresh)
        }
    var isLoadMore = true
        private set
    protected var mCurrentPage = 0
    var adapter: BaseRecyclerViewAdapter<T, *>? = null
    protected val observer = Observer { responseBean: PageBean<T>? ->
        if (responseBean == null) {
            setListData(ArrayList())
        } else {
            setListData(responseBean.list)
        }
    }

    override val layoutId: Int
        get() = R.layout.smartrecyclerview
    override fun initData(bundle: Bundle?) {
        lifecycleScope.launch {
            mViewModel?.pageMutableStateFlow?.collect { value ->
                observer.onChanged(value)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView = binding!!.root.findViewById(R.id.recyclerview)
        emptyLayout = binding!!.root.findViewById(R.id.emptyLayout)
        refreshLayout = binding!!.root.findViewById(R.id.smartFreshLayout)
        adapter = recyclerAdapter
        recyclerView?.setAdapter(adapter)
        recyclerView?.setLayoutManager(initLayoutManager())
        if (!hideRecycleViewDivider()) {
            recyclerView?.addItemDecoration(
                RecycleViewDivider(
                    activity, LinearLayoutManager.HORIZONTAL, 1,
                    ContextCompat.getColor(requireActivity(), R.color.h_line_color)
                )
            )
        }
        adapter!!.setOnItemClickListener(this)
        adapter!!.setOnItemLongClickListener(this)
        emptyLayout?.setOnEmptyLayoutClickListener(this)
        refreshLayout?.setOnRefreshListener(this)
        refreshLayout?.setOnLoadMoreListener(this)
        if (NetworkStateUtil.isConnected(activity)) {
            setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING)
        } else {
            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR)
        }
    }

    protected open fun requestData() {}

    /**
     * 给列表添加数据
     *
     * @param listData
     */
    @SuppressLint("NotifyDataSetChanged")
    protected fun setListData(listData: List<T>?) {
        try {
            val isRefresh =
                refreshLayout!!.state == RefreshState.Refreshing || emptyLayout!!.errorState == EmptyLayout.NETWORK_LOADING && mCurrentPage == 1 || emptyLayout!!.errorState == EmptyLayout.NETWORK_LOADING_RERESH
            if (isRefresh) {
                onRefreshFinish(true)
                adapter!!.setList(listData)
            } else {
                if (listData == null || listData.isEmpty()) {
                    onLoadFinishNoData(true)
                } else {
                    onLoadFinish(true)
                }
                adapter!!.addAll(listData)
            }
            adapter!!.notifyDataSetChanged()
            if (adapter == null || adapter!!.list == null || adapter!!.list.isEmpty()) {
                setRecyclerViewVisibility(EmptyLayout.NODATA)
            } else {
                setRecyclerViewVisibility(EmptyLayout.HIDE_LAYOUT)
            }
        } catch (e: Exception) {
            LogUtil.show(TAG, "| BasePresenterRecyclerViewFragment解析数据:$e")
            e.printStackTrace()
            setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR)
            ToastUtils.showShort(activity, BaseException.PARSE_ERROR_MSG)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(rows: List<T>?) {
        adapter!!.addAll(rows)
        adapter!!.notifyDataSetChanged()
    }

    protected fun initLayoutManager(): RecyclerView.LayoutManager {
        return FullyLinearLayoutManager(activity)
    }

    protected fun hideRecycleViewDivider(): Boolean {
        return false
    }

    fun setCusTomDecoration(recycleViewDivider: RecycleViewDivider?) {
        recyclerView!!.addItemDecoration(recycleViewDivider!!)
    }

    protected abstract val recyclerAdapter: BaseRecyclerViewAdapter<T, *>?
    override fun onLoginSuccessCallback(bundle: Bundle?) {
        super.onLoginSuccessCallback(bundle)
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING)
        onRefresh()
    }

    override fun onItemClick(view: View, position: Int) {}
    override fun onItemLongClick(view: View, position: Int) {}
    override fun onErrorCode(model: BaseModelEntity<*>?) {
        try {
            if (refreshLayout!!.state == RefreshState.Refreshing || emptyLayout!!.errorState == EmptyLayout.NETWORK_LOADING || emptyLayout!!.errorState == EmptyLayout.NETWORK_LOADING_RERESH || refreshLayout!!.state == RefreshState.Loading) {
                setRecyclerViewVisibility(EmptyLayout.LOADING_ERROR)
            }
            onRefreshFinish(false)
            onLoadFinish(false)
            if (errorService == null || model == null) {
                return
            }
            if (!errorService.isLogin(model.code)) {
                errorService.toLogin(requireContext(), loginLauncher)
                return
            }
            if (!errorService.hasPermission(model.code)) {
                errorService.toNoPermission(requireContext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun setCanLoadMore(isCanLoadMore: Boolean) {
        isLoadMore = isCanLoadMore
        refreshLayout!!.setEnableLoadMore(isCanLoadMore)
    }

    val emptyType: Int
        get() = emptyLayout!!.errorState

    protected fun setRecyclerViewVisibility(emptyType: Int) {
        if (emptyLayout == null || recyclerView == null) {
            return
        }
        emptyLayout!!.setErrorType(emptyType)
        when (emptyType) {
            EmptyLayout.LOADING_ERROR -> {
                recyclerView!!.visibility = View.GONE
                onRefreshFinish(false)
                onLoadFinish(false)
            }

            EmptyLayout.NETWORK_LOADING -> {
                emptyLayout!!.visibility = View.VISIBLE
                recyclerView!!.visibility = View.GONE
            }

            EmptyLayout.NETWORK_LOADING_RERESH, EmptyLayout.NETWORK_LOADING_LOADMORE -> {
                emptyLayout!!.visibility = View.GONE
                recyclerView!!.visibility = View.VISIBLE
            }

            EmptyLayout.NODATA -> {
                onRefreshFinish(true)
                onLoadFinish(true)
                recyclerView!!.visibility = View.GONE
            }

            EmptyLayout.HIDE_LAYOUT -> {
                onRefreshFinish(true)
                onLoadFinish(true)
                emptyLayout!!.visibility = View.GONE
                recyclerView!!.visibility = View.VISIBLE
            }

            else -> {}
        }
    }

    protected fun onRefreshFinish(isSuccess: Boolean) {
        if (refreshLayout != null) {
            refreshLayout!!.finishRefresh(isSuccess) //传入false表示刷新失败
        }
    }

    protected fun onLoadFinish(isSuccess: Boolean) {
        if (refreshLayout != null) {
            refreshLayout!!.finishLoadMore(isSuccess) //传入false表示刷新失败
        }
    }

    protected fun onLoadFinishNoData(isNoData: Boolean) {
        if (refreshLayout != null) {
            refreshLayout!!.finishLoadMoreWithNoMoreData() //没有更多数据了
        }
    }

    override fun onEmptyLayoutClick(v: View) {
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING_RERESH)
        onRefresh()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        mCurrentPage++
        requestData()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mCurrentPage = 0
        requestData()
    }

    fun onRefresh() {
        setRecyclerViewVisibility(EmptyLayout.NETWORK_LOADING)
        onRefresh(refreshLayout!!)
    }
}