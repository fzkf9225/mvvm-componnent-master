package io.coderf.arklab.user.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.common.base.launchUiRequest
import io.coderf.arklab.user.api.UserApiService
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
 * @updated 2026/9/12
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val refreshUserProfile: RefreshUserProfileUseCase
) : BaseViewModel<UserRepositoryImpl>(application) {

    @Inject
    lateinit var userApiService: UserApiService

    val userInfoLiveData by lazy {
        MutableLiveData<UserInfo>()
    }

    override fun createRepository(): UserRepositoryImpl {
        return UserRepositoryImpl(userApiService)
    }

    fun refreshUserInfo() {
        // 当前 Repository 无遮罩 Loading；统一用 launchUiRequest，便于日后打开可取消策略
        launchUiRequest {
            refreshUserProfile.execute(iRepository).collect { result ->
                result.onSuccess { userInfoLiveData.value = it }
            }
        }
    }
}
