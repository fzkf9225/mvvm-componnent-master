package pers.fz.mvvm.helper;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * created by fz on 2025/6/4 9:28
 * describe:
 */
public class AuthManager {
    public interface AuthCallback {
        void onLoginSuccessCallback(@Nullable Bundle data);

        void onLoginFailCallback(int resultCode, @Nullable Bundle data);
    }

    private final ActivityResultLauncher<Intent> launcher;

    private AuthCallback authCallback;

    public AuthManager(ComponentActivity activity) {
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (authCallback == null) return;

                    if (result.getResultCode() == ComponentActivity.RESULT_OK) {
                        authCallback.onLoginSuccessCallback(result.getData() != null ?
                                result.getData().getExtras() : null);
                    } else {
                        authCallback.onLoginFailCallback(result.getResultCode(),
                                result.getData() != null ? result.getData().getExtras() : null);
                    }
                }
        );
    }

    public AuthManager(Fragment fragment) {
        this.launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (authCallback == null) return;

                    if (result.getResultCode() == ComponentActivity.RESULT_OK) {
                        authCallback.onLoginSuccessCallback(result.getData() != null ?
                                result.getData().getExtras() : null);
                    } else {
                        authCallback.onLoginFailCallback(result.getResultCode(),
                                result.getData() != null ? result.getData().getExtras() : null);
                    }
                }
        );
    }

    public ActivityResultLauncher<Intent> getLauncher() {
        return launcher;
    }

    public void setAuthCallback(AuthCallback authCallback) {
        this.authCallback = authCallback;
    }

    public void launchLogin(Intent intent, AuthCallback callback) {
        this.authCallback = callback;
        launcher.launch(intent);
    }

    public void unregister() {
        if (launcher != null) {
            launcher.unregister();
        }
    }
}

