package io.coderf.arklab.common.utils.common;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.widget.gallery.bean.PreviewInfoBean;

/**
 * 实况图（Live / Motion Photo）扩展信息。
 * <p>
 * 持久化约定写入 {@link AttachmentBean#getLiveInfo()}（专用字段，不占用 {@code otherInfo}）。
 * 无附件的预览条目可临时使用 {@link PreviewInfoBean#getMetaData()}。
 * <pre>
 * {
 *   "livePhoto": true,
 *   "motionVideoPath": "content://... 或绝对路径 / http(s)",
 *   "motionSource": "google_motion|apple_live|sidecar|custom"
 * }
 * </pre>
 * 不改变 {@code fileType}，实况图仍按 image 处理，避免破坏现有类型分支。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/9
 * @updated 2026/11/9 改为 AttachmentBean.liveInfo，与 otherInfo 分离
 */
public final class LivePhotoInfo {

    public static final String KEY_LIVE_PHOTO = "livePhoto";
    public static final String KEY_MOTION_VIDEO_PATH = "motionVideoPath";
    public static final String KEY_MOTION_SOURCE = "motionSource";

    public static final LivePhotoInfo EMPTY = new LivePhotoInfo(false, null, null);

    private final boolean livePhoto;
    @Nullable
    private final String motionVideoPath;
    @Nullable
    private final String motionSource;

    private LivePhotoInfo(boolean livePhoto, @Nullable String motionVideoPath, @Nullable String motionSource) {
        this.livePhoto = livePhoto;
        this.motionVideoPath = TextUtils.isEmpty(motionVideoPath) ? null : motionVideoPath;
        this.motionSource = TextUtils.isEmpty(motionSource) ? null : motionSource;
    }

    /**
     * 是否具备可播放的动感片段（真正能进 L2 预览的条件）。
     */
    public boolean hasMotionVideo() {
        return !TextUtils.isEmpty(motionVideoPath);
    }

    /**
     * JSON 中显式标记为实况，或已带动感路径。
     */
    public boolean isLivePhoto() {
        return livePhoto || hasMotionVideo();
    }

    @Nullable
    public String getMotionVideoPath() {
        return motionVideoPath;
    }

    @Nullable
    public String getMotionSource() {
        return motionSource;
    }

    @NonNull
    public static LivePhotoInfo of(boolean livePhoto, @Nullable String motionVideoPath, @Nullable String motionSource) {
        if (!livePhoto && TextUtils.isEmpty(motionVideoPath)) {
            return EMPTY;
        }
        return new LivePhotoInfo(livePhoto || !TextUtils.isEmpty(motionVideoPath), motionVideoPath, motionSource);
    }

    /**
     * 从附件专用字段 {@link AttachmentBean#getLiveInfo()} 解析。
     */
    @NonNull
    public static LivePhotoInfo from(@Nullable AttachmentBean bean) {
        if (bean == null) {
            return EMPTY;
        }
        return parse(bean.getLiveInfo());
    }

    /**
     * 优先附件 {@code liveInfo}；无附件或无实况时再读 {@link PreviewInfoBean#getMetaData()}。
     */
    @NonNull
    public static LivePhotoInfo from(@Nullable PreviewInfoBean bean) {
        if (bean == null) {
            return EMPTY;
        }
        LivePhotoInfo fromAttachment = from(bean.getAttachment());
        if (fromAttachment.hasMotionVideo() || fromAttachment.isLivePhoto()) {
            return fromAttachment;
        }
        return parse(bean.getMetaData());
    }

    @NonNull
    public static LivePhotoInfo parse(@Nullable String json) {
        if (TextUtils.isEmpty(json)) {
            return EMPTY;
        }
        try {
            JSONObject object = new JSONObject(json);
            boolean live = object.optBoolean(KEY_LIVE_PHOTO, false);
            String motionPath = object.optString(KEY_MOTION_VIDEO_PATH, null);
            if ("null".equalsIgnoreCase(motionPath)) {
                motionPath = null;
            }
            String source = object.optString(KEY_MOTION_SOURCE, null);
            if ("null".equalsIgnoreCase(source)) {
                source = null;
            }
            if (!live && TextUtils.isEmpty(motionPath)) {
                return EMPTY;
            }
            return new LivePhotoInfo(live || !TextUtils.isEmpty(motionPath), motionPath, source);
        } catch (Exception ignore) {
            return EMPTY;
        }
    }

    /**
     * 生成写入 {@link AttachmentBean#getLiveInfo()} 的 JSON；无实况信息时返回空串。
     */
    @NonNull
    public static String toJson(boolean livePhoto, @Nullable String motionVideoPath, @Nullable String motionSource) {
        if (!livePhoto && TextUtils.isEmpty(motionVideoPath)) {
            return "";
        }
        try {
            JSONObject object = new JSONObject();
            object.put(KEY_LIVE_PHOTO, livePhoto || !TextUtils.isEmpty(motionVideoPath));
            if (!TextUtils.isEmpty(motionVideoPath)) {
                object.put(KEY_MOTION_VIDEO_PATH, motionVideoPath);
            }
            if (!TextUtils.isEmpty(motionSource)) {
                object.put(KEY_MOTION_SOURCE, motionSource);
            }
            return object.toString();
        } catch (Exception ignore) {
            return "";
        }
    }

    /**
     * @deprecated 使用 {@link #toJson(boolean, String, String)}，避免与业务 {@code otherInfo} 混淆。
     */
    @Deprecated
    @NonNull
    public static String toOtherInfoJson(boolean livePhoto, @Nullable String motionVideoPath, @Nullable String motionSource) {
        return toJson(livePhoto, motionVideoPath, motionSource);
    }

    /**
     * 将本实例写入附件 {@link AttachmentBean#setLiveInfo(String)}。
     */
    public void applyTo(@NonNull AttachmentBean bean) {
        bean.setLiveInfo(toJson(livePhoto, motionVideoPath, motionSource));
    }

    /**
     * 列表角标显隐：{@code liveInfo} 有动感路径，或同目录存在 sidecar 视频时显示。
     * <p>内嵌 Motion Photo 需先 {@link MotionPhotoResolver#enrichAttachment} 才会写入 {@code liveInfo}。</p>
     */
    public static void applyBadge(@Nullable View badgeView, @Nullable AttachmentBean bean) {
        if (badgeView == null) {
            return;
        }
        boolean show = from(bean).hasMotionVideo()
                || (bean != null && MotionPhotoResolver.hasSidecarMotion(bean.getPath()));
        ensureCapsuleBackground(badgeView);
        badgeView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 列表角标显隐（信息预览条目）。
     */
    public static void applyBadge(@Nullable View badgeView, @Nullable PreviewInfoBean bean) {
        if (badgeView == null) {
            return;
        }
        boolean show = from(bean).hasMotionVideo()
                || (bean != null && MotionPhotoResolver.hasSidecarMotion(bean.getPath()));
        ensureCapsuleBackground(badgeView);
        badgeView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 列表胶囊默认底色（与 {@link io.coderf.arklab.common.widget.customview.LivePhotoView} 一致）。
     */
    private static final int DEFAULT_BADGE_BG_COLOR = 0xB3666666;

    /**
     * 保证列表胶囊有圆角背景：用 {@link ShapeUtils} 生成，不依赖独立 shape XML。
     */
    private static void ensureCapsuleBackground(@NonNull View badgeView) {
        if (badgeView.getBackground() != null) {
            return;
        }
        float radiusPx = io.coderf.arklab.core.utils.DensityUtil.dp2px(badgeView.getContext(), 999f);
        ShapeUtils.apply(badgeView, radiusPx, DEFAULT_BADGE_BG_COLOR);
    }
}
