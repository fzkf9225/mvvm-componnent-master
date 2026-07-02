package io.coderf.arklab.user.repository

import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.ApiRequestOptions
import io.coderf.arklab.common.repository.RepositoryImpl
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.domain.repository.UserProfileRepository
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Created by fz on 2024/11/4 13:47
 * describe :
 */
class UserRepositoryImpl(apiService: UserApiService?) :
    RepositoryImpl<UserApiService, BaseView>(apiService),
    UserProfileRepository {

    override fun refreshUserInfo(liveData: MutableLiveData<UserInfo>): Disposable {
        return sendRequest(
            apiService.getUserInfo(),
            ApiRequestOptions.Builder()
                .setShowDialog(false)
                .build(),
            liveData
        )
    }
}
