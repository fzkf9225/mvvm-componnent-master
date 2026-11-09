package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textview.MaterialTextView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.utils.common.LivePhotoInfo;
import io.coderf.arklab.common.utils.common.MotionPhotoResolver;
import io.coderf.arklab.common.utils.common.ShapeUtils;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.gallery.attacher.PhotoViewAttacher;
import io.coderf.arklab.common.widget.gallery.bean.PreviewInfoBean;
import io.coderf.arklab.common.widget.gallery.config.PreviewGalleryZoomConfig;
import io.coderf.arklab.common.widget.gallery.impl.PhotoView;
import io.coderf.arklab.common.widget.gallery.inter.IPhotoView;
import io.coderf.arklab.core.utils.DensityUtil;
import io.coderf.arklab.core.utils.ThreadExecutorBounded;

/**
 * 实况图预览控件：静图（{@link PhotoView}）+ 短视频（{@link TextureView}）。
 * <p>
 * L2 交互：按住播放动感片段，松手回到静图。非实况时退化为普通可缩放静图。
 * 微信款「实况」胶囊默认左下角；图标 / 文案 / 间距 / padding / 圆角背景均可配置。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/9
 * @updated 2026/11/9 播放状态回调 + 播放中图标
 */
public class LivePhotoView extends FrameLayout {

    private static final String TAG = "LivePhotoView";
    /**
     * 按住多久后开始播放，短于系统长按，避免与保存长按抢手势
     */
    private static final long HOLD_TO_PLAY_MS = 180L;
    /**
     * 默认胶囊底色：深灰半透明（微信款）。
     * 不用纯黑半透明，避免在大图预览黑底上融为一体。
     */
    private static final int DEFAULT_BADGE_BG_COLOR = 0xB3666666;

    /**
     * 实况角标四角位置，默认 {@link #BOTTOM_LEFT}。
     */
    public enum BadgePosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT;

        @NonNull
        public static BadgePosition fromAttr(int value) {
            return switch (value) {
                case 0 -> TOP_LEFT;
                case 1 -> TOP_RIGHT;
                case 3 -> BOTTOM_RIGHT;
                default -> BOTTOM_LEFT;
            };
        }
    }

    /** 静图层，支持双指缩放；非实况时即普通大图 */
    private PhotoView stillView;
    /** 动感短视频层，按住播放时盖在静图上 */
    private TextureView textureView;
    /** 「实况」胶囊根布局 */
    private View liveBadge;
    /** 胶囊内实况图标 */
    private ImageView liveBadgeIcon;
    /** 胶囊内文案，默认「实况」 */
    private MaterialTextView liveBadgeText;

    /** 动感片段播放器；无实况时为 null */
    @Nullable
    private MediaPlayer mediaPlayer;
    /** 当前静图地址（path / uri / url） */
    @Nullable
    private String stillPath;
    /** 动感视频地址；来自 {@code AttachmentBean.liveInfo} 或自动解析（sidecar / 内嵌 Motion Photo） */
    @Nullable
    private String motionVideoPath;
    /** 是否具备可播放的动感片段（决定胶囊显隐与按住播） */
    private boolean motionEnabled;
    /** TextureView 的 Surface 是否已就绪 */
    private boolean surfaceReady;
    /** MediaPlayer 是否已 prepare 完成 */
    private boolean playerPrepared;
    /** 当前是否正在播放动感片段 */
    private boolean playing;
    /** 手指是否仍按在实况区域（松手即停） */
    private boolean holdPressed;
    /** prepare 尚未完成时，是否在就绪后立即起播 */
    private boolean wantPlayWhenPrepared;
    /** 动感视频宽，用于 TextureView 等比变换 */
    private int videoWidth;
    /** 动感视频高，用于 TextureView 等比变换 */
    private int videoHeight;

    /** 按住起点 X，用于滑动超阈值时取消起播 */
    private float downX;
    /** 按住起点 Y */
    private float downY;
    /** 系统 touchSlop，滑动超过则取消按住播放 */
    private final int touchSlop;
    /** 延迟起播任务（按住超过 {@link #HOLD_TO_PLAY_MS} 后执行） */
    private final Runnable startPlayRunnable = this::startMotionInternal;
    /**
     * 绑定代数：每次 {@link #bind} 递增，用于丢弃过期的异步解析结果
     */
    private int bindGeneration;
    /**
     * 当 {@link LivePhotoInfo} 无视频路径时，是否自动解析同名 MOV / 内嵌 Motion Photo，默认 true
     */
    private boolean autoResolveEnabled = true;
    /**
     * 胶囊锚点位置，默认左下角（微信款）
     */
    @NonNull
    private BadgePosition badgePosition = BadgePosition.BOTTOM_LEFT;
    /**
     * 胶囊与容器左/右边缘的外边距（px），默认约 16dp
     */
    private int badgeMarginHorizontalPx;
    /**
     * 胶囊与容器上/下边缘的外边距（px），默认约 14dp
     */
    private int badgeMarginVerticalPx;
    /**
     * 胶囊内实况图标边长（px），默认约 14dp
     */
    private int badgeIconSizePx;
    /**
     * 胶囊内图标与文字间距（px），默认约 4dp
     */
    private int badgeIconTextSpacingPx;
    /**
     * 胶囊内容左右内边距（px），默认约 9dp
     */
    private int badgePaddingHorizontalPx;
    /**
     * 胶囊内容上下内边距（px），默认约 5dp
     */
    private int badgePaddingVerticalPx;
    /**
     * 胶囊圆角半径（px）；足够大即为完全胶囊形
     */
    private int badgeCornerRadiusPx;
    /**
     * 胶囊背景色，默认深灰半透明 {@link #DEFAULT_BADGE_BG_COLOR}
     */
    @ColorInt
    private int badgeBackgroundColor = DEFAULT_BADGE_BG_COLOR;
    /**
     * 胶囊文字颜色，同时用于图标着色，默认白色
     */
    @ColorInt
    private int badgeTextColor = Color.WHITE;
    /**
     * 胶囊文字字号（px），默认约 12sp
     */
    private float badgeTextSizePx;
    /**
     * 胶囊文案内容，默认「实况」
     */
    @Nullable
    private CharSequence badgeText;
    /**
     * 胶囊内静止态实况图标资源（未播放）
     */
    @DrawableRes
    private int badgeIconRes = R.drawable.ic_live_photo_play;
    /**
     * 胶囊内播放中实况图标资源
     */
    @DrawableRes
    private int badgePlayingIconRes = R.drawable.ic_live_photo_playing;
    /**
     * 播放中图标边长（px）；&lt;=0 时与 {@link #badgeIconSizePx} 相同
     */
    private int badgePlayingIconSizePx;
    /**
     * 当前播放状态
     */
    @NonNull
    private PlaybackState playbackState = PlaybackState.IDLE;
    /**
     * 动感片段可用性回调（含异步自动解析成功）
     */
    @Nullable
    private OnMotionAvailabilityListener motionAvailabilityListener;
    /**
     * 实况播放状态回调
     */
    @Nullable
    private OnPlaybackStateListener playbackStateListener;

    /**
     * 实况动感片段播放状态。
     */
    public enum PlaybackState {
        /** 未播放（静图） */
        IDLE,
        /** 已按住，正在准备播放器 */
        PREPARING,
        /** 正在播放动感片段 */
        PLAYING
    }

    /**
     * 动感片段可用性变化（含异步自动解析成功）。
     */
    public interface OnMotionAvailabilityListener {
        void onMotionAvailabilityChanged(boolean available);
    }

    /**
     * 实况播放状态变化监听。
     */
    public interface OnPlaybackStateListener {
        /**
         * @param view  当前控件
         * @param state 新状态
         */
        void onPlaybackStateChanged(@NonNull LivePhotoView view, @NonNull PlaybackState state);
    }

    public LivePhotoView(@NonNull Context context) {
        this(context, null);
    }

    public LivePhotoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LivePhotoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initDefaultBadgeMetrics(context);
        initAttrs(context, attrs);
        init(context);
        applyBadgeStyle();
    }

    private void initDefaultBadgeMetrics(@NonNull Context context) {
        // 水平略大于垂直，贴边更接近微信大图预览观感
        badgeMarginHorizontalPx = DensityUtil.dp2px(context, 16f);
        badgeMarginVerticalPx = DensityUtil.dp2px(context, 14f);
        badgeIconSizePx = DensityUtil.dp2px(context, 14f);
        // 播放中图标默认与静止态同尺寸
        badgePlayingIconSizePx = badgeIconSizePx;
        badgeIconTextSpacingPx = DensityUtil.dp2px(context, 4f);
        badgePaddingHorizontalPx = DensityUtil.dp2px(context, 9f);
        badgePaddingVerticalPx = DensityUtil.dp2px(context, 5f);
        // 足够大即成胶囊
        badgeCornerRadiusPx = DensityUtil.dp2px(context, 999f);
        badgeTextSizePx = DensityUtil.sp2px(context, 12f);
        badgeText = context.getString(R.string.live_photo_badge);
    }

    private void initAttrs(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LivePhotoView);
        try {
            badgePosition = BadgePosition.fromAttr(
                    a.getInt(R.styleable.LivePhotoView_liveBadgePosition, 2));
            badgeMarginHorizontalPx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgeMarginHorizontal, badgeMarginHorizontalPx);
            badgeMarginVerticalPx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgeMarginVertical, badgeMarginVerticalPx);
            badgeIconRes = a.getResourceId(R.styleable.LivePhotoView_liveBadgeIcon, badgeIconRes);
            badgePlayingIconRes = a.getResourceId(
                    R.styleable.LivePhotoView_liveBadgePlayingIcon, badgePlayingIconRes);
            badgeIconSizePx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgeIconSize, badgeIconSizePx);
            // 未单独配置播放中尺寸时，与静止态保持一致
            badgePlayingIconSizePx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgePlayingIconSize, badgeIconSizePx);
            if (a.hasValue(R.styleable.LivePhotoView_liveBadgeText)) {
                badgeText = a.getText(R.styleable.LivePhotoView_liveBadgeText);
            }
            badgeTextSizePx = a.getDimension(
                    R.styleable.LivePhotoView_liveBadgeTextSize, badgeTextSizePx);
            badgeTextColor = a.getColor(R.styleable.LivePhotoView_liveBadgeTextColor, badgeTextColor);
            badgeIconTextSpacingPx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgeIconTextSpacing, badgeIconTextSpacingPx);
            badgePaddingHorizontalPx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgePaddingHorizontal, badgePaddingHorizontalPx);
            badgePaddingVerticalPx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgePaddingVertical, badgePaddingVerticalPx);
            badgeBackgroundColor = a.getColor(
                    R.styleable.LivePhotoView_liveBadgeBackgroundColor, badgeBackgroundColor);
            badgeCornerRadiusPx = a.getDimensionPixelSize(
                    R.styleable.LivePhotoView_liveBadgeCornerRadius, badgeCornerRadiusPx);
        } finally {
            a.recycle();
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_live_photo, this, true);
        stillView = findViewById(R.id.live_still_view);
        textureView = findViewById(R.id.live_texture_view);
        liveBadge = findViewById(R.id.live_badge);
        liveBadgeIcon = liveBadge.findViewById(R.id.live_badge_icon);
        liveBadgeText = liveBadge.findViewById(R.id.live_badge_text);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        installTouchHook();
        installTextureTouch();
    }

    /**
     * 配置胶囊锚点：左上 / 右上 / 左下（默认）/ 右下。
     */
    public void setBadgePosition(@NonNull BadgePosition position) {
        this.badgePosition = position;
        applyBadgeLayout();
    }

    /** @return 当前胶囊锚点 */
    @NonNull
    public BadgePosition getBadgePosition() {
        return badgePosition;
    }

    /**
     * 同时设置胶囊水平 / 垂直外边距（px）。
     * <p>外边距指胶囊相对 {@link LivePhotoView} 容器边缘，不是胶囊内部 padding。</p>
     */
    public void setBadgeMarginPx(int horizontalPx, int verticalPx) {
        this.badgeMarginHorizontalPx = Math.max(0, horizontalPx);
        this.badgeMarginVerticalPx = Math.max(0, verticalPx);
        applyBadgeLayout();
    }

    /**
     * 同时设置胶囊水平 / 垂直外边距（dp）。
     */
    public void setBadgeMarginDp(float horizontalDp, float verticalDp) {
        setBadgeMarginPx(
                DensityUtil.dp2px(getContext(), horizontalDp),
                DensityUtil.dp2px(getContext(), verticalDp));
    }

    /**
     * 胶囊与容器左/右边缘的外边距（px）。
     */
    public void setBadgeMarginHorizontalPx(int marginPx) {
        this.badgeMarginHorizontalPx = Math.max(0, marginPx);
        applyBadgeLayout();
    }

    /** @see #setBadgeMarginHorizontalPx(int) */
    public void setBadgeMarginHorizontalDp(float marginDp) {
        setBadgeMarginHorizontalPx(DensityUtil.dp2px(getContext(), marginDp));
    }

    /**
     * 胶囊与容器上/下边缘的外边距（px）。
     */
    public void setBadgeMarginVerticalPx(int marginPx) {
        this.badgeMarginVerticalPx = Math.max(0, marginPx);
        applyBadgeLayout();
    }

    /** @see #setBadgeMarginVerticalPx(int) */
    public void setBadgeMarginVerticalDp(float marginDp) {
        setBadgeMarginVerticalPx(DensityUtil.dp2px(getContext(), marginDp));
    }

    /**
     * 胶囊内静止态实况图标资源（未播放）。
     */
    public void setBadgeIcon(@DrawableRes int iconRes) {
        this.badgeIconRes = iconRes;
        applyBadgeContent();
    }

    /**
     * 胶囊内播放中实况图标资源。
     */
    public void setBadgePlayingIcon(@DrawableRes int iconRes) {
        this.badgePlayingIconRes = iconRes;
        applyBadgeContent();
    }

    /**
     * 胶囊内静止态实况图标边长（px）。
     * <p>若播放中尺寸未单独设置过，会同步为相同值。</p>
     */
    public void setBadgeIconSizePx(int sizePx) {
        int old = this.badgeIconSizePx;
        this.badgeIconSizePx = Math.max(1, sizePx);
        if (badgePlayingIconSizePx == old || badgePlayingIconSizePx <= 0) {
            badgePlayingIconSizePx = this.badgeIconSizePx;
        }
        applyBadgeContent();
    }

    /** @see #setBadgeIconSizePx(int) */
    public void setBadgeIconSizeDp(float sizeDp) {
        setBadgeIconSizePx(DensityUtil.dp2px(getContext(), sizeDp));
    }

    /**
     * 胶囊内播放中图标边长（px）；默认与静止态相同。
     */
    public void setBadgePlayingIconSizePx(int sizePx) {
        this.badgePlayingIconSizePx = Math.max(1, sizePx);
        applyBadgeContent();
    }

    /** @see #setBadgePlayingIconSizePx(int) */
    public void setBadgePlayingIconSizeDp(float sizeDp) {
        setBadgePlayingIconSizePx(DensityUtil.dp2px(getContext(), sizeDp));
    }

    /**
     * 设置实况播放状态监听。
     */
    public void setOnPlaybackStateListener(@Nullable OnPlaybackStateListener listener) {
        this.playbackStateListener = listener;
    }

    /**
     * @return 当前实况播放状态
     */
    @NonNull
    public PlaybackState getPlaybackState() {
        return playbackState;
    }

    /**
     * 胶囊文案内容，例如「实况」。
     */
    public void setBadgeText(@Nullable CharSequence text) {
        this.badgeText = text;
        applyBadgeContent();
    }

    /**
     * 胶囊文字字号（px）。
     */
    public void setBadgeTextSizePx(float textSizePx) {
        this.badgeTextSizePx = Math.max(1f, textSizePx);
        applyBadgeContent();
    }

    /** @see #setBadgeTextSizePx(float) */
    public void setBadgeTextSizeSp(float textSizeSp) {
        setBadgeTextSizePx(DensityUtil.sp2px(getContext(), textSizeSp));
    }

    /**
     * 胶囊文字颜色；同时用于实况图标着色。
     */
    public void setBadgeTextColor(@ColorInt int color) {
        this.badgeTextColor = color;
        applyBadgeContent();
    }

    /**
     * 图标与文字间距（px）。
     */
    public void setBadgeIconTextSpacingPx(int spacingPx) {
        this.badgeIconTextSpacingPx = Math.max(0, spacingPx);
        applyBadgeContent();
    }

    /** @see #setBadgeIconTextSpacingPx(int) */
    public void setBadgeIconTextSpacingDp(float spacingDp) {
        setBadgeIconTextSpacingPx(DensityUtil.dp2px(getContext(), spacingDp));
    }

    /**
     * 胶囊内容左右内边距（px）。
     */
    public void setBadgePaddingHorizontalPx(int paddingPx) {
        this.badgePaddingHorizontalPx = Math.max(0, paddingPx);
        applyBadgeContent();
    }

    /** @see #setBadgePaddingHorizontalPx(int) */
    public void setBadgePaddingHorizontalDp(float paddingDp) {
        setBadgePaddingHorizontalPx(DensityUtil.dp2px(getContext(), paddingDp));
    }

    /**
     * 胶囊内容上下内边距（px）。
     */
    public void setBadgePaddingVerticalPx(int paddingPx) {
        this.badgePaddingVerticalPx = Math.max(0, paddingPx);
        applyBadgeContent();
    }

    /** @see #setBadgePaddingVerticalPx(int) */
    public void setBadgePaddingVerticalDp(float paddingDp) {
        setBadgePaddingVerticalPx(DensityUtil.dp2px(getContext(), paddingDp));
    }

    /**
     * 同时设置胶囊水平 / 垂直内边距（px）。
     */
    public void setBadgePaddingPx(int horizontalPx, int verticalPx) {
        this.badgePaddingHorizontalPx = Math.max(0, horizontalPx);
        this.badgePaddingVerticalPx = Math.max(0, verticalPx);
        applyBadgeContent();
    }

    /** @see #setBadgePaddingPx(int, int) */
    public void setBadgePaddingDp(float horizontalDp, float verticalDp) {
        setBadgePaddingPx(
                DensityUtil.dp2px(getContext(), horizontalDp),
                DensityUtil.dp2px(getContext(), verticalDp));
    }

    /**
     * 胶囊背景色（建议带透明度的深灰，避免在大图黑底上融为一体）。
     */
    public void setBadgeBackgroundColor(@ColorInt int color) {
        this.badgeBackgroundColor = color;
        applyBadgeBackground();
    }

    /**
     * 胶囊圆角半径（px）。传很大的值（如 999dp）即完全胶囊形。
     */
    public void setBadgeCornerRadiusPx(int radiusPx) {
        this.badgeCornerRadiusPx = Math.max(0, radiusPx);
        applyBadgeBackground();
    }

    /** @see #setBadgeCornerRadiusPx(int) */
    public void setBadgeCornerRadiusDp(float radiusDp) {
        setBadgeCornerRadiusPx(DensityUtil.dp2px(getContext(), radiusDp));
    }

    /** 一次性应用位置、背景与内容样式 */
    private void applyBadgeStyle() {
        applyBadgeLayout();
        applyBadgeBackground();
        applyBadgeContent();
    }

    /** 按锚点与水平/垂直外边距摆放胶囊 */
    private void applyBadgeLayout() {
        if (liveBadge == null) {
            return;
        }
        LayoutParams lp = (LayoutParams) liveBadge.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        lp.gravity = switch (badgePosition) {
            case TOP_LEFT -> android.view.Gravity.START | android.view.Gravity.TOP;
            case TOP_RIGHT -> android.view.Gravity.END | android.view.Gravity.TOP;
            case BOTTOM_RIGHT -> android.view.Gravity.END | android.view.Gravity.BOTTOM;
            default -> android.view.Gravity.START | android.view.Gravity.BOTTOM;
        };
        lp.setMargins(
                badgeMarginHorizontalPx,
                badgeMarginVerticalPx,
                badgeMarginHorizontalPx,
                badgeMarginVerticalPx);
        liveBadge.setLayoutParams(lp);
    }

    /**
     * 用 {@link ShapeUtils} 绘制胶囊圆角背景（MaterialShapeDrawable），避免冗余 shape XML。
     */
    private void applyBadgeBackground() {
        if (liveBadge == null) {
            return;
        }
        ShapeUtils.apply(liveBadge, badgeCornerRadiusPx, badgeBackgroundColor);
        liveBadge.setClipToOutline(true);
    }

    /** 刷新胶囊内图标、文案、间距与 padding */
    private void applyBadgeContent() {
        if (liveBadge == null) {
            return;
        }
        liveBadge.setPadding(
                badgePaddingHorizontalPx,
                badgePaddingVerticalPx,
                badgePaddingHorizontalPx,
                badgePaddingVerticalPx);

        if (liveBadgeIcon != null) {
            boolean isPlayingState = playbackState == PlaybackState.PLAYING;
            int iconSize = isPlayingState
                    ? (badgePlayingIconSizePx > 0 ? badgePlayingIconSizePx : badgeIconSizePx)
                    : badgeIconSizePx;
            int iconRes = isPlayingState ? badgePlayingIconRes : badgeIconRes;
            ViewGroup.LayoutParams iconLp = liveBadgeIcon.getLayoutParams();
            if (iconLp != null) {
                iconLp.width = iconSize;
                iconLp.height = iconSize;
                liveBadgeIcon.setLayoutParams(iconLp);
            }
            liveBadgeIcon.setImageResource(iconRes);
            try {
                liveBadgeIcon.setColorFilter(badgeTextColor);
            } catch (Exception ignore) {
            }
        }

        if (liveBadgeText != null) {
            ViewGroup.MarginLayoutParams textLp =
                    (ViewGroup.MarginLayoutParams) liveBadgeText.getLayoutParams();
            if (textLp != null) {
                textLp.setMarginStart(badgeIconTextSpacingPx);
                liveBadgeText.setLayoutParams(textLp);
            }
            liveBadgeText.setText(badgeText);
            liveBadgeText.setTextColor(badgeTextColor);
            liveBadgeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, badgeTextSizePx);
        }
    }

    /**
     * 把按住播放挂在 PhotoView 触摸链上，并继续把事件交给 {@link PhotoViewAttacher} 做缩放拖动。
     */
    @SuppressLint("ClickableViewAccessibility")
    private void installTouchHook() {
        IPhotoView impl = stillView.getIPhotoViewImplementation();
        if (!(impl instanceof PhotoViewAttacher attacher)) {
            return;
        }
        stillView.setOnTouchListener((v, event) -> {
            if (motionEnabled) {
                handleHoldToPlay(event);
            }
            return attacher.onTouch(v, event);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void installTextureTouch() {
        textureView.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                holdPressed = false;
                stopMotion();
                getParentRequestDisallow(false);
            }
            return true;
        });
    }

    private void handleHoldToPlay(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                holdPressed = true;
                downX = event.getX();
                downY = event.getY();
                wantPlayWhenPrepared = false;
                removeCallbacks(startPlayRunnable);
                postDelayed(startPlayRunnable, HOLD_TO_PLAY_MS);
                getParentRequestDisallow(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (holdPressed && !playing) {
                    float dx = Math.abs(event.getX() - downX);
                    float dy = Math.abs(event.getY() - downY);
                    if (dx > touchSlop || dy > touchSlop) {
                        removeCallbacks(startPlayRunnable);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                holdPressed = false;
                wantPlayWhenPrepared = false;
                removeCallbacks(startPlayRunnable);
                stopMotion();
                getParentRequestDisallow(false);
                break;
            default:
                break;
        }
    }

    private void getParentRequestDisallow(boolean disallow) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(disallow);
        }
    }

    /**
     * @return 内部静图 {@link PhotoView}，用于单击关闭、长按保存、缩放配置等
     */
    @NonNull
    public PhotoView getPhotoView() {
        return stillView;
    }

    /** @return 当前是否具备可播放动感片段 */
    public boolean isMotionEnabled() {
        return motionEnabled;
    }

    /** @return 是否正在按住播放动感片段 */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * 是否在 {@link LivePhotoInfo} 无视频路径时自动解析 sidecar / 内嵌 Motion Photo，默认 true。
     */
    public void setAutoResolveEnabled(boolean autoResolveEnabled) {
        this.autoResolveEnabled = autoResolveEnabled;
    }

    /**
     * 监听动感片段是否可用（含异步自动解析成功后的回调）。
     */
    public void setOnMotionAvailabilityListener(@Nullable OnMotionAvailabilityListener listener) {
        this.motionAvailabilityListener = listener;
    }

    /**
     * 应用大图预览缩放配置（透传给 {@link PhotoView}）。
     */
    public void applyZoomConfig(@Nullable PreviewGalleryZoomConfig zoomConfig) {
        stillView.applyZoomConfig(zoomConfig);
    }

    /**
     * 图片缩放到边缘时是否允许父布局拦截滑动（ViewPager2 翻页）。
     */
    public void setAllowParentInterceptOnEdge(boolean allow) {
        stillView.setAllowParentInterceptOnEdge(allow);
    }

    /**
     * 绑定附件：静图 path + {@code liveInfo} 中的动感视频；缺失时按 {@link #autoResolveEnabled} 自动解析。
     */
    public void bind(@Nullable AttachmentBean bean,
                     @Nullable Drawable placeholder,
                     @Nullable Drawable error) {
        if (bean == null) {
            clear();
            return;
        }
        bind(bean.getPath(), LivePhotoInfo.from(bean), placeholder, error);
    }

    /**
     * 绑定信息预览条目（委托 {@link PreviewInfoBean} 的 path / meta / attachment.liveInfo）。
     */
    public void bind(@Nullable PreviewInfoBean bean,
                     @Nullable Drawable placeholder,
                     @Nullable Drawable error) {
        if (bean == null) {
            clear();
            return;
        }
        bind(bean.getPath(), LivePhotoInfo.from(bean), placeholder, error);
    }

    /**
     * 绑定静图与实况信息。
     *
     * @param stillImagePath 静图地址
     * @param livePhotoInfo  实况扩展，null / 无视频时仅静图（仍可能自动解析）
     * @param placeholder    加载占位图
     * @param error          加载失败图
     */
    public void bind(@Nullable String stillImagePath,
                     @Nullable LivePhotoInfo livePhotoInfo,
                     @Nullable Drawable placeholder,
                     @Nullable Drawable error) {
        bind(stillImagePath, livePhotoInfo, placeholder, error, autoResolveEnabled);
    }

    /**
     * 绑定静图与实况信息。
     *
     * @param stillImagePath   静图地址
     * @param livePhotoInfo    实况扩展
     * @param placeholder      加载占位图
     * @param error            加载失败图
     * @param resolveIfMissing 无视频路径时是否后台自动解析 sidecar / 内嵌 Motion Photo
     */
    public void bind(@Nullable String stillImagePath,
                     @Nullable LivePhotoInfo livePhotoInfo,
                     @Nullable Drawable placeholder,
                     @Nullable Drawable error,
                     boolean resolveIfMissing) {
        final int generation = ++bindGeneration;
        stopMotion();
        releasePlayerOnly();
        stillPath = stillImagePath;
        LivePhotoInfo info = livePhotoInfo == null ? LivePhotoInfo.EMPTY : livePhotoInfo;
        applyMotionState(info.getMotionVideoPath(), info.hasMotionVideo());
        playerPrepared = false;
        wantPlayWhenPrepared = false;
        videoWidth = 0;
        videoHeight = 0;
        textureView.setVisibility(GONE);
        textureView.setTransform(new Matrix());

        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .error(error);
        Glide.with(getContext())
                .asBitmap()
                .load(stillPath)
                .apply(options)
                .into(stillView);

        if (motionEnabled && surfaceReady) {
            preparePlayer();
        }
        notifyMotionAvailability(motionEnabled);

        if (!motionEnabled && resolveIfMissing && !TextUtils.isEmpty(stillPath)) {
            final Context appContext = getContext().getApplicationContext();
            final String pathToResolve = stillPath;
            ThreadExecutorBounded.getInstance().execute(() -> {
                MotionPhotoResolver.ResolveResult result =
                        MotionPhotoResolver.resolve(appContext, pathToResolve);
                post(() -> {
                    if (generation != bindGeneration) {
                        return;
                    }
                    if (!result.hasMotion()) {
                        return;
                    }
                    applyMotionState(result.motionVideoPath, true);
                    if (surfaceReady) {
                        preparePlayer();
                    }
                    notifyMotionAvailability(true);
                });
            });
        }
    }

    private void applyMotionState(@Nullable String motionPath, boolean enabled) {
        motionVideoPath = motionPath;
        motionEnabled = enabled && !TextUtils.isEmpty(motionPath);
        liveBadge.setVisibility(motionEnabled ? VISIBLE : GONE);
    }

    private void notifyMotionAvailability(boolean available) {
        if (motionAvailabilityListener != null) {
            motionAvailabilityListener.onMotionAvailabilityChanged(available);
        }
    }

    /**
     * 仅加载静图（明确关闭实况，不做自动解析）。
     */
    public void bindStillOnly(@Nullable String stillImagePath,
                              @Nullable Drawable placeholder,
                              @Nullable Drawable error) {
        bind(stillImagePath, LivePhotoInfo.EMPTY, placeholder, error, false);
    }

    private void startMotionInternal() {
        if (!motionEnabled || !holdPressed || TextUtils.isEmpty(motionVideoPath)) {
            return;
        }
        if (playerPrepared && mediaPlayer != null) {
            beginPlayback();
            return;
        }
        wantPlayWhenPrepared = true;
        setPlaybackState(PlaybackState.PREPARING);
        if (surfaceReady) {
            preparePlayer();
        } else {
            textureView.setVisibility(VISIBLE);
        }
    }

    private void beginPlayback() {
        if (mediaPlayer == null || !holdPressed) {
            return;
        }
        try {
            textureView.setVisibility(VISIBLE);
            applyVideoTransform();
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            playing = true;
            stillView.setZoomable(false);
            setPlaybackState(PlaybackState.PLAYING);
        } catch (Exception e) {
            LogUtil.e(TAG, "start motion failed: " + e);
            stopMotion();
        }
    }

    /**
     * 更新播放状态：刷新胶囊图标，并在状态变化时回调监听。
     */
    private void setPlaybackState(@NonNull PlaybackState newState) {
        if (playbackState == newState) {
            return;
        }
        playbackState = newState;
        applyBadgeContent();
        if (playbackStateListener != null) {
            playbackStateListener.onPlaybackStateChanged(this, newState);
        }
    }

    /**
     * 按静图显示区域等比适配视频，避免 TextureView 默认拉伸铺满。
     */
    private void applyVideoTransform() {
        int viewW = textureView.getWidth();
        int viewH = textureView.getHeight();
        if (viewW <= 0 || viewH <= 0 || videoWidth <= 0 || videoHeight <= 0) {
            return;
        }

        RectF displayRect = stillView.getDisplayRect();
        float targetLeft;
        float targetTop;
        float targetW;
        float targetH;
        if (displayRect != null && displayRect.width() > 1f && displayRect.height() > 1f) {
            // 优先对齐 PhotoView 当前静图显示框，保证播实时与静图重合
            float scale = Math.min(displayRect.width() / videoWidth, displayRect.height() / videoHeight);
            targetW = videoWidth * scale;
            targetH = videoHeight * scale;
            targetLeft = displayRect.centerX() - targetW / 2f;
            targetTop = displayRect.centerY() - targetH / 2f;
        } else {
            // 回退：容器内 FIT_CENTER
            float scale = Math.min((float) viewW / videoWidth, (float) viewH / videoHeight);
            targetW = videoWidth * scale;
            targetH = videoHeight * scale;
            targetLeft = (viewW - targetW) / 2f;
            targetTop = (viewH - targetH) / 2f;
        }

        Matrix matrix = new Matrix();
        // TextureView 默认把内容拉伸到 viewW x viewH，这里缩放到目标显示框
        matrix.setScale(targetW / viewW, targetH / viewH);
        matrix.postTranslate(targetLeft, targetTop);
        textureView.setTransform(matrix);
    }

    /**
     * 停止动感播放并隐藏 TextureView，回到静图；缩放重新启用。
     */
    public void stopMotion() {
        wantPlayWhenPrepared = false;
        removeCallbacks(startPlayRunnable);
        if (mediaPlayer != null && playing) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (Exception ignore) {
            }
        }
        playing = false;
        textureView.setVisibility(GONE);
        stillView.setZoomable(true);
        setPlaybackState(PlaybackState.IDLE);
    }

    private void preparePlayer() {
        if (!motionEnabled || TextUtils.isEmpty(motionVideoPath) || !surfaceReady) {
            return;
        }
        releasePlayerOnly();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setLooping(true);
            setPlayerDataSource(mediaPlayer, motionVideoPath);
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            if (surfaceTexture == null) {
                return;
            }
            mediaPlayer.setSurface(new android.view.Surface(surfaceTexture));
            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
                videoWidth = width;
                videoHeight = height;
                applyVideoTransform();
            });
            mediaPlayer.setOnPreparedListener(mp -> {
                playerPrepared = true;
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
                applyVideoTransform();
                if (wantPlayWhenPrepared && holdPressed) {
                    beginPlayback();
                }
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                LogUtil.e(TAG, "MediaPlayer error what=" + what + " extra=" + extra);
                playerPrepared = false;
                stopMotion();
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            LogUtil.e(TAG, "prepare motion failed: " + e);
            releasePlayerOnly();
            stopMotion();
        }
    }

    private void setPlayerDataSource(@NonNull MediaPlayer player, @NonNull String path) throws Exception {
        if (path.startsWith("content://")
                || path.startsWith("file://")
                || path.startsWith("http://")
                || path.startsWith("https://")
                || path.startsWith("android.resource://")) {
            player.setDataSource(getContext(), Uri.parse(path));
        } else {
            player.setDataSource(path);
        }
    }

    private void releasePlayerOnly() {
        playerPrepared = false;
        videoWidth = 0;
        videoHeight = 0;
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignore) {
            }
            mediaPlayer = null;
        }
    }

    /**
     * 清空静图 / 实况状态并释放播放器，胶囊隐藏。
     */
    public void clear() {
        stopMotion();
        releasePlayerOnly();
        stillPath = null;
        motionVideoPath = null;
        motionEnabled = false;
        liveBadge.setVisibility(GONE);
        textureView.setVisibility(GONE);
        stillView.setImageDrawable(null);
    }

    /**
     * 释放播放资源；ViewPager {@code onViewRecycled} 或 Dialog dismiss 时调用。
     */
    public void release() {
        holdPressed = false;
        wantPlayWhenPrepared = false;
        removeCallbacks(startPlayRunnable);
        stopMotion();
        releasePlayerOnly();
    }

    @Override
    protected void onDetachedFromWindow() {
        // 翻页/回收时停播即可；完整释放由 Adapter.onViewRecycled / Dialog 关闭调用 release()
        stopMotion();
        releasePlayerOnly();
        super.onDetachedFromWindow();
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    surfaceReady = true;
                    if (motionEnabled && !playerPrepared) {
                        preparePlayer();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                    applyVideoTransform();
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    surfaceReady = false;
                    releasePlayerOnly();
                    playing = false;
                    setPlaybackState(PlaybackState.IDLE);
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                }
            };
}
