package com.casic.titan.demo.activity

import android.os.Build
import android.os.Bundle
import com.casic.titan.demo.R
import com.casic.titan.demo.bean.UseCase
import com.casic.titan.demo.databinding.ActivityRoomPagingBinding
import com.casic.titan.demo.viewmodel.DemoRoomPagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.base.BaseActivity

@AndroidEntryPoint
class RoomPagingActivity : BaseActivity<DemoRoomPagingViewModel,ActivityRoomPagingBinding>() {

    private var useCase: UseCase? = null
    override fun getLayoutId() = R.layout.activity_room_paging

    override fun setTitleBar() = ""

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(bundle: Bundle?) {
        useCase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle!!.getParcelable<UseCase>("args", UseCase::class.java)
        } else {
            bundle!!.getParcelable<UseCase>("args")
        }
        toolbarBind.toolbarConfig?.setTitle(useCase?.name)
    }

}