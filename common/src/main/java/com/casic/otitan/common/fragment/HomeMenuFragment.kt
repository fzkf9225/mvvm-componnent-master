package com.casic.otitan.common.fragment

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingData
import com.casic.otitan.common.R
import com.casic.otitan.common.adapter.HomeMenuAdapter
import com.casic.otitan.common.base.BaseFragment
import com.casic.otitan.common.base.BasePagingAdapter
import com.casic.otitan.common.bean.HomeMenuBean
import com.casic.otitan.common.databinding.FragmentHomeMenuBinding
import com.casic.otitan.common.listener.PagingAdapterListener
import com.casic.otitan.common.utils.common.DensityUtil
import com.casic.otitan.common.viewmodel.EmptyViewModel
import com.casic.otitan.common.widget.customview.HomeMenuView
import com.casic.otitan.common.widget.recyclerview.GridSpacingItemDecoration

class HomeMenuFragment<T : HomeMenuBean> : BaseFragment<EmptyViewModel, FragmentHomeMenuBinding>() {
    private var menuList: List<HomeMenuBean>? = null
    private var adapter: BasePagingAdapter<HomeMenuBean, ViewDataBinding>? = null
    private val defaultAdapter by lazy {
        HomeMenuAdapter(homeMenuView).apply {
            setOnAdapterListener(homeMenuView?.getAdapterListener() as PagingAdapterListener<HomeMenuBean?>?)
        }
    }
    private var homeMenuView: HomeMenuView? = null
    override fun getLayoutId() = R.layout.fragment_home_menu

    override fun initView(savedInstanceState: Bundle?) {
        binding.mRecyclerviewMenu.layoutManager =
            object : com.casic.otitan.common.widget.recyclerview.FullyGridLayoutManager(requireContext(), homeMenuView?.getColumnCount()?:4) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        binding.mRecyclerviewMenu.addItemDecoration(
            GridSpacingItemDecoration(
                homeMenuView?.startMargin?:DensityUtil.dp2px(
                    requireContext(),
                    8f
                ), 0x00000000
            )
        )
        adapter =
            (homeMenuView?.customHomeMenuAdapterCallback?.getAdapter<HomeMenuBean, ViewDataBinding>()
                ?: defaultAdapter) as BasePagingAdapter<HomeMenuBean, ViewDataBinding>?
        binding.mRecyclerviewMenu.adapter = adapter

        binding.mRecyclerviewMenu.setPadding(
            homeMenuView?.startMargin?:DensityUtil.dp2px(requireContext(), 12f),
            0,
            homeMenuView?.endMargin?:DensityUtil.dp2px(requireContext(), 12f),
            0
        )
    }

    override fun initData(bundle: Bundle?) {
        menuList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelableArrayList<HomeMenuBean>(DATA, HomeMenuBean::class.java)
        } else {
            bundle?.getParcelableArrayList<HomeMenuBean>(DATA)
        }
        adapter?.submitData(lifecycle, PagingData.from(menuList ?: emptyList()))
    }

    companion object {
        const val DATA = "DATA"

        @JvmStatic
        fun <T : HomeMenuBean> newInstance(
            menuList:List<T>?,
            homeMenuView: HomeMenuView? = null
        ) = HomeMenuFragment<T>().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(DATA, menuList as ArrayList<out Parcelable>?)
            }
            this.homeMenuView = homeMenuView;
        }
    }
}