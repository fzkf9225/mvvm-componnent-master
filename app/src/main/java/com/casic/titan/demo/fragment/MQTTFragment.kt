package com.casic.titan.demo.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.casic.titan.demo.R
import com.casic.titan.demo.databinding.MqttFragmentBinding
import com.casic.titan.demo.view.KotlinView
import com.casic.titan.demo.viewmodel.MQTTViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pers.fz.mvvm.base.kotlin.BaseFragment

/**
 * Created by fz on 2023/8/25 16:50
 * describe :
 */
@AndroidEntryPoint
class MQTTFragment :BaseFragment<MQTTViewModel,MqttFragmentBinding,KotlinView>() ,KotlinView{
    override val layoutId: Int
        get() = R.layout.mqtt_fragment

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun initData(bundle: Bundle?) {
        lifecycleScope.launch {
            mViewModel?.mqttStateFlow?.collect { value ->
                binding?.mqttBean = value;
            }
        }
        mViewModel?.requestData()
    }

    override fun callBack() {

    }
}