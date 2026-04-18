package io.coderf.arklab.googlegps.utils

import android.location.Location


/**
 * created by fz on 2024/10/16 17:17
 * describe：arcgis工具类
 */
object EsriUtil {


    // 校验wkt格式的方法
    @JvmStatic
    fun isValidWkt(wkt: String): Boolean {
        // 简化示例，仅支持 POINT、LINESTRING 和 POLYGON
        val pointPattern = """POINT\s*\(\s*(-?\d+\.\d+)\s+(-?\d+\.\d+)\s*\)""".toRegex()
        val lineStringPattern =
            """LINESTRING\s*\(\s*((-?\d+\.\d+)\s+(-?\d+\.\d+)\s*,?\s*)+\)""".toRegex()
        val polygonPattern =
            """POLYGON\s*\(\s*\(((-\d+\.\d+)\s+(-?\d+\.\d+)\s*,?\s*)+\)\)""".toRegex()

        return when {
            pointPattern.matches(wkt) -> true
            lineStringPattern.matches(wkt) -> true
            polygonPattern.matches(wkt) -> {
                // 对多边形进行更复杂的校验，例如检查自交、重叠等
                // ...
                true
            }

            else -> false
        }
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

            // 确保两个位置都有有效的经纬度
            if (previousLocation.hasBearing() && currentLocation.hasBearing()) {
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
        val R = 6371000.0 // 地球平均半径（米）

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

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
}