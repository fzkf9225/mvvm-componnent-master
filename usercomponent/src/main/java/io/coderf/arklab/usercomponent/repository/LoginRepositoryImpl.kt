package io.coderf.arklab.usercomponent.repository

import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.usercomponent.api.UserAccountHelper
import io.coderf.arklab.usercomponent.api.UserApiService
import io.coderf.arklab.usercomponent.bean.GraphicVerificationCodeBean
import io.coderf.arklab.usercomponent.bean.RequestLoginBean
import io.coderf.arklab.usercomponent.bean.TokenBean
import io.coderf.arklab.usercomponent.view.UserView
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.coderf.arklab.common.bean.ApiRequestOptions
import io.coderf.arklab.common.repository.RepositoryImpl

/**
 * Created by fz on 2023/12/1 10:47
 * describe :
 */
class LoginRepositoryImpl constructor(apiService: UserApiService?) : io.coderf.arklab.common.repository.RepositoryImpl<UserApiService, UserView>(apiService) {

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
