package io.coderf.arklab.wscomponent;

/**
 * Callback 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public interface Callback<T> {
    void onEvent(String code, String msg, T t);
}
