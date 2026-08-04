package io.coderf.arklab.common.fragment

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingData
import io.coderf.arklab.common.R
import io.coderf.arklab.common.adapter.GridMenuAdapter
import io.coderf.arklab.common.base.BaseFragment
import io.coderf.arklab.common.base.BasePagingAdapter
import io.coderf.arklab.common.bean.GridMenuBean
import io.coderf.arklab.common.databinding.FragmentGridMenuBinding
import io.coderf.arklab.common.listener.PagingAdapterListener
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.viewmodel.EmptyViewModel
import io.coderf.arklab.common.widget.customview.GridMenuView
import io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration

class GridMenuFragment<T : GridMenuBean> : BaseFragment<EmptyViewModel, FragmentGridMenuBinding>() {
    private var menuList: List<GridMenuBean>? = null
    private var adapter: BasePagingAdapter<GridMenuBean, ViewDataBinding>? = null
    private val defaultAdapter by lazy {
        GridMenuAdapter(gridMenuView).apply {
            setOnAdapterListener(gridMenuView?.getAdapterListener() as PagingAdapterListener<GridMenuBean?>?)
        }
    }
    private var gridMenuView: GridMenuView? = null
    override fun getLayoutId() = R.layout.fragment_grid_menu

    override fun initView(savedInstanceState: Bundle?) {
        binding.mRecyclerviewMenu.layoutManager =
            object : FullyGridLayoutManager(requireContext(), gridMenuView?.getColumnCount() ?: 4) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        val columnGap = gridMenuView?.columnMargin ?: DensityUtil.dp2px(requireContext(), 8f)
        val rowGap = gridMenuView?.getRowMarginOrDefault() ?: columnGap
        binding.mRecyclerviewMenu.addItemDecoration(
            GridSpacingItemDecoration.spacingOnly(columnGap, rowGap)
        )
        adapter =
            (gridMenuView?.customGridMenuAdapterCallback?.getAdapter<GridMenuBean, ViewDataBinding>()
                ?: defaultAdapter) as BasePagingAdapter<GridMenuBean, ViewDataBinding>?
        binding.mRecyclerviewMenu.adapter = adapter

        binding.mRecyclerviewMenu.setPadding(
            gridMenuView?.contentPaddingStart ?: DensityUtil.dp2px(requireContext(), 12f),
            0,
            gridMenuView?.contentPaddingEnd ?: DensityUtil.dp2px(requireContext(), 12f),
            0
        )
    }

    override fun initData(bundle: Bundle?) {
        menuList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelableArrayList(DATA, GridMenuBean::class.java)
        } else {
            bundle?.getParcelableArrayList(DATA)
        }
        adapter?.submitData(lifecycle, PagingData.from(menuList ?: emptyList()))
    }

    companion object {
        const val DATA = "DATA"

        @JvmStatic
        fun <T : GridMenuBean> newInstance(
            menuList: List<T>?,
            gridMenuView: GridMenuView? = null
        ) = GridMenuFragment<T>().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(DATA, menuList as ArrayList<out Parcelable>?)
            }
            this.gridMenuView = gridMenuView
        }
    }
}
