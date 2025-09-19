package pers.fz.mvvm.fragment

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingData
import pers.fz.mvvm.R
import pers.fz.mvvm.adapter.HomeMenuAdapter
import pers.fz.mvvm.base.BaseFragment
import pers.fz.mvvm.base.BasePagingAdapter
import pers.fz.mvvm.bean.HomeMenuBean
import pers.fz.mvvm.databinding.FragmentHomeMenuBinding
import pers.fz.mvvm.listener.PagingAdapterListener
import pers.fz.mvvm.utils.common.DensityUtil
import pers.fz.mvvm.viewmodel.EmptyViewModel
import pers.fz.mvvm.widget.customview.HomeMenuView
import pers.fz.mvvm.widget.recyclerview.FullyGridLayoutManager
import pers.fz.mvvm.widget.recyclerview.GridSpacingItemDecoration

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
            object : FullyGridLayoutManager(requireContext(), homeMenuView?.getColumnCount()?:4) {
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