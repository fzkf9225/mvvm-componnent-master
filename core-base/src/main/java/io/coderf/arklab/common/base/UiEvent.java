package io.coderf.arklab.common.base;

import androidx.annotation.Nullable;

/**
 * 只消费一次的界面事件。阻挡部分情况LiveData数据倒灌的问题
 * <p>
 * LiveData 会把最后一次值交给新观察者。Toast、登录跳转不能这样重放，
 * 所以观察方必须走 {@link #getContentIfNotHandled()}。同一事件只会被第一个观察者取走。
 *
 * @param <T> 事件内容
 * @author fz
 * @version 1.0
 * @since 2.0.0
 */
public final class UiEvent<T> {

    @Nullable
    private final T content;

    private boolean handled;

    public UiEvent(@Nullable T content) {
        this.content = content;
    }

    /**
     * 取出内容并标记已消费。之后的观察者，包括旋转后重新订阅的页面，得到 null。
     */
    @Nullable
    public synchronized T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
