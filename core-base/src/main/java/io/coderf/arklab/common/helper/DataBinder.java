package io.coderf.arklab.common.helper;

import android.os.Binder;

/**
 * Binder传参
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/12 10:17
 */
public class DataBinder<T> extends Binder {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

