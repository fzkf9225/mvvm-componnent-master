package io.coderf.arklab.wscomponent;

public interface Callback<T> {
    void onEvent(String code, String msg, T t);
}
