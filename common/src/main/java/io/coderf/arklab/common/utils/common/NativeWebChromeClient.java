package io.coderf.arklab.common.utils.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.helper.CordovaDialogsHelper;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.utils.permission.PermissionManager;
import io.coderf.arklab.common.utils.permission.WebViewPermissionHelper;

/**
 * 纯原生 {@link WebChromeClient} 实现，不依赖 commonmedia / googlegps。
 * <p>
 * 能力：系统文件选择器（图/视频/任意文件）、拍照、录像、Geolocation 与 Web 相机/麦克风权限。
 * </p>
 */
public class NativeWebChromeClient extends SystemWebChromeClient {

    private static final String TAG = "NativeWebChromeClient";

    protected final ComponentActivity activity;
    protected final PermissionManager permissionManager;

    @Nullable
    protected ValueCallback<Uri[]> pendingFileCallback;

    private final ActivityResultLauncher<Intent> documentLauncher;

    @Nullable
    private Uri cameraOutputUri;
    @Nullable
    private Uri videoOutputUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher;
    private final ActivityResultLauncher<Uri> takeVideoLauncher;

    @Nullable
    private GeolocationPermissions.Callback pendingGeoCallback;
    @Nullable
    private String pendingGeoOrigin;

    @Nullable
    private PermissionRequest pendingPermissionRequest;

    @Nullable
    private FileChooserParams pendingFileChooserParams;

    public NativeWebChromeClient(
            @NonNull ComponentActivity activity,
            @NonNull CordovaDialogsHelper dialogsHelper,
            @NonNull ProgressBar progressBar,
            @Nullable TextView tvBarTitle
    ) {
        super(activity, dialogsHelper, progressBar, tvBarTitle);
        this.activity = activity;
        this.permissionManager = new PermissionManager(activity);

        documentLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> deliverFileChooserResult(result.getResultCode(), result.getData())
        );

        takePictureLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> deliverCaptureResult(Boolean.TRUE.equals(success), cameraOutputUri)
        );

        takeVideoLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.CaptureVideo(),
                success -> deliverCaptureResult(Boolean.TRUE.equals(success), videoOutputUri)
        );
    }

    @Override
    public boolean onShowFileChooser(
            @NonNull WebView webView,
            @NonNull ValueCallback<Uri[]> filePathCallback,
            @NonNull FileChooserParams fileChooserParams
    ) {
        cancelPendingFileChooser();
        pendingFileCallback = filePathCallback;
        pendingFileChooserParams = fileChooserParams;

        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (fileChooserParams.isCaptureEnabled()) {
            if (acceptsVideo(acceptTypes) && !acceptsImage(acceptTypes)) {
                return launchVideoCapture();
            }
            if (acceptsImage(acceptTypes)) {
                return launchCameraCapture();
            }
        }

        return launchSystemFileChooserWithPermission(fileChooserParams);
    }

    public void cancelPendingFileChooser() {
        if (pendingFileCallback != null) {
            pendingFileCallback.onReceiveValue(null);
            pendingFileCallback = null;
        }
        cameraOutputUri = null;
        videoOutputUri = null;
        pendingFileChooserParams = null;
    }

    protected boolean launchSystemFileChooserWithPermission(@NonNull FileChooserParams fileChooserParams) {
        if (WebViewPermissionHelper.hasReadStorageForFileChooser(activity)) {
            return launchSystemFileChooser(fileChooserParams);
        }
        WebViewPermissionHelper.request(
                activity,
                permissionManager,
                WebViewPermissionHelper.readStorageForFileChooser(),
                PermissionManager.GrantMode.ALL_GRANTED,
                () -> {
                    if (!WebViewPermissionHelper.hasReadStorageForFileChooser(activity)) {
                        cancelPendingFileChooser();
                        Toast.makeText(activity, "需要相册/存储权限才能选择文件", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FileChooserParams params = pendingFileChooserParams != null
                            ? pendingFileChooserParams
                            : fileChooserParams;
                    launchSystemFileChooser(params);
                },
                this::cancelPendingFileChooser
        );
        return true;
    }

    protected boolean launchSystemFileChooser(@NonNull FileChooserParams fileChooserParams) {
        try {
            boolean multiple = fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;
            Intent intent = fileChooserParams.createIntent();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple);
            String[] acceptTypes = fileChooserParams.getAcceptTypes();
            if (acceptTypes != null && acceptTypes.length > 0) {
                if (acceptTypes.length == 1 && !TextUtils.isEmpty(acceptTypes[0])) {
                    intent.setType(acceptTypes[0]);
                } else {
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes);
                }
            } else {
                intent.setType("*/*");
            }
            documentLauncher.launch(intent);
            return true;
        } catch (Exception e) {
            LogUtil.logger(TAG, "launchSystemFileChooser error: " + e.getMessage());
            cancelPendingFileChooser();
            Toast.makeText(activity, "调用文件管理器失败", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    protected boolean launchCameraCapture() {
        WebViewPermissionHelper.request(
                activity,
                permissionManager,
                WebViewPermissionHelper.cameraPermissions(false),
                PermissionManager.GrantMode.ALL_GRANTED,
                this::launchCameraCaptureInternal,
                this::cancelPendingFileChooser
        );
        return true;
    }

    protected boolean launchVideoCapture() {
        WebViewPermissionHelper.request(
                activity,
                permissionManager,
                WebViewPermissionHelper.cameraPermissions(true),
                PermissionManager.GrantMode.ALL_GRANTED,
                this::launchVideoCaptureInternal,
                this::cancelPendingFileChooser
        );
        return true;
    }

    private boolean launchCameraCaptureInternal() {
        try {
            File photoFile = File.createTempFile("webview_capture_", ".jpg", activity.getCacheDir());
            cameraOutputUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".FileProvider",
                    photoFile
            );
            takePictureLauncher.launch(cameraOutputUri);
            return true;
        } catch (IOException e) {
            LogUtil.logger(TAG, "camera temp file error: " + e.getMessage());
            cancelPendingFileChooser();
            return false;
        }
    }

    private boolean launchVideoCaptureInternal() {
        try {
            File videoFile = File.createTempFile("webview_video_", ".mp4", activity.getCacheDir());
            videoOutputUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".FileProvider",
                    videoFile
            );
            takeVideoLauncher.launch(videoOutputUri);
            return true;
        } catch (IOException e) {
            LogUtil.logger(TAG, "video temp file error: " + e.getMessage());
            cancelPendingFileChooser();
            return false;
        }
    }

    private void deliverCaptureResult(boolean success, @Nullable Uri outputUri) {
        if (pendingFileCallback == null) {
            return;
        }
        if (success && outputUri != null) {
            pendingFileCallback.onReceiveValue(new Uri[]{outputUri});
        } else {
            pendingFileCallback.onReceiveValue(null);
        }
        pendingFileCallback = null;
        cameraOutputUri = null;
        videoOutputUri = null;
        pendingFileChooserParams = null;
    }

    private void deliverFileChooserResult(int resultCode, @Nullable Intent data) {
        if (pendingFileCallback == null) {
            return;
        }
        try {
            if (resultCode != Activity.RESULT_OK || data == null) {
                pendingFileCallback.onReceiveValue(null);
                return;
            }
            Uri[] result;
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                result = new Uri[count];
                for (int i = 0; i < count; i++) {
                    result[i] = data.getClipData().getItemAt(i).getUri();
                }
            } else if (data.getData() != null) {
                result = FileChooserParams.parseResult(resultCode, data);
            } else {
                result = null;
            }
            pendingFileCallback.onReceiveValue(result);
        } catch (Exception e) {
            LogUtil.logger(TAG, "deliverFileChooserResult error: " + e.getMessage());
            pendingFileCallback.onReceiveValue(null);
        } finally {
            pendingFileCallback = null;
            pendingFileChooserParams = null;
        }
    }

    protected static boolean acceptsImage(@Nullable String[] acceptTypes) {
        if (acceptTypes == null || acceptTypes.length == 0) {
            return false;
        }
        for (String type : acceptTypes) {
            if (!TextUtils.isEmpty(type) && !"*/*".equals(type) && type.contains("image")) {
                return true;
            }
        }
        return false;
    }

    /** accept 为通配 MIME（{@literal *\/ *}）或为空时走通用文件选择，而非相册弹窗。 */
    protected static boolean isGenericFileAccept(@Nullable String[] acceptTypes) {
        if (acceptTypes == null || acceptTypes.length == 0) {
            return true;
        }
        for (String type : acceptTypes) {
            if (TextUtils.isEmpty(type) || "*/*".equals(type)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean acceptsImageOnly(@Nullable String[] acceptTypes) {
        return acceptsImage(acceptTypes) && !acceptsVideo(acceptTypes);
    }

    protected static boolean acceptsVideo(@Nullable String[] acceptTypes) {
        if (acceptTypes == null || acceptTypes.length == 0) {
            return false;
        }
        for (String type : acceptTypes) {
            if (!TextUtils.isEmpty(type) && type.startsWith("video")) {
                return true;
            }
        }
        return false;
    }

    protected static boolean acceptsAnyFile(@Nullable String[] acceptTypes) {
        if (isGenericFileAccept(acceptTypes)) {
            return true;
        }
        for (String type : acceptTypes) {
            if (type.contains("application") || type.contains("text") || type.contains("pdf")) {
                return true;
            }
        }
        return !acceptsImage(acceptTypes) && !acceptsVideo(acceptTypes);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(
            @NonNull String origin,
            @NonNull GeolocationPermissions.Callback callback
    ) {
        pendingGeoOrigin = origin;
        pendingGeoCallback = callback;
        if (WebViewPermissionHelper.hasLocationPermission(activity)) {
            callback.invoke(origin, true, false);
            clearPendingGeo();
            return;
        }
        WebViewPermissionHelper.request(
                activity,
                permissionManager,
                WebViewPermissionHelper.locationPermissions(),
                PermissionManager.GrantMode.ANY_GRANTED,
                () -> {
                    if (pendingGeoCallback != null && pendingGeoOrigin != null) {
                        boolean granted = WebViewPermissionHelper.hasLocationPermission(activity);
                        pendingGeoCallback.invoke(pendingGeoOrigin, granted, false);
                    }
                    clearPendingGeo();
                },
                () -> {
                    if (pendingGeoCallback != null && pendingGeoOrigin != null) {
                        pendingGeoCallback.invoke(pendingGeoOrigin, false, false);
                    }
                    clearPendingGeo();
                }
        );
    }

    private void clearPendingGeo() {
        pendingGeoCallback = null;
        pendingGeoOrigin = null;
    }

    @Override
    public void onPermissionRequest(@NonNull PermissionRequest request) {
        String[] androidPerms = mapWebKitResourcesToAndroid(request.getResources());
        if (androidPerms.length == 0) {
            request.grant(request.getResources());
            return;
        }
        if (WebViewPermissionHelper.isGranted(
                activity, androidPerms, PermissionManager.GrantMode.ALL_GRANTED)) {
            request.grant(request.getResources());
            return;
        }
        pendingPermissionRequest = request;
        WebViewPermissionHelper.request(
                activity,
                permissionManager,
                androidPerms,
                PermissionManager.GrantMode.ALL_GRANTED,
                () -> {
                    if (pendingPermissionRequest != null) {
                        if (WebViewPermissionHelper.isGranted(
                                activity, androidPerms, PermissionManager.GrantMode.ALL_GRANTED)) {
                            pendingPermissionRequest.grant(pendingPermissionRequest.getResources());
                        } else {
                            pendingPermissionRequest.deny();
                        }
                        pendingPermissionRequest = null;
                    }
                },
                () -> {
                    if (pendingPermissionRequest != null) {
                        pendingPermissionRequest.deny();
                        pendingPermissionRequest = null;
                    }
                }
        );
    }

    @NonNull
    private static String[] mapWebKitResourcesToAndroid(@NonNull String[] resources) {
        List<String> perms = new ArrayList<>();
        for (String res : resources) {
            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(res)) {
                perms.add(android.Manifest.permission.CAMERA);
                perms.add(android.Manifest.permission.RECORD_AUDIO);
            } else if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(res)) {
                perms.add(android.Manifest.permission.RECORD_AUDIO);
            }
        }
        return perms.toArray(new String[0]);
    }
}
