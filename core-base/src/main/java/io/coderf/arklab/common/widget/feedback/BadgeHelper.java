package io.coderf.arklab.common.widget.feedback;

import android.content.Context;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * Tab / 底栏角标工具。推荐使用 {@link #attach(View, int, BadgeConfig)} 获取 {@link BadgeHandle}，
 * 由句柄负责更新与 {@link BadgeHandle#detach()}；同一锚点重复 attach 会先移除旧角标。
 *
 * <p>material 1.14 起官方挂载机制为锚点 ViewOverlay，因此任意 View 均可挂角标、
 * 同一容器多锚点互不冲突，无需宿主容器配合。角标默认位于锚点右上角（RTL 自动翻转）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/20 13:33
 */
@ExperimentalBadgeUtils
public final class BadgeHelper {

    private static final String TAG = "BadgeHelper";

    /** {@link BadgeDrawable} 内部 TextDrawableHelper 字段名，仅用于反射设置字号（官方无公开 API）。 */
    private static final String FIELD_TEXT_DRAWABLE_HELPER = "textDrawableHelper";

    /** 锚点 → 句柄注册表。键为弱引用：锚点销毁后条目自动清理；仅主线程访问。 */
    private static final Map<View, BadgeHandle> REGISTRY = new WeakHashMap<>();

    private BadgeHelper() {
    }

    /**
     * 给任意 View 挂数字角标，样式按 {@link BadgeConfig} 配置。
     * 锚点未布局完成时先挂监听，布局完成后自动挂载；布局变化时角标坐标自动跟随。
     * 需在主线程调用；页面销毁前应调用 {@link BadgeHandle#detach()}。
     *
     * @param anchor 锚点 View
     * @param number 初始数字，&lt;=0 时挂载后隐藏
     * @param config 角标样式配置
     * @return 角标句柄，用于更新/移除
     */
    @NonNull
    public static BadgeHandle attach(@NonNull View anchor, int number, @NonNull BadgeConfig config) {
        detachExisting(anchor);
        BadgeDrawable badge = createBadge(anchor.getContext(), config);
        applyNumber(badge, number);
        BadgeHandle handle = new BadgeHandle(anchor, badge);
        REGISTRY.put(anchor, handle);
        return handle;
    }

    /**
     * 给任意 View 挂文字角标（如 "NEW"），样式按 {@link BadgeConfig} 配置。
     * 文字优先于数字显示，传 null/空串时角标隐藏。
     *
     * @param anchor 锚点 View
     * @param text   角标文字，null/空串表示先隐藏
     * @param config 角标样式配置
     * @return 角标句柄，用于更新/移除
     */
    @NonNull
    public static BadgeHandle attachText(@NonNull View anchor, @Nullable String text,
                                         @NonNull BadgeConfig config) {
        detachExisting(anchor);
        BadgeDrawable badge = createBadge(anchor.getContext(), config);
        BadgeHandle handle = new BadgeHandle(anchor, badge);
        handle.updateText(text);
        REGISTRY.put(anchor, handle);
        return handle;
    }

    /**
     * 挂数字角标，全部使用默认样式。仅兼容旧用法，请迁移到
     * {@link #attach(View, int, BadgeConfig)} 并持有 {@link BadgeHandle}；
     * 移除角标请用 {@link #detach(View, BadgeDrawable)}。
     *
     * @deprecated 返回的 {@link BadgeDrawable} 无句柄能力（持续坐标跟随、文字更新），
     *             请改用 {@link #attach(View, int, BadgeConfig)}
     */
    @Deprecated
    @NonNull
    public static BadgeDrawable attach(@NonNull View anchor, int number) {
        BadgeDrawable badge = createBadge(anchor.getContext(), BadgeConfig.builder().build());
        applyNumber(badge, number);
        // 兜底监听：锚点可能尚未布局完成，布局完成后再挂载；已布局时直接挂载并移除监听，避免重复 attach
        View.OnLayoutChangeListener listener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                BadgeUtils.attachBadgeDrawable(badge, v);
            }
        };
        anchor.addOnLayoutChangeListener(listener);
        if (anchor.isLaidOut()) {
            anchor.removeOnLayoutChangeListener(listener);
            BadgeUtils.attachBadgeDrawable(badge, anchor);
        }
        return badge;
    }

    /**
     * 更新数字角标：&gt;0 显示数字，&lt;=0 清空并隐藏。
     *
     * @deprecated 请改用 {@link BadgeHandle#update(int)}
     */
    @Deprecated
    public static void update(@Nullable BadgeDrawable badge, int number) {
        if (badge == null) {
            return;
        }
        applyNumber(badge, number);
    }

    /**
     * 移除角标：新 API 挂载的角标走句柄完整清理，旧 API 挂载的走官方移除。
     * 需在主线程调用。
     *
     * @param anchor 角标锚点
     * @param badge  待移除的角标，null 时不做任何事
     */
    public static void detach(@NonNull View anchor, @Nullable BadgeDrawable badge) {
        if (badge == null) {
            return;
        }
        BadgeHandle handle = REGISTRY.get(anchor);
        if (handle != null && handle.badge() == badge) {
            handle.detach();
            return;
        }
        // 旧 API 挂载的角标没有句柄，直接走官方移除
        BadgeUtils.detachBadgeDrawable(badge, anchor);
    }

    /** 移除同一锚点上已挂载的句柄（overlay 不可重复叠加同一 drawable）。 */
    private static void detachExisting(@NonNull View anchor) {
        BadgeHandle existing = REGISTRY.get(anchor);
        if (existing != null) {
            existing.detach();
        }
    }

    /** 句柄 detach 后从注册表移除，保证后续 attach 不会命中已失效句柄。 */
    static void unregister(@NonNull BadgeHandle handle) {
        Iterator<Map.Entry<View, BadgeHandle>> iterator = REGISTRY.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() == handle) {
                iterator.remove();
                break;
            }
        }
    }

    /** 按配置创建角标并应用颜色/位置/字号等样式，数字内容由调用方设置。 */
    @NonNull
    private static BadgeDrawable createBadge(@NonNull Context context, @NonNull BadgeConfig config) {
        BadgeDrawable badge = BadgeDrawable.create(context);
        if (config.textAppearanceRes != 0) {
            // textAppearance 优先级高于具体 sp 值；官方实现中文字颜色始终以 badgeTextColor 为准
            badge.setTextAppearance(config.textAppearanceRes);
        } else if (config.textSizeSp > 0) {
            applyTextSizeReflectively(badge, context, config.textSizeSp);
        }
        badge.setBackgroundColor(config.backgroundColor != BadgeConfig.UNSET_COLOR
                ? config.backgroundColor : ThemeAttrs.error(context));
        badge.setBadgeTextColor(config.textColor != BadgeConfig.UNSET_COLOR
                ? config.textColor : ThemeAttrs.onPrimary(context));
        badge.setBadgeGravity(config.badgeGravity);
        if (config.horizontalOffsetDp != BadgeConfig.UNSET_OFFSET_DP) {
            badge.setHorizontalOffset(DensityUtil.dp2px(context, config.horizontalOffsetDp));
        }
        if (config.verticalOffsetDp != BadgeConfig.UNSET_OFFSET_DP) {
            badge.setVerticalOffset(DensityUtil.dp2px(context, config.verticalOffsetDp));
        }
        if (config.maxNumber > 0) {
            badge.setMaxNumber(config.maxNumber);
        }
        if (config.maxCharacterCount > 0) {
            badge.setMaxCharacterCount(config.maxCharacterCount);
        }
        return badge;
    }

    /**
     * 设置角标文字大小（sp）。官方未公开 setTextSize API（1.14 起 BadgeDrawable 不再暴露
     * TextDrawable 接口），只能反射 internal.TextDrawableHelper 的画笔设置字号；
     * 挂载时 updateBadgeCoordinates 会按新字号重算角标尺寸。反射失败时回退默认字号，仅日志提示。
     */
    private static void applyTextSizeReflectively(@NonNull BadgeDrawable badge,
                                                  @NonNull Context context, float textSizeSp) {
        try {
            Field field = BadgeDrawable.class.getDeclaredField(FIELD_TEXT_DRAWABLE_HELPER);
            field.setAccessible(true);
            Object helper = field.get(badge);
            Method getTextPaint = helper.getClass().getMethod("getTextPaint");
            TextPaint paint = (TextPaint) getTextPaint.invoke(helper);
            paint.setTextSize(DensityUtil.sp2px(context, textSizeSp));
            // 标记文字尺寸脏，后续测量/坐标计算会按新字号重算
            Method setTextSizeDirty = helper.getClass().getMethod("setTextSizeDirty", boolean.class);
            setTextSizeDirty.invoke(helper, true);
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "badge textSize not applied, fallback to default size", e);
        }
    }

    /** 数字与显隐联动：&gt;0 显示，&lt;=0 清空数字并隐藏。 */
    private static void applyNumber(@NonNull BadgeDrawable badge, int number) {
        if (number > 0) {
            badge.setNumber(number);
            badge.setVisible(true);
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }
}
