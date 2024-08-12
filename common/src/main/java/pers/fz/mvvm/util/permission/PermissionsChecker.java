package pers.fz.mvvm.util.permission;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

/**
 * Created by fz on 2017/6/28.
 * 检查权限的工具类
 */

public class PermissionsChecker {
    private PermissionsChecker() {
    }

    private static final class PermissionsCheckerHolder {
        private static final PermissionsChecker INSTANCE = new PermissionsChecker();
    }

    public static PermissionsChecker getInstance() {
        return PermissionsCheckerHolder.INSTANCE;
    }

    /**
     * 判断权限集合
     */
    public boolean lacksPermissions(Context mContext, String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(mContext, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     */
    private boolean lacksPermission(Context mContext, String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
