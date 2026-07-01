package io.coderf.arklab.demo.bean;

import java.util.UUID;

/**
 * 绑定在 ViewModelComponent，生命周期与 ViewModel 相同。
 */
public class HiltViewModelScopedToken {

    private final String token = UUID.randomUUID().toString().substring(0, 8);

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "HiltViewModelScopedToken{token='" + token + "', identity=" + System.identityHashCode(this) + '}';
    }
}
