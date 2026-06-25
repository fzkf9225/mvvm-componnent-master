package io.coderf.arklab.common.widget.video;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import io.coderf.arklab.common.widget.dialog.bean.VideoPlayerDialogIconConfig;

/**
 * 视频播放器通用配置工具。
 */
public final class VideoPlayerViewHelper {

    private VideoPlayerViewHelper() {
    }

    public static void setupPlayer(@NonNull ArkVideoPlayerView player,
                                   @NonNull String videoUrl,
                                   boolean cacheEnable,
                                   @Nullable String title,
                                   @Nullable String thumbUrl,
                                   @NonNull VideoPlayerConfig config) {
        config.setCacheEnable(cacheEnable);
        player.applyConfig(config);
        player.setUp(videoUrl, cacheEnable, TextUtils.isEmpty(title) ? "" : title);
        player.setThumbImageView(createThumbImageView(player.getContext(),
            TextUtils.isEmpty(thumbUrl) ? videoUrl : thumbUrl));
        if (player.getTitleTextView() != null) {
            player.getTitleTextView().setVisibility(
                TextUtils.isEmpty(title) || !config.isShowTitle() ? View.GONE : View.VISIBLE);
        }
        player.setIsTouchWiget(true);
    }

    public static void setupPlayer(@NonNull ArkVideoPlayerView player,
                                   @NonNull String videoUrl,
                                   boolean cacheEnable,
                                   @Nullable String title,
                                   @Nullable VideoPlayerDialogIconConfig iconConfig,
                                   @Nullable String thumbUrl) {
        VideoPlayerConfig config = player.getPlayerConfig();
        if (iconConfig != null) {
            mergeIconConfig(config.getIconConfig(), iconConfig);
        }
        setupPlayer(player, videoUrl, cacheEnable, title, thumbUrl, config);
    }

    @NonNull
    public static ImageView createThumbImageView(@NonNull Context context, @NonNull String imageUrl) {
        AppCompatImageView imageView = new AppCompatImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context).load(imageUrl).into(imageView);
        return imageView;
    }

    public static void startPlay(@NonNull ArkVideoPlayerView player) {
        player.startPlayLogic();
    }

    private static void mergeIconConfig(@NonNull VideoPlayerIconConfig target,
                                        @NonNull VideoPlayerDialogIconConfig legacy) {
        if (legacy.getBackIconRes() != 0) {
            target.setBackIconRes(legacy.getBackIconRes());
        }
        if (legacy.getEnlargeIconRes() != 0) {
            target.setFullscreenIconRes(legacy.getEnlargeIconRes());
        }
        if (legacy.getShrinkIconRes() != 0) {
            target.setFullscreenExitIconRes(legacy.getShrinkIconRes());
        }
        if (legacy.getRotateIconRes() != 0) {
            target.setRotateIconRes(legacy.getRotateIconRes());
        }
        if (legacy.getCloseIconRes() != 0) {
            target.setCloseIconRes(legacy.getCloseIconRes());
        }
    }
}
