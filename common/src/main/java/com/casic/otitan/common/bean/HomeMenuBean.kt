package com.casic.otitan.common.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable

open class HomeMenuBean : BaseObservable, Parcelable {
    var id: Int

    /**
     * 图标资源
     */
    @DrawableRes
    var icon: Int

    /**
     * 标题
     */
    var title: String

    /**
     * 描述
     */
    var describe: String?

    /**
     * 目标路径，通过intent.setComponent(new ComponentName(requireContext(), className));使用
     */
    var componentName: String?

    /**
     * 图标宽度
     */
    var iconWidth: Float?

    /**
     * 图标高度
     */
    var iconHeight: Float?

    /**
     * 文字与图标的距离
     */
    var iconTextMargin: Float?

    /**
     * 文字颜色
     */
    @ColorInt
    var labelColor: Int?

    /**
     * 文字大小
     */
    var labelSize: Float?

    /**
     * 是否置灰
     */
    var isGray: Boolean?= false

    constructor(
        id: Int,
        @DrawableRes icon: Int,
        title: String,
        describe: String,
        componentName: String?,
        iconWidth: Float? = null,
        iconHeight: Float? = null,
        iconTextMargin: Float? = null,
        @ColorInt labelColor: Int? = null,
        labelSize: Float? = null
    ) {
        this.id = id
        this.icon = icon
        this.title = title
        this.describe = describe
        this.componentName = componentName
        this.iconWidth = iconWidth
        this.iconHeight = iconHeight
        this.iconTextMargin = iconTextMargin
        this.labelColor = labelColor
        this.labelSize = labelSize
    }

    constructor(
        id: Int,
        @DrawableRes icon: Int,
        title: String,
        describe: String,
        componentName: String?
    ) : this(id, icon, title, describe, componentName,null,null,null,null,null)

    constructor(
        id: Int,
        @DrawableRes icon: Int,
        title: String,
        componentName: String?
    ) : this(id, icon, title, "", componentName,null,null,null,null,null)


    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        icon = parcel.readInt(),
        title = parcel.readString() ?: "",
        describe = parcel.readString() ?: "",
        componentName = parcel.readString(),
        iconWidth = parcel.readValue(Float::class.java.classLoader) as? Float,
        iconHeight = parcel.readValue(Float::class.java.classLoader) as? Float,
        iconTextMargin = parcel.readValue(Float::class.java.classLoader) as? Float,
        labelColor = parcel.readValue(Int::class.java.classLoader) as? Int,
        labelSize = parcel.readValue(Float::class.java.classLoader) as? Float
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(icon)
        parcel.writeString(title)
        parcel.writeString(describe)
        parcel.writeString(componentName)
        parcel.writeValue(iconWidth)
        parcel.writeValue(iconHeight)
        parcel.writeValue(iconTextMargin)
        parcel.writeValue(labelColor)
        parcel.writeValue(labelSize)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HomeMenuBean

        if (id != other.id) return false
        if (icon != other.icon) return false
        if (title != other.title) return false
        if (describe != other.describe) return false
        if (componentName != other.componentName) return false
        if (iconWidth != other.iconWidth) return false
        if (iconHeight != other.iconHeight) return false
        if (iconTextMargin != other.iconTextMargin) return false
        if (labelColor != other.labelColor) return false
        if (labelSize != other.labelSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + icon
        result = 31 * result + title.hashCode()
        result = 31 * result + (describe?.hashCode() ?: 0)
        result = 31 * result + (componentName?.hashCode() ?: 0)
        result = 31 * result + (iconWidth?.hashCode() ?: 0)
        result = 31 * result + (iconHeight?.hashCode() ?: 0)
        result = 31 * result + (iconTextMargin?.hashCode() ?: 0)
        result = 31 * result + (labelColor ?: 0)
        result = 31 * result + (labelSize?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "HomeMenuBean(id=$id, icon=$icon, title='$title', describe='$describe', " +
                "componentName=$componentName, iconWidth=$iconWidth, iconHeight=$iconHeight, " +
                "iconTextMargin=$iconTextMargin, labelColor=$labelColor, labelSize=$labelSize)"
    }

    companion object CREATOR : Parcelable.Creator<HomeMenuBean> {
        override fun createFromParcel(parcel: Parcel): HomeMenuBean {
            return HomeMenuBean(parcel)
        }

        override fun newArray(size: Int): Array<HomeMenuBean?> {
            return arrayOfNulls(size)
        }
    }
}