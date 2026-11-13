package io.coderf.arklab.user.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.user.domain.usecase.RefreshUserProfileUseCase
import io.coderf.arklab.user.repository.UserRepositoryImpl
import io.coderf.arklab.userapi.bean.UserInfo
import javax.inject.Inject

/**
 * 用户资料 ViewModel（新版 Flow）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/13
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepositoryImpl,
    private val refreshUserProfile: RefreshUserProfileUseCase
) : BaseViewModel(application) {

    val userInfoLiveData by lazy {
        MutableLiveData<UserInfo>()
    }

    fun refreshUserInfo() {
        launchRequest(RequestOptions(showLoading = false)) {
            refreshUserProfile.execute(userRepository).collectSuccess {
                userInfoLiveData.value = it
            }
        }
    }
}
