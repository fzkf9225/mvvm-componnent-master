package com.casic.otitan.media.compressor.video;

import androidx.annotation.NonNull;

/**
 * created by fz on 2024/11/14 9:15
 * describe:
 */
public record VideoInfo(int width, int height, int rotation, long duration) {

    @NonNull
    @Override
    public String toString() {
        return "VideoInfo{" +
                "width=" + width +
                ", height=" + height +
                ", rotation=" + rotation +
                ", duration=" + duration +
                '}';
    }
}

