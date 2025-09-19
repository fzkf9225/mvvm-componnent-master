package pers.fz.mvvm.utils.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import pers.fz.mvvm.helper.CordovaDialogsHelper;
import pers.fz.mvvm.utils.log.LogUtil;

/**
 * Created by fz on 2024/2/1 14:59
 * describe :
 */
public class SystemWebChromeClient extends WebChromeClient {
    public final static String TAG = "SystemWebChromeClient";
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int MAX_PROGRESS = 100;
    private final CordovaDialogsHelper dialogsHelper;
    private final ProgressBar progressBar;
    private final Context mContext;
    private TextView tvBarTitle;
    /**
     * 文件服务
     */
    protected ActivityResultLauncher<Intent> fileLauncher;

    public SystemWebChromeClient(ComponentActivity activity, CordovaDialogsHelper dialogsHelper, ProgressBar progressBar) {
        this.mContext = activity;
        this.dialogsHelper = dialogsHelper;
        this.progressBar = progressBar;
        fileLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> chooseFileCallback(result.getResultCode(), result.getData()));
    }

    public SystemWebChromeClient(ComponentActivity activity, CordovaDialogsHelper dialogsHelper, ProgressBar progressBar, TextView tvBarTitle) {
        this.mContext = activity;
        this.dialogsHelper = dialogsHelper;
        this.progressBar = progressBar;
        this.tvBarTitle = tvBarTitle;
        fileLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> chooseFileCallback(result.getResultCode(), result.getData()));
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == MAX_PROGRESS) {
            //加载完网页进度条消失
            progressBar.setVisibility(View.GONE);
        } else {
            //开始加载网页时显示进度条
            progressBar.setVisibility(View.VISIBLE);
            //设置进度值
            progressBar.setProgress(newProgress);
        }
    }

    /**
     * Tell the client to display a javascript alert dialog.
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        dialogsHelper.showAlert(message, (success, value) -> {
            if (success) {
                result.confirm();
            } else {
                result.cancel();
            }
        });
        return true;
    }

    /**
     * Tell the client to display a confirm dialog to the user.
     */
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        dialogsHelper.showConfirm(message, (success, value) -> {
            if (success) {
                result.confirm();
            } else {
                result.cancel();
            }
        });
        return true;
    }
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        LogUtil.show(TAG,"SourceId："+consoleMessage.sourceId()+"，MessageLevel："+consoleMessage.messageLevel()+
                ",LineNumber："+consoleMessage.lineNumber()+",Message："+consoleMessage.message());
        return super.onConsoleMessage(consoleMessage);
    }
    /**
     * Tell the client to display a prompt dialog to the user.
     * If the client returns true, WebView will assume that the client will
     * handle the prompt dialog and call the appropriate JsPromptResult method.
     * <p>
     * Since we are hacking prompts for our own purposes, we should not be using them for
     * this purpose, perhaps we should hack console.log to do this instead!
     */
    @Override
    public boolean onJsPrompt(WebView view, String origin, String message, String defaultValue, final JsPromptResult result) {
        dialogsHelper.showPrompt(message, defaultValue, (success, value) -> {
            if (success) {
                result.confirm(value);
            } else {
                result.cancel();
            }
        });
        return true;
    }

    /**
     * Handle database quota exceeded notification.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
                                        long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
        long MAX_QUOTA = 100 * 1024 * 1024;
        quotaUpdater.updateQuota(MAX_QUOTA);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        try {
            mUploadCallbackAboveL = filePathCallback;
            Boolean selectMultiple = false;
            if (fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
                selectMultiple = true;
            }
            Intent intent = fileChooserParams.createIntent();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, selectMultiple);
            String[] acceptTypes = fileChooserParams.getAcceptTypes();
            if (acceptTypes.length > 1) {
                // Accept all, filter mime types by Intent.EXTRA_MIME_TYPES.
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes);
            }
            fileLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
            mUploadCallbackAboveL = null;
            Toast.makeText(mContext, "调用文件管理器失败", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        request.grant(request.getResources());
    }

    /**
     * Android API >= 21(Android 5.0) 版本的回调处理
     */
    public void chooseFileCallback(int resultCode, Intent data) {
        try {
            if (Activity.RESULT_OK != resultCode) {
                mUploadCallbackAboveL.onReceiveValue(null);
                return;
            }
            if (data == null) {
                mUploadCallbackAboveL.onReceiveValue(null);
                return;
            }
            Uri[] result = null;
            if (data.getClipData() != null) {
                // handle multiple-selected files
                final int numSelectedFiles = data.getClipData().getItemCount();
                result = new Uri[numSelectedFiles];
                for (int i = 0; i < numSelectedFiles; i++) {
                    result[i] = data.getClipData().getItemAt(i).getUri();
                }
            } else if (data.getData() != null) {
                // handle single-selected file
                result = FileChooserParams.parseResult(resultCode, data);
            }
            mUploadCallbackAboveL.onReceiveValue(result);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "获取选择结果异常", Toast.LENGTH_SHORT).show();
        }
        mUploadCallbackAboveL = null;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (tvBarTitle == null) {
            return;
        }
        boolean isStartHttp = !TextUtils.isEmpty(title) && (title.startsWith("http") || title.startsWith("HTTP"));
        if (isStartHttp) {
            return;
        }
        tvBarTitle.setText(title);
    }
}
