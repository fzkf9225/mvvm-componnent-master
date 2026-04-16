package io.coderf.arklab.usercomponent.repository

import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.usercomponent.api.UserApiService
import io.reactivex.rxjava3.disposables.Disposable
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.ApiRequestOptions
import io.coderf.arklab.common.repository.RepositoryImpl

/**
 * Created by fz on 2024/11/4 13:47
 * describe :
 */
class UserRepositoryImpl(apiService: UserApiService?) : io.coderf.arklab.common.repository.RepositoryImpl<UserApiService, BaseView>(apiService) {

    fun refreshUserInfo(liveData: MutableLiveData<UserInfo>): Disposable {
        return sendRequest(
            apiService.getUserInfo(),
            ApiRequestOptions.Builder()
                .setShowDialog(false)
                .build(),
            liveData
        )
    }
}
