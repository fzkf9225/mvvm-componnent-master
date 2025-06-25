package com.casic.titan.usercomponent.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

/**
 * Create by CherishTang on 2024/11/8 0018
 * describe:
 */
data class RequestLoginBean(
    @Bindable
    var accountId: String? = null,
    @Bindable
    var code: String? = null,
    @Bindable
    var num: String? = null,
    @Bindable
    var openId: String? = null,
    @Bindable
    var password: String? = null,
    @Bindable
    var userName: String? = null
) : BaseObservable() {
    constructor(
        code: String,
        num: String,
        password: String? = null,
        userName: String
    ) : this(null, code, num, null, password, userName)

    constructor(userName: String? = null) : this(null, null, null, null, null, userName)
    constructor() : this(null, null, null, null, null, null)

}
