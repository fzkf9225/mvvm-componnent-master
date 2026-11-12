package io.coderf.arklab.common.utils.download;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import io.coderf.arklab.common.base.BaseException;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * 下载/更新模块的存储权限辅助类。
 * <p>
 * Android 10（API 29）及以下写入外部存储需要 {@link Manifest.permission#WRITE_EXTERNAL_STORAGE}，
 * Android 11+ 使用应用专属目录（{@link io.coderf.arklab.core.utils.FileUtil#getDefaultDownloadDir}）无需此权限。
 * <p>
 * <b>自动重试机制</b>：调用 {@link #deferWithStoragePermission} 或 {@link #requestPermission} 时，
 * 若权限尚未授予会先弹出系统授权框；用户同意后 {@link #dispatchPermissionResult} 会执行挂起的下载任务，
 * 无需用户再次点击。继承 {@link io.coderf.arklab.common.base.BaseActivity} 的页面已自动转发权限结果。
 * <p>
 * 协程栈请用 {@link DownloadPermissionHelperKtx#awaitStoragePermission}。
 *
 * @author fz
 * @see DownloadManager
 * @see DownloadClient
 * @see UpdateManager
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/12
 */
public final class DownloadPermissionHelper {
    /** 普通文件下载权限请求码，对应 {@link DownloadManager} / {@link DownloadClient} */
    public static final int REQUEST_CODE_DOWNLOAD = 0x01;

    /** APK 更新权限请求码，对应 {@link io.coderf.arklab.common.utils.download.listener.ApkUpdateListener} 及自定义 Dialog */
    public static final int REQUEST_CODE_UPDATE = 0x02;

    private static final Object LOCK = new Object();

    /** Activity → (requestCode → 挂起动作)，WeakReference 避免泄漏 */
    private static final Map<Activity, Map<Integer, PendingAction>> PENDING_ACTIONS = new WeakHashMap<>();

    private DownloadPermissionHelper() {
    }

    /**
     * 是否已具备写入存储所需权限。
     * API 30+ 始终返回 {@code true}（使用应用专属目录）。
     */
    public static boolean hasStoragePermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 包装 Observable：无权限时挂起任务，授权后自动订阅 {@code supplier} 提供的下载流。
     */
    public static <T> Observable<T> deferWithStoragePermission(@NonNull Activity activity, int requestCode,
                                                               @NonNull Supplier<Observable<T>> supplier) {
        return Observable.defer(() -> {
            if (hasStoragePermission(activity)) {
                return supplier.get();
            }
            return Observable.create(emitter -> requestPermission(activity, requestCode,
                    () -> {
                        Disposable disposable = supplier.get()
                                .subscribe(emitter::onNext, emitter::onError, emitter::onComplete);
                        emitter.setDisposable(disposable);
                    },
                    emitter::onError));
        });
    }

    /**
     * 在 Activity 的 {@code onRequestPermissionsResult} 中调用，触发挂起的下载/更新任务。
     *
     * @return {@code true} 表示 requestCode 属于本模块并已处理
     */
    public static boolean dispatchPermissionResult(@NonNull Activity activity, int requestCode,
                                                   @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE_DOWNLOAD && requestCode != REQUEST_CODE_UPDATE) {
            return false;
        }
        PendingAction pending = pollPending(activity, requestCode);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pending != null && pending.onGranted != null) {
                pending.onGranted.run();
            }
        } else if (pending != null && pending.onDenied != null) {
            try {
                pending.onDenied.accept(
                        new BaseException(BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION));
            } catch (Throwable ignored) {
            }
        }
        return true;
    }

    /**
     * 申请存储权限并在授权后执行 {@code onGranted}。
     * 已有权限时同步执行 {@code onGranted}，不弹窗。
     *
     * @param onDenied 用户拒绝授权时的回调
     */
    public static void requestPermission(@NonNull Activity activity, int requestCode,
                                         @NonNull Runnable onGranted,
                                         @NonNull Consumer<Throwable> onDenied) {
        if (hasStoragePermission(activity)) {
            onGranted.run();
            return;
        }
        enqueuePending(activity, requestCode, new PendingAction(onGranted, onDenied));
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
    }

    /** 取消尚未完成的权限等待（协程取消时调用） */
    public static void clearPending(@NonNull Activity activity, int requestCode) {
        synchronized (LOCK) {
            Map<Integer, PendingAction> actions = PENDING_ACTIONS.get(activity);
            if (actions == null) {
                return;
            }
            actions.remove(requestCode);
            if (actions.isEmpty()) {
                PENDING_ACTIONS.remove(activity);
            }
        }
    }

    private static void enqueuePending(@NonNull Activity activity, int requestCode,
                                       @NonNull PendingAction action) {
        synchronized (LOCK) {
            PENDING_ACTIONS.computeIfAbsent(activity, ignored -> new HashMap<>())
                    .put(requestCode, action);
        }
    }

    @Nullable
    private static PendingAction pollPending(@NonNull Activity activity, int requestCode) {
        synchronized (LOCK) {
            Map<Integer, PendingAction> actions = PENDING_ACTIONS.get(activity);
            if (actions == null) {
                return null;
            }
            PendingAction pending = actions.remove(requestCode);
            if (actions.isEmpty()) {
                PENDING_ACTIONS.remove(activity);
            }
            return pending;
        }
    }

    private static final class PendingAction {
        final Runnable onGranted;
        final Consumer<Throwable> onDenied;

        PendingAction(@NonNull Runnable onGranted, @Nullable Consumer<Throwable> onDenied) {
            this.onGranted = onGranted;
            this.onDenied = onDenied;
        }
    }
}
