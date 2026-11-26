package io.coderf.arklab.common.widget.gallery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.LivePhotoInfo;
import io.coderf.arklab.common.widget.customview.LivePhotoView;
import io.coderf.arklab.common.widget.dialog.ImageSaveDialog;
import io.coderf.arklab.common.widget.dialog.ImageSaveDialogConfig;
import io.coderf.arklab.common.widget.gallery.attacher.PhotoViewAttacher;
import io.coderf.arklab.common.widget.gallery.config.PreviewGalleryZoomConfig;
import io.coderf.arklab.common.widget.gallery.impl.PhotoView;
import io.coderf.arklab.common.widget.video.ArkVideoPlayerView;
import io.coderf.arklab.common.widget.video.VideoPlayerConfig;
import io.coderf.arklab.common.widget.video.VideoPlayerViewHelper;
import io.coderf.arklab.core.utils.FileUtil;

/**
 * 大图预览中图片 / 实况 / 视频的绑定与释放公共逻辑。
 * <p>
 * {@link io.coderf.arklab.common.widget.gallery.adapter.PreviewViewPagerAdapter} 与
 * {@link io.coderf.arklab.common.widget.gallery.adapter.PreviewInfoViewPagerAdapter} 共用，避免双份拷贝。
 * 预览策略：实况关按住播、仅胶囊点击；视频用 {@link VideoPlayerConfig#previewDefaults()} 页内播放。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25
 */
public final class PreviewMediaBinder {

    /** ViewPager item：图片 / 实况 */
    public static final int VIEW_TYPE_IMAGE = 10;
    /** ViewPager item：视频 */
    public static final int VIEW_TYPE_VIDEO = 11;

    private PreviewMediaBinder() {
    }

    /**
     * 根据附件类型判断是否视频（与历史 Adapter 判定一致）。
     */
    public static boolean isVideo(@NonNull Context context,
                                  @Nullable String fileType,
                                  @Nullable String path) {
        return AttachmentTypeEnum.VIDEO == AttachmentUtil.getMediaType(context, fileType, path);
    }

    /**
     * 大图预览实况 / 静图绑定：关闭整页按住播放，保留左下角胶囊点击；长按保存始终可挂。
     *
     * @param livePhoto     实况控件
     * @param path          静图地址
     * @param liveInfo      实况扩展，可为 null
     * @param placeholder   占位图
     * @param error         错误图
     * @param zoomConfig    缩放配置
     * @param canSave       是否允许长按保存
     * @param saveConfig    保存弹窗配置
     * @param onSaveConfirm 用户确认保存后的回调（已 dismiss 保存弹窗）
     * @param onPhotoTap    单击图片关闭预览
     */
    public static void bindImageOrLive(@NonNull LivePhotoView livePhoto,
                                       @Nullable String path,
                                       @Nullable LivePhotoInfo liveInfo,
                                       @Nullable Drawable placeholder,
                                       @Nullable Drawable error,
                                       @Nullable PreviewGalleryZoomConfig zoomConfig,
                                       boolean canSave,
                                       @Nullable ImageSaveDialogConfig saveConfig,
                                       @Nullable Runnable onSaveConfirm,
                                       @Nullable PhotoViewAttacher.OnPhotoTapListener onPhotoTap) {
        livePhoto.setAllowParentInterceptOnEdge(true);
        livePhoto.applyZoomConfig(zoomConfig);
        // 大图预览：关按住播，仅胶囊点击；控件内部按住逻辑保留给列表等场景
        livePhoto.setHoldToPlayEnabled(false);
        livePhoto.setOnMotionAvailabilityListener(null);
        livePhoto.bind(path, liveInfo, placeholder, error, true);

        PhotoView photoView = livePhoto.getPhotoView();
        if (onPhotoTap != null) {
            photoView.setOnPhotoTapListener(onPhotoTap);
        }
        photoView.setOnLongClickListener(v -> {
            if (!canSave || onSaveConfirm == null) {
                return false;
            }
            new ImageSaveDialog(v.getContext())
                    .setConfig(saveConfig)
                    .setOnImageSaveListener(dialog -> {
                        dialog.dismiss();
                        onSaveConfirm.run();
                    })
                    .build()
                    .show();
            return true;
        });
    }

    /**
     * 绑定预览内嵌视频：GSY 自带封面，单击画面暂停/播放，无旋转全屏。
     *
     * @param player   播放器
     * @param path     视频地址
     * @param title    标题（预览默认不展示，仅 setUp 需要）
     * @param autoPlay 是否立即起播（仅当前页建议 true）
     */
    public static void bindVideoPlayer(@NonNull ArkVideoPlayerView player,
                                       @Nullable String path,
                                       @Nullable String title,
                                       boolean autoPlay) {
        if (TextUtils.isEmpty(path)) {
            releaseVideoPlayer(player);
            return;
        }
        boolean local = isLocalPath(path);
        VideoPlayerConfig config = VideoPlayerConfig.previewDefaults()
                .setCacheEnable(!local);
        String safeTitle = TextUtils.isEmpty(title) ? FileUtil.getFileName(path) : title;
        // 封面用同一地址，GSY + Glide 会按视频/图加载；网络视频也可另传缩略图
        VideoPlayerViewHelper.setupPlayer(player, path, !local, safeTitle, path, config);
        if (autoPlay) {
            VideoPlayerViewHelper.startPlay(player);
        }
    }

    /**
     * 暂停播放（翻页离开当前视频页时调用）。
     */
    public static void pauseVideoPlayer(@Nullable ArkVideoPlayerView player) {
        if (player == null) {
            return;
        }
        try {
            player.onVideoPause();
        } catch (Exception ignore) {
        }
    }

    /**
     * 释放单个播放器资源（recycle / dismiss）。
     */
    public static void releaseVideoPlayer(@Nullable ArkVideoPlayerView player) {
        if (player == null) {
            return;
        }
        try {
            player.onVideoPause();
        } catch (Exception ignore) {
        }
        try {
            player.release();
        } catch (Exception ignore) {
        }
    }

    /**
     * Dialog 关闭时释放全部 GSY 视频（多页预览兜底）。
     */
    public static void releaseAllGsyVideos() {
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception ignore) {
        }
    }

    /**
     * 绑定视频页左上角关闭按钮（仅视频 layout 有）。
     */
    public static void bindVideoCloseButton(@Nullable View closeButton, @Nullable Runnable onClose) {
        if (closeButton == null) {
            return;
        }
        if (onClose == null) {
            closeButton.setOnClickListener(null);
            return;
        }
        closeButton.setOnClickListener(v -> onClose.run());
    }

    private static boolean isLocalPath(@NonNull String path) {
        return path.startsWith("/")
                || path.startsWith("file://")
                || path.startsWith("content://")
                || path.startsWith("android.resource://");
    }
}
