package io.coderf.arklab.user.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.domain.usecase.RefreshUserProfileUseCase
import io.coderf.arklab.user.repository.UserRepositoryImpl
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户资料 ViewModel（新版 Flow）。
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val refreshUserProfile: RefreshUserProfileUseCase
) : BaseViewModel<UserRepositoryImpl, BaseView>(application) {

    @Inject
    lateinit var userApiService: UserApiService

    val userInfoLiveData by lazy {
        MutableLiveData<UserInfo>()
    }

    override fun createRepository(): UserRepositoryImpl {
        return UserRepositoryImpl(userApiService)
    }

    fun refreshUserInfo() {
        viewModelScope.launch {
            refreshUserProfile.execute(iRepository).collect { result ->
                result.onSuccess { userInfoLiveData.value = it }
            }
        }
    }
}
