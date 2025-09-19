package pers.fz.mvvm.utils.permission;

import android.content.Context;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * created by fz on 2025/6/4 9:09
 * describe:
 */
public class PermissionManager {
    private final ActivityResultLauncher<String[]> launcher;
    private Consumer<Map<String, Boolean>> onGrantedCallback;
    private Consumer<Map<String, Boolean>> onDeniedCallback = new Consumer<>() {
        @Override
        public void accept(Map<String, Boolean> stringBooleanMap) {
            Toast.makeText(mContext, "拒绝权限可能会导致应用软件运行异常", Toast.LENGTH_SHORT).show();
        }
    };

    private final Context mContext;

    public PermissionManager(ComponentActivity activity) {
        this.mContext = activity;
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult
        );
    }

    public PermissionManager(Fragment fragment) {
        this.mContext = fragment.requireContext();
        this.launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult
        );
    }

    public boolean lacksPermissions(String... permission) {
        return PermissionsChecker.getInstance().lacksPermissions(mContext, permission);
    }

    public boolean lacksPermissions(List<String> permission) {
        return PermissionsChecker.getInstance().lacksPermissions(mContext, permission);
    }

    public void request(List<String> permissions) {
        launcher.launch(permissions.toArray(new String[0]));
    }

    public void request(String... permissions) {
        launcher.launch(permissions);
    }

    public void setOnGrantedCallback(Consumer<Map<String, Boolean>> callback) {
        this.onGrantedCallback = callback;
    }

    public void setOnDeniedCallback(Consumer<Map<String, Boolean>> callback) {
        this.onDeniedCallback = callback;
    }

    private void handlePermissionResult(Map<String, Boolean> result) {
        if (result.values().stream().allMatch(Boolean::booleanValue)) {
            if (onGrantedCallback != null) {
                onGrantedCallback.accept(result);
            }
        } else {
            if (onDeniedCallback != null) {
                onDeniedCallback.accept(result);
            }
        }
    }

    public void unregister() {
        if (launcher != null) {
            launcher.unregister();
        }
    }
}

