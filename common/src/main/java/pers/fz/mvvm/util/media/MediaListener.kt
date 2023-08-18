package pers.fz.mvvm.util.media

/**
 * Created by fz on 2023/8/18 14:35
 * describe :
 */
interface MediaListener {
    fun onSelectedImageCount(): Int = 0
    fun onSelectedVideoCount(): Int = 0

}