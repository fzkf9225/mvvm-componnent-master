package io.coderf.arklab.common.widget.video;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import io.coderf.arklab.common.widget.dialog.bean.VideoPlayerDialogIconConfig;

/**
 * 视频播放器通用初始化工具。
 * <p>
 * 负责 setUp、封面图、标题显隐等一次性配置；播放控制与生命周期请用 {@link VideoPlayerController}。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public final class VideoPlayerViewHelper {

    private VideoPlayerViewHelper() {
    }

    /**
     * 完整初始化：应用 config、setUp、封面与标题。
     *
     * @param player      播放器 View
     * @param videoUrl    播放地址（http/https、file 路径或 content Uri 字符串）
     * @param cacheEnable 是否边播边缓存；本地地址建议 false
     * @param title       顶部标题，可为空
     * @param thumbUrl    封面图地址，为空时复用 videoUrl
     * @param config      播放器配置
     */
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

    /**
     * 兼容 {@link VideoPlayerDialogIconConfig} 的初始化重载，将旧图标配置合并进当前 player 的 config。
     *
     * @param player      播放器 View
     * @param videoUrl    播放地址
     * @param cacheEnable 是否缓存
     * @param title       标题
     * @param iconConfig  旧版 Dialog 图标配置，可为 null
     * @param thumbUrl    封面地址
     */
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

    /**
     * 创建封面 ImageView，使用 Glide 加载。
     *
     * @param context  上下文
     * @param imageUrl 封面图 URL 或本地路径
     * @return 已发起加载的 ImageView（需由调用方交给播放器 setThumbImageView）
     */
    @NonNull
    public static ImageView createThumbImageView(@NonNull Context context, @NonNull String imageUrl) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context).load(imageUrl).into(imageView);
        return imageView;
    }

    /**
     * 开始播放（调用 GSY {@code startPlayLogic}）。
     *
     * @param player 已完成 {@link #setupPlayer} 的播放器
     */
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
