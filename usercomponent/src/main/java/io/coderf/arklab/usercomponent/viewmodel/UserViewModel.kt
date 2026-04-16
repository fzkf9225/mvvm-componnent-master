package io.coderf.arklab.usercomponent.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.usercomponent.api.UserApiService
import io.coderf.arklab.usercomponent.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.api.RepositoryFactory
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.base.BaseViewModel
import javax.inject.Inject

/**
 * created by fz on 2024/11/4 13:57
 * describe:
 */
@HiltViewModel
class UserViewModel @Inject constructor(application: Application) : io.coderf.arklab.common.base.BaseViewModel<UserRepositoryImpl, BaseView>(application) {

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