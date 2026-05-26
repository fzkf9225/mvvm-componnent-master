package io.coderf.arklab.common.helper;

import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link WebViewAssetLoader.PathHandler} 实现：从指定根目录提供 html、pdf 等静态文件。
 * <p>用于沙盒 {@code files}、{@code external}、系统 {@code download} 等路径映射。</p>
 */
public class StoragePathHandler implements WebViewAssetLoader.PathHandler {

  private final File rootDir;

  /**
   * @param rootDir 允许访问的根目录（如 {@code context.getFilesDir()}）
   */
  public StoragePathHandler(@NonNull File rootDir) {
    this.rootDir = rootDir;
  }

  @Nullable
  @Override
  public WebResourceResponse handle(@NonNull String path) {
    File target = safeResolve(rootDir, path);
    if (target == null || !target.isFile() || !target.canRead()) {
      return null;
    }
    try {
      String mime = guessMimeType(target.getName());
      InputStream inputStream = new FileInputStream(target);
      return new WebResourceResponse(mime, "UTF-8", inputStream);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * 安全拼接路径，防止 {@code ../} 穿越到根目录外。
   */
  @Nullable
  private static File safeResolve(@NonNull File root, @NonNull String relativePath) {
    try {
      File rootCanonical = root.getCanonicalFile();
      File target = new File(rootCanonical, relativePath).getCanonicalFile();
      String rootPath = rootCanonical.getPath();
      String targetPath = target.getPath();
      if (!targetPath.startsWith(rootPath)) {
        return null;
      }
      return target;
    } catch (IOException e) {
      return null;
    }
  }

  @NonNull
  private static String guessMimeType(@NonNull String fileName) {
    String ext = MimeTypeMap.getFileExtensionFromUrl(fileName);
    if (ext != null) {
      String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
      if (mime != null) {
        return mime;
      }
    }
    if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
      return "text/html";
    }
    if (fileName.endsWith(".pdf")) {
      return "application/pdf";
    }
    return "application/octet-stream";
  }
}
