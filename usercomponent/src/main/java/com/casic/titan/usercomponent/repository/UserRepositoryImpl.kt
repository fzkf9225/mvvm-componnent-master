package com.casic.titan.usercomponent.repository

import androidx.lifecycle.MutableLiveData
import com.casic.titan.userapi.bean.UserInfo
import com.casic.titan.usercomponent.api.UserApiService
import io.reactivex.rxjava3.disposables.Disposable
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.bean.ApiRequestOptions
import pers.fz.mvvm.repository.RepositoryImpl

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
