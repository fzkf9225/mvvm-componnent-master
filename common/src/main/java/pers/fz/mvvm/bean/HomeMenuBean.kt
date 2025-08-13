package pers.fz.mvvm.bean

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.BaseObservable

data class HomeMenuBean(
    var id: Int,
    var icon: Int,
    var title: String,
    var describe: String,
    var clx: Class<*>?
) : BaseObservable(), Parcelable {
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

    companion object CREATOR : Parcelable.Creator<HomeMenuBean> {
        override fun createFromParcel(parcel: Parcel): HomeMenuBean {
            return HomeMenuBean(parcel)
        }

        override fun newArray(size: Int): Array<HomeMenuBean?> {
            return arrayOfNulls(size)
        }
    }
}