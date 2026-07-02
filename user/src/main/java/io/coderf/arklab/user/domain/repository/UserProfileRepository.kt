package io.coderf.arklab.user.domain.repository

import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.userapi.bean.UserInfo
import io.reactivex.rxjava3.disposables.Disposable

/**
 * 用户资料相关数据端口；由 [io.coderf.arklab.user.repository.UserRepositoryImpl] 实现，
 * [io.coderf.arklab.user.domain.usecase.RefreshUserProfileUseCase] 只依赖本接口以便替换与测试。
 */
interface UserProfileRepository {

    fun refreshUserInfo(liveData: MutableLiveData<UserInfo>): Disposable
}
