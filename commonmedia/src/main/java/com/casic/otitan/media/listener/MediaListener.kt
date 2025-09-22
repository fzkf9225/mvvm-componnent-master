package com.casic.otitan.media.listener

/**
 * Created by fz on 2023/8/18 14:35
 * describe :
 */
interface MediaListener {
    fun onSelectedImageCount(): Int = 0
    fun onSelectedVideoCount(): Int = 0
    fun onSelectedAudioCount(): Int = 0
    fun onSelectedFileCount(): Int = 0
    fun onSelectedMediaCount(): Int = 0
}