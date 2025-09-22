package com.casic.otitan.usercomponent.repository

import androidx.lifecycle.MutableLiveData
import com.casic.otitan.userapi.bean.UserInfo
import com.casic.otitan.usercomponent.api.UserApiService
import io.reactivex.rxjava3.disposables.Disposable
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.bean.ApiRequestOptions
import com.casic.otitan.common.repository.RepositoryImpl

/**
 * Created by fz on 2024/11/4 13:47
 * describe :
 */
class UserRepositoryImpl(apiService: UserApiService?) : RepositoryImpl<UserApiService,BaseView>(apiService) {

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
