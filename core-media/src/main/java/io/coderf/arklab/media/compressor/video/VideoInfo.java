package io.coderf.arklab.media.compressor.video;

import androidx.annotation.NonNull;

/**
 * public 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/14 9:15
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

