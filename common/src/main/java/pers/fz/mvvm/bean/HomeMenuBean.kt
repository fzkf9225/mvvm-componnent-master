package pers.fz.mvvm.bean

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.BaseObservable

open class HomeMenuBean : BaseObservable, Parcelable {
    var id: Int
    var icon: Int
    var title: String
    var describe: String
    var clx: Class<*>?

    constructor(
        id: Int,
        icon: Int,
        title: String,
        describe: String,
        clx: Class<*>?
    ) {
        this.id = id
        this.icon = icon
        this.title = title
        this.describe = describe
        this.clx = clx
    }

    constructor(
        id: Int,
        icon: Int,
        title: String,
        clx: Class<*>?
    ) : this(id, icon, title, "", clx)

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readSerializable(null, Class::class.java) as Class<*>
        } else {
            parcel.readSerializable() as Class<*>
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(icon)
        parcel.writeString(title)
        parcel.writeString(describe)
        parcel.writeSerializable(clx)
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
        if (clx != other.clx) return false

        return true
    }


    override fun toString(): String {
        return "HomeMenuBean(id=$id, icon=$icon, title='$title', describe='$describe', clx=$clx)"
    }


    companion object CREATOR : Parcelable.Creator<HomeMenuBean> {
        override fun createFromParcel(parcel: Parcel): HomeMenuBean {
            return HomeMenuBean(parcel)
        }

        override fun newArray(size: Int): Array<HomeMenuBean?> {
            return arrayOfNulls(size)
        }
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + icon
        result = 31 * result + title.hashCode()
        result = 31 * result + describe.hashCode()
        result = 31 * result + (clx?.hashCode() ?: 0)
        return result
    }
}