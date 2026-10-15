package io.coderf.arklab.googlegps.utils

import android.location.Location
import java.util.Locale
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * 地理信息工具类
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/10/16 17:17
 */
object EsriUtil {

    /** 地球平均半径（米） */
    private const val EARTH_RADIUS = 6371000.0

    // WKT 数值：支持整数与小数、正负号
    private const val WKT_NUM = """-?\d+(?:\.\d+)?"""
    private const val WKT_COORD = """$WKT_NUM\s+$WKT_NUM"""
    private val WKT_POINT = """POINT\s*\(\s*$WKT_COORD\s*\)""".toRegex(RegexOption.IGNORE_CASE)
    private val WKT_LINESTRING =
        """LINESTRING\s*\(\s*$WKT_COORD(?:\s*,\s*$WKT_COORD)+\s*\)""".toRegex(RegexOption.IGNORE_CASE)
    // 单环多边形，至少 4 个坐标（首尾闭合）
    private val WKT_POLYGON =
        """POLYGON\s*\(\s*\(\s*$WKT_COORD(?:\s*,\s*$WKT_COORD){3,}\s*\)\s*\)""".toRegex(RegexOption.IGNORE_CASE)

    /**
     * 校验 WKT 格式
     *
     * 简化实现，仅支持 POINT、LINESTRING 和单环 POLYGON，不校验自交等几何合法性
     */
    @JvmStatic
    fun isValidWkt(wkt: String?): Boolean {
        val text = wkt?.trim() ?: return false
        return when {
            WKT_POINT.matches(text) -> true
            WKT_LINESTRING.matches(text) -> true
            WKT_POLYGON.matches(text) -> true
            else -> false
        }
    }

    /**
     * 判断经纬度是否为合法坐标
     *
     * 排除 NaN、越界值以及 GPS 常见的 (0, 0) 脏数据
     */
    @JvmStatic
    fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        if (lat.isNaN() || lon.isNaN()) return false
        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) return false
        return !(lat == 0.0 && lon == 0.0)
    }

    /**
     * 判断 Location 是否为合法坐标
     */
    @JvmStatic
    fun isValidCoordinate(location: Location?): Boolean {
        return location != null && isValidCoordinate(location.latitude, location.longitude)
    }

    @JvmStatic
    fun calculateDistance(locationList: List<Location>?): Double {
        var totalDistance = 0.0

        if (locationList.isNullOrEmpty()) {
            return totalDistance
        }

        for (i in 1 until locationList.size) {
            val previousLocation = locationList[i - 1]
            val currentLocation = locationList[i]

            // 确保两个位置都有有效的经纬度（不能用 hasBearing 判断，静止点通常没有方位）
            if (isValidCoordinate(previousLocation) && isValidCoordinate(currentLocation)) {
                val distance = previousLocation.distanceTo(currentLocation)
                totalDistance += distance
            }
        }

        return totalDistance
    }
    /**
     * 计算两点之间的距离（米）
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 两点之间的距离（米）
     */
    @JvmStatic
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // 使用 Android 自带的 distanceBetween 方法
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }

    /**
     * 计算两点之间的距离（米）- 使用 Location 对象
     * @param location1 第一个位置点
     * @param location2 第二个位置点
     * @return 两点之间的距离（米），如果任一位置无效则返回 0
     */
    @JvmStatic
    fun calculateDistance(location1: Location?, location2: Location?): Double {
        if (location1 == null || location2 == null) {
            return 0.0
        }
        return location1.distanceTo(location2).toDouble()
    }

    /**
     * 计算两点之间的距离（米）- 使用 Haversine 公式（不依赖 Android API）
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 两点之间的距离（米）
     */
    @JvmStatic
    fun calculateDistanceHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = EARTH_RADIUS

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    /**
     * 判断点是否在圆形范围内
     * @param centerLat 圆心纬度
     * @param centerLon 圆心经度
     * @param radius 半径（米）
     * @param targetLat 目标点纬度
     * @param targetLon 目标点经度
     * @return 是否在圆形范围内
     */
    @JvmStatic
    fun isWithinCircle(centerLat: Double, centerLon: Double, radius: Double, targetLat: Double, targetLon: Double): Boolean {
        val distance = calculateDistance(centerLat, centerLon, targetLat, targetLon)
        return distance <= radius
    }

    /**
     * 判断点是否在矩形范围内
     * @param minLat 最小纬度（南）
     * @param maxLat 最大纬度（北）
     * @param minLon 最小经度（西）
     * @param maxLon 最大经度（东）
     * @param targetLat 目标点纬度
     * @param targetLon 目标点经度
     * @return 是否在矩形范围内
     */
    @JvmStatic
    fun isWithinBounds(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, targetLat: Double, targetLon: Double): Boolean {
        return targetLat in minLat..maxLat && targetLon in minLon..maxLon
    }

    /**
     * 判断点是否在多边形范围内（射线法）
     *
     * 以经纬度作平面处理，适用于城市级及以下的电子围栏；多边形无需手动闭合
     * @param targetLat 目标点纬度
     * @param targetLon 目标点经度
     * @param polygon 多边形顶点，按顺序给出，至少 3 个点
     * @return 是否在多边形内（落在边上视为在内）
     */
    @JvmStatic
    fun isPointInPolygon(targetLat: Double, targetLon: Double, polygon: List<Location>?): Boolean {
        if (polygon == null || polygon.size < 3) return false

        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val yi = polygon[i].latitude
            val xi = polygon[i].longitude
            val yj = polygon[j].latitude
            val xj = polygon[j].longitude

            val intersects = (yi > targetLat) != (yj > targetLat) &&
                    targetLon < (xj - xi) * (targetLat - yi) / (yj - yi) + xi
            if (intersects) inside = !inside
            j = i
        }
        return inside
    }

    /**
     * 计算从第一个点到第二个点的初始方位角
     *
     * 用于定位点没有 bearing 时（如 network provider、静止点）由前后两点推算航向
     * @return 方位角（度），范围 [0, 360)，0 为正北、顺时针
     */
    @JvmStatic
    fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLon)

        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /**
     * 计算两点之间的平均速度（米/秒）
     *
     * 依赖 Location.getTime()，时间差为 0 或任一位置为空时返回 0
     */
    @JvmStatic
    fun calculateSpeed(from: Location?, to: Location?): Double {
        if (from == null || to == null) return 0.0
        val seconds = abs(to.time - from.time) / 1000.0
        if (seconds <= 0) return 0.0
        return calculateDistance(from, to) / seconds
    }

    /**
     * 根据起点、方位角和距离推算目标点
     *
     * 是 calculateDistance / calculateBearing 的反算，可用于位置预测、生成缓冲点等
     * @param lat 起点纬度
     * @param lon 起点经度
     * @param bearing 方位角（度），0 为正北、顺时针
     * @param distance 距离（米）
     * @return [纬度, 经度]
     */
    @JvmStatic
    fun destinationPoint(lat: Double, lon: Double, bearing: Double, distance: Double): DoubleArray {
        val delta = distance / EARTH_RADIUS
        val theta = Math.toRadians(bearing)
        val lat1Rad = Math.toRadians(lat)
        val lon1Rad = Math.toRadians(lon)

        val lat2Rad = asin(sin(lat1Rad) * cos(delta) + cos(lat1Rad) * sin(delta) * cos(theta))
        val lon2Rad = lon1Rad + atan2(
            sin(theta) * sin(delta) * cos(lat1Rad),
            cos(delta) - sin(lat1Rad) * sin(lat2Rad)
        )

        // 经度归一化到 [-180, 180)
        val lon2 = (Math.toDegrees(lon2Rad) + 540.0) % 360.0 - 180.0
        return doubleArrayOf(Math.toDegrees(lat2Rad), lon2)
    }

    /**
     * 生成 WKT POINT，注意 WKT 坐标顺序是 经度 纬度
     */
    @JvmStatic
    fun toWktPoint(lat: Double, lon: Double): String {
        return "POINT (${formatWktCoord(lat, lon)})"
    }

    /**
     * 轨迹点转 WKT LINESTRING
     * @return 少于 2 个点时返回 null
     */
    @JvmStatic
    fun toWktLineString(locations: List<Location>?): String? {
        if (locations == null || locations.size < 2) return null
        return locations.joinToString(
            separator = ", ",
            prefix = "LINESTRING (",
            postfix = ")"
        ) { formatWktCoord(it.latitude, it.longitude) }
    }

    /**
     * 顶点列表转 WKT POLYGON，首尾未闭合时自动闭合
     * @return 少于 3 个点时返回 null
     */
    @JvmStatic
    fun toWktPolygon(locations: List<Location>?): String? {
        if (locations == null || locations.size < 3) return null
        val first = locations.first()
        val last = locations.last()
        val closed = first.latitude == last.latitude && first.longitude == last.longitude
        val ring = if (closed) locations else locations + first
        return ring.joinToString(
            separator = ", ",
            prefix = "POLYGON ((",
            postfix = "))"
        ) { formatWktCoord(it.latitude, it.longitude) }
    }

    private fun formatWktCoord(lat: Double, lon: Double): String {
        return String.format(Locale.US, "%.6f %.6f", lon, lat)
    }

    /**
     * 距离格式化为可读文本，1000 米以下显示米，以上显示公里
     */
    @JvmStatic
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            String.format(Locale.getDefault(), "%.0f m", meters)
        } else {
            String.format(Locale.getDefault(), "%.2f km", meters / 1000.0)
        }
    }

    /**
     * 十进制度转 度分秒 文本，例如 39°54'26.52"N
     * @param decimal 十进制度
     * @param isLatitude true 为纬度（N/S），false 为经度（E/W）
     */
    @JvmStatic
    fun toDms(decimal: Double, isLatitude: Boolean): String {
        val absValue = abs(decimal)
        val degrees = absValue.toInt()
        val minutesFull = (absValue - degrees) * 60
        val minutes = minutesFull.toInt()
        val seconds = (minutesFull - minutes) * 60
        val direction = if (isLatitude) {
            if (decimal >= 0) "N" else "S"
        } else {
            if (decimal >= 0) "E" else "W"
        }
        return String.format(Locale.US, "%d°%02d'%05.2f\"%s", degrees, minutes, seconds, direction)
    }

}