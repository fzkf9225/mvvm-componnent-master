package com.casic.titan.usercomponent.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.casic.titan.userapi.bean.UserInfo
import com.casic.titan.usercomponent.api.UserApiService
import com.casic.titan.usercomponent.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import pers.fz.mvvm.api.RepositoryFactory
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.base.BaseViewModel
import javax.inject.Inject

/**
 * created by fz on 2024/11/4 13:57
 * describe:
 */
@HiltViewModel
class UserViewModel @Inject constructor(application: Application) : BaseViewModel<UserRepositoryImpl, BaseView>(application) {

    @Inject
    lateinit var userApiService: UserApiService

    val userInfoLiveData by lazy {
        MutableLiveData<UserInfo>()
    }


    override fun createRepository(): UserRepositoryImpl {
        return RepositoryFactory.create(UserRepositoryImpl::class.java,userApiService)
    }

    public fun refreshUserInfo() {
        iRepository.refreshUserInfo(userInfoLiveData)
    }

}