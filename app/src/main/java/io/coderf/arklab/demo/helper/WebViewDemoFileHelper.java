package io.coderf.arklab.demo.helper;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * WebView 混合能力 Demo：将 assets 中的示例文件复制到应用沙盒，供 {@code local://} 加载验证。
 */
public final class WebViewDemoFileHelper {

    private static final String DEMO_DIR = "webview";

    private WebViewDemoFileHelper() {
    }

    /**
     * 确保 {@code files/webview/} 下存在 sandbox_page.html 等演示文件。
     *
     * @return 沙盒演示目录
     */
    @NonNull
    public static File ensureDemoFiles(@NonNull Context context) throws IOException {
        File targetDir = new File(context.getFilesDir(), DEMO_DIR);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("无法创建演示目录: " + targetDir);
        }
        copyAssetIfNeeded(context.getAssets(), "webview/sandbox_page.html",
                new File(targetDir, "sandbox_page.html"));
        return targetDir;
    }

    private static void copyAssetIfNeeded(
            @NonNull AssetManager assets,
            @NonNull String assetPath,
            @NonNull File dest
    ) throws IOException {
        if (dest.exists() && dest.length() > 0) {
            return;
        }
        try (InputStream in = assets.open(assetPath);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
}
