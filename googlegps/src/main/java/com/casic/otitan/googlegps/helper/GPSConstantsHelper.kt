package com.casic.otitan.googlegps.helper

import android.Manifest
import android.os.Build

/**
 * Created by fz on 2023/10/18 13:55
 * describe :静态参数
 */
object GPSConstantsHelper {
    const val CHANNEL_ID: String = "GPSService"
    const val CHANNEL_NAME: String = "GPS位置服务"
    const val NOTIFICATION_ID: Int = 300000
    const val GET_NEXT_POINT: String = "getnextpoint"
    const val HDOP: String = "HDOP"
    const val PDOP: String = "PDOP"
    const val VDOP: String = "VDOP"
    const val GEOIDHEIGHT: String = "GEOIDHEIGHT"
    const val AGEOFDGPSDATA: String = "AGEOFDGPSDATA"
    const val DGPSID: String = "DGPSID"
    const val PASSIVE: String = "PASSIVE"
    const val LISTENER: String = "LISTENER"
    const val SATELLITES_FIX: String = "SATELLITES_FIX"
    val PERMISSIONS_LOCATION: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        ) else arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
}
