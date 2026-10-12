package io.coderf.arklab.core.autosize;

import android.app.Activity;

/**
 * 屏幕适配监听器，用于监听屏幕适配时的一些事件
 * <p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2018/10/30 16:29
 */
public interface OnAdaptListener {
    /**
     * 在屏幕适配前调用
     *
     * @param target   需要屏幕适配的对象 (可能是 {@link Activity} 或者 Fragment)
     * @param activity 当前 {@link Activity}
     */
    void onAdaptBefore(Object target, Activity activity);

    /**
     * 在屏幕适配后调用
     *
     * @param target   需要屏幕适配的对象 (可能是 {@link Activity} 或者 Fragment)
     * @param activity 当前 {@link Activity}
     */
    void onAdaptAfter(Object target, Activity activity);
}
