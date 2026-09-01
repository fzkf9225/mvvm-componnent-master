package io.coderf.arklab.common.listener;

/**
 * created fz on 2024/10/22 19:56
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public interface TypeListener {
    /**
     * 取消按钮事件
     */
    void cancel();

    /**
     * 确认按钮事件
     */
    void confirm();
}
