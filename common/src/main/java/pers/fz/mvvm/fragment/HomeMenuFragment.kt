package pers.fz.mvvm.fragment

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.paging.PagingData
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.R
import pers.fz.mvvm.adapter.HomeMenuAdapter
import pers.fz.mvvm.base.BaseFragment
import pers.fz.mvvm.bean.HomeMenuBean
import pers.fz.mvvm.databinding.FragmentHomeMenuBinding
import pers.fz.mvvm.listener.PagingAdapterListener
import pers.fz.mvvm.util.common.DensityUtil
import pers.fz.mvvm.viewmodel.EmptyViewModel
import pers.fz.mvvm.widget.recyclerview.FullyGridLayoutManager
import pers.fz.mvvm.widget.recyclerview.GridSpacingItemDecoration

@AndroidEntryPoint
class HomeMenuFragment : BaseFragment<EmptyViewModel, FragmentHomeMenuBinding>(){
    private var menuList: List<HomeMenuBean>? = null
    private val adapter by lazy {
        HomeMenuAdapter().apply {
            setOnAdapterListener(adapterListener)
        }
    }
    private var column : Int = 4
    private var adapterListener: PagingAdapterListener<HomeMenuBean>? = null

    override fun getLayoutId() = R.layout.fragment_home_menu

    override fun initView(savedInstanceState: Bundle?) {
        binding.mRecyclerviewMenu.layoutManager =
            object : FullyGridLayoutManager(requireContext(), column) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        binding.mRecyclerviewMenu.addItemDecoration(
            GridSpacingItemDecoration(
                DensityUtil.dp2px(
                    requireContext(),
                    21f
                ), 0x00000000
            )
        )
        binding.mRecyclerviewMenu.adapter = adapter
        binding.mRecyclerviewMenu.setPadding(
            DensityUtil.dp2px(requireContext(), 12f),
            0,
            DensityUtil.dp2px(requireContext(), 12f),
            0
        )
    }

    override fun initData(bundle: Bundle?) {
        menuList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelableArrayList<HomeMenuBean>(DATA, HomeMenuBean::class.java)
        } else {
            bundle?.getParcelableArrayList<HomeMenuBean>(DATA)
        }
        adapter.submitData(lifecycle, PagingData.from(menuList!!))
    }

    companion object {
        const val DATA = "DATA"

        @JvmStatic
        fun newInstance(menuList: List<HomeMenuBean>?, column: Int = 4,adapterListener: PagingAdapterListener<HomeMenuBean>?) = HomeMenuFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(DATA, menuList as ArrayList<out Parcelable?>?)
            }
            this.column = column
            this.adapterListener = adapterListener
        }
    }
}