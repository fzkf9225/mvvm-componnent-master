package com.casic.titan.demo.viewmodel

import android.app.Application
import com.casic.titan.demo.api.ApiServiceHelper
import com.casic.titan.demo.view.KotlinView
import com.casic.titan.mqttcomponent.MqttBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.base.kotlin.BaseViewModel
import pers.fz.mvvm.util.log.LogUtil
import javax.inject.Inject

/**
 * Created by fz on 2023/8/25 13:57
 * describe :
 */
@HiltViewModel
class KotlinViewModel @Inject constructor(application: Application) :
    BaseViewModel<BaseView>(application) {
}