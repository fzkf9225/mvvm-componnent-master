package io.coderf.arklab.ui.webview;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.helper.CordovaDialogsHelper;
import io.coderf.arklab.common.utils.common.NativeWebChromeClient;
import io.coderf.arklab.common.utils.permission.PermissionManager;
import io.coderf.arklab.common.utils.permission.WebViewPermissionHelper;
import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.bean.MediaBean;
import io.coderf.arklab.media.dialog.OpenImageDialog;
import io.coderf.arklab.media.dialog.OpenMediaDialog;
import io.coderf.arklab.media.dialog.OpenShootDialog;

/**
 * 增强版 WebChromeClient：文件选择走 commonmedia {@link MediaHelper} 已有能力。
 */
public class MediaWebChromeClient extends NativeWebChromeClient {

    private final MediaHelper mediaHelper;

    @Nullable
    private Runnable pendingMediaAction;

    public MediaWebChromeClient(
            @NonNull ComponentActivity activity,
            @NonNull CordovaDialogsHelper dialogsHelper,
            @NonNull ProgressBar progressBar,
            @Nullable TextView tvBarTitle,
            @NonNull MediaHelper mediaHelper
    ) {
        super(activity, dialogsHelper, progressBar, tvBarTitle);
        this.mediaHelper = mediaHelper;
        mediaHelper.setActionSheetDismissWithoutPicker(this::cancelPendingWebFileChooser);
        mediaHelper.getMutableLiveData().observe(activity, this::onMediaHelperResult);
    }

    @Override
    public boolean onShowFileChooser(
            @NonNull WebView webView,
            @NonNull ValueCallback<Uri[]> filePathCallback,
            @NonNull FileChooserParams fileChooserParams
    ) {
        cancelPendingFileChooser();
        pendingFileCallback = filePathCallback;

        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        boolean capture = fileChooserParams.isCaptureEnabled();
        boolean image = acceptsImage(acceptTypes);
        boolean video = acceptsVideo(acceptTypes);

        if (capture) {
            if (video && !image) {
                return runMediaActionWithPermission(
                        WebViewPermissionHelper.cameraPermissions(true),
                        mediaHelper::shoot
                );
            }
            return runMediaActionWithPermission(
                    WebViewPermissionHelper.cameraPermissions(false),
                    mediaHelper::camera
            );
        }

        // */* 或空 accept：系统文档选择器，避免误开相册弹窗
        if (isGenericFileAccept(acceptTypes)) {
            return launchSystemFileChooserWithPermission(fileChooserParams);
        }

        if (image && video) {
            return runMediaAction(() -> mediaHelper.openMediaDialog(
                    webView.getRootView(),
                    OpenMediaDialog.CAMERA_SHOOT_ALBUM
            ));
        }
        if (video && !image) {
            return runMediaAction(() -> mediaHelper.openShootDialog(
                    webView.getRootView(),
                    OpenShootDialog.CAMERA_ALBUM
            ));
        }
        if (acceptsImageOnly(acceptTypes)) {
            return runMediaAction(() -> mediaHelper.openImageDialog(
                    webView.getRootView(),
                    OpenImageDialog.CAMERA_ALBUM
            ));
        }
        return launchSystemFileChooserWithPermission(fileChooserParams);
    }

    private boolean runMediaAction(@NonNull Runnable action) {
        pendingMediaAction = action;
        action.run();
        return true;
    }

    private boolean runMediaActionWithPermission(
            @NonNull String[] permissions,
            @NonNull Runnable action
    ) {
        pendingMediaAction = action;
        WebViewPermissionHelper.request(
                activity,
                permissionManager,
                permissions,
                PermissionManager.GrantMode.ALL_GRANTED,
                action,
                () -> {
                    cancelPendingWebFileChooser();
                    Toast.makeText(activity, "需要相机/麦克风权限才能继续", Toast.LENGTH_SHORT).show();
                }
        );
        return true;
    }

    private void cancelPendingWebFileChooser() {
        pendingMediaAction = null;
        cancelPendingFileChooser();
    }

    private void onMediaHelperResult(@NonNull MediaBean mediaBean) {
        if (pendingFileCallback == null) {
            return;
        }
        pendingMediaAction = null;
        if (mediaBean.getMediaList() == null || mediaBean.getMediaList().isEmpty()) {
            pendingFileCallback.onReceiveValue(null);
            pendingFileCallback = null;
            return;
        }
        List<Uri> uris = new ArrayList<>();
        for (Uri item : mediaBean.getMediaList()) {
            if (item != null) {
                uris.add(item);
            }
        }
        if (uris.isEmpty()) {
            pendingFileCallback.onReceiveValue(null);
        } else {
            pendingFileCallback.onReceiveValue(uris.toArray(new Uri[0]));
        }
        pendingFileCallback = null;
    }
}
