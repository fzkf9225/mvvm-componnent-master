package com.casic.otitan.usercomponent.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.casic.otitan.userapi.bean.UserInfo
import com.casic.otitan.usercomponent.api.UserApiService
import com.casic.otitan.usercomponent.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import com.casic.otitan.common.api.RepositoryFactory
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.base.BaseViewModel
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