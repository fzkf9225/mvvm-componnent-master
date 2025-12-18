package com.casic.otitan.usercomponent.repository

import androidx.lifecycle.MutableLiveData
import com.casic.otitan.userapi.bean.UserInfo
import com.casic.otitan.usercomponent.api.UserAccountHelper
import com.casic.otitan.usercomponent.api.UserApiService
import com.casic.otitan.usercomponent.bean.GraphicVerificationCodeBean
import com.casic.otitan.usercomponent.bean.RequestLoginBean
import com.casic.otitan.usercomponent.bean.TokenBean
import com.casic.otitan.usercomponent.view.UserView
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import com.casic.otitan.common.bean.ApiRequestOptions
import com.casic.otitan.common.repository.RepositoryImpl

/**
 * Created by fz on 2023/12/1 10:47
 * describe :
 */
class LoginRepositoryImpl constructor(apiService: UserApiService?) : RepositoryImpl<UserApiService, UserView>(apiService) {

    fun getImageCode(
        randomNumber: String,
        liveData: MutableLiveData<GraphicVerificationCodeBean>
    ): Disposable {
        return sendRequest(
            apiService.getImageCode(randomNumber),
            ApiRequestOptions.Builder()
                .setShowDialog(false)
                .build(),
            liveData
        )
    }

    fun login(requestLoginBean: RequestLoginBean, liveData: MutableLiveData<UserInfo>) {
        sendRequest(
            apiService.getToken(requestLoginBean)
                .flatMap { tokenBean: TokenBean ->
                    UserAccountHelper.setToken(tokenBean.tokenId)
                    apiService.getUserInfo()
                },
            ApiRequestOptions.getDefault().apply { dialogMessage = "登录中，请稍后..." },
            liveData
        )
    }


    fun logout(liveData: MutableLiveData<Any>) {
        sendRequest(
            apiService.logout(),
            ApiRequestOptions.getDefault(),
            liveData
        )
    }

    fun logout(consumer: Consumer<Any>, consumerError: Consumer<Throwable>) {
        sendRequest(
            apiService.logout(),
            ApiRequestOptions.getDefault().apply { isShowDialog = false },
            consumer,
            consumerError
        )
    }
}
