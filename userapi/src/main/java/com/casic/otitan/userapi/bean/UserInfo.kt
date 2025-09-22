package com.casic.otitan.userapi.bean

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

data class UserInfo(
    /**
     * 主键
     */
    var id: String,
    /**
     * 钉钉账户ID
     */
    var accountId: String? = null,
    /**
     * 个人评价
     */
    var assessment: String? = null,
    /**
     * 出生日期
     */
    var birthday: String? = null,
    /**
     * 邮箱地址
     */
    var email: String? = null,
    /**
     * 身份 1：管理员 2：领导 3：护林员 4：其他 5：林长
     */
    var identity: Int? = null,
    /**
     * 办公电话
     */
    var officePhone: String? = null,
    /**
     * 工作组织id
     */
    var workOrgId: String? = null,
    /**
     * 工作组织名称
     */
    var workOrgName: String? = null,
    /**
     * 父工作组织id
     */
    var parentWorkOrgId: String? = null,
    /**
     * 是否在线 0 ：否 1：是
     */
    var onLine: Int? = null,
    /**
     * 工作机构
     */
    var organizationIds: List<String>? = null,
    /**
     * 密码
     */
    var password: String? = null,
    /**
     * 手机号
     */
    var phone: String? = null,
    /**
     * 政治面貌
     */
    var politicalLandscape: String? = null,
    /**
     * 政治面貌
     */
    var politicalLandscapeName: String? = null,
    /**
     * 行政职务
     */
    var position: String? = null,
    /**
     * 真实姓名
     */
    var realName: String? = null,
    /**
     * 备注
     */
    var remarks: String? = null,
    /**
     * 性别
     */
    var sex: String? = null,
    /**
     * 账号状态（0：禁用；1：启用）
     */
    var status: String? = null,
    /**
     * 声网code
     */
    var swCode: Int? = null,
    /**
     * 头像
     */
    var userIcon: String? = null,
    /**
     * 用户名称
     */
    var userName: String? = null,
    /**
     * 声网渠道号
     */
    var channel: String? = "titan_wnslfh"
) : Parcelable {

    constructor() : this(
        UUID.randomUUID().toString(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),  // politicalLandscapeName
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "titan_wnslfh"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(accountId)
        parcel.writeString(assessment)
        parcel.writeString(birthday)
        parcel.writeString(email)
        parcel.writeValue(identity)
        parcel.writeString(officePhone)
        parcel.writeString(workOrgId)
        parcel.writeString(workOrgName)
        parcel.writeString(parentWorkOrgId)
        parcel.writeValue(onLine)
        parcel.writeStringList(organizationIds)
        parcel.writeString(password)
        parcel.writeString(phone)
        parcel.writeString(politicalLandscape)
        parcel.writeString(politicalLandscapeName)  // 添加这行
        parcel.writeString(position)
        parcel.writeString(realName)
        parcel.writeString(remarks)
        parcel.writeString(sex)
        parcel.writeString(status)
        parcel.writeValue(swCode)
        parcel.writeString(userIcon)
        parcel.writeString(userName)
        parcel.writeString(channel)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInfo> {
        override fun createFromParcel(parcel: Parcel): UserInfo {
            return UserInfo(parcel)
        }

        override fun newArray(size: Int): Array<UserInfo?> {
            return arrayOfNulls(size)
        }
    }

    public fun isOnline(): Boolean {
        return 1 == onLine
    }
}

