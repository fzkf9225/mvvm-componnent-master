package io.coderf.arklab.common.utils.download.core;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.core.utils.FileUtil;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import okhttp3.Headers;
import okhttp3.MediaType;
import okio.BufferedSource;

/**
 * 文件下载的 RxJava {@link ObservableOnSubscribe} 实现。
 * <p>
 * 职责：
 * <ol>
 *   <li>从 {@link DownloadInterceptor} 读取响应体，追加写入临时文件（支持断点续传）</li>
 *   <li>更新通知栏进度并回调 {@link DownloadListener}</li>
 *   <li>下载完成后重命名临时文件为目标文件名（自动去重命名）</li>
 * </ol>
 * 取消订阅（dispose）时会关闭通知并触发 {@link DownloadListener#onCancel()}。
 *
 * @see DownloadRetrofitFactory
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/7
 */
public class DownloadObservable implements ObservableOnSubscribe<File> {

    /**
     * RFC 5987：{@code filename*=charset'lang'urlencoded-name}，优先于普通 filename。
     */
    private static final Pattern CONTENT_DISPOSITION_FILENAME_STAR = Pattern.compile(
            "filename\\*\\s*=\\s*([^']*)'[^']*'([^;]+)", Pattern.CASE_INSENSITIVE);

    /**
     * 普通 {@code filename="..." } / {@code filename=...}（不改写原始大小写）。
     */
    private static final Pattern CONTENT_DISPOSITION_FILENAME = Pattern.compile(
            "filename\\s*=\\s*\"([^\"]*)\"|filename\\s*=\\s*([^;\\s]+)", Pattern.CASE_INSENSITIVE);

    /** 断点续传临时文件（{@code .download} 后缀） */
    private final File tempFile;
    /** 持有本次 HTTP 响应，供读取 body 与 headers */
    private final DownloadInterceptor interceptor;
    private final DownloadNotificationUtil downloadNotificationUtil;
    private final String saveBasePath;
    private final String saveFileName;
    private final String fileUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final DownloadListener downloadListener;
    /** 下载完成后是否展示默认的"文件已保存至..." Toast */
    private final boolean showToast;

    public DownloadObservable(DownloadInterceptor interceptor, String fileUrl, File tempFile, String saveBasePath, String saveFileName, DownloadListener downloadListener, boolean showToast) {
        this.tempFile = tempFile;
        this.interceptor = interceptor;
        this.saveBasePath = saveBasePath;
        this.saveFileName = saveFileName;
        this.fileUrl = fileUrl;
        this.downloadListener = downloadListener;
        this.showToast = showToast;
        downloadNotificationUtil = new DownloadNotificationUtil(Config.getInstance().getApplication());
        mainHandler.post(() -> {
            downloadNotificationUtil.showNotification(fileUrl.hashCode());
            if (downloadListener != null) {
                downloadListener.onStart();
            }
        });
    }

    @Override
    public void subscribe(ObservableEmitter<File> emitter) throws Exception {
        emitter.setCancellable(() -> mainHandler.post(() -> {
            downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
            if (downloadListener != null) {
                downloadListener.onCancel();
            }
        }));
        try (BufferedSource source = interceptor.getResponseBody().source()) {
            long totalByte = interceptor.getResponseBody().contentLength();
            long downloadByte = 0;
            if (!Objects.requireNonNull(tempFile.getParentFile()).exists()) {
                boolean mkdir = tempFile.getParentFile().mkdirs();
            }

            byte[] buffer = new byte[1024 * 4];
            RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rwd");
            long tempFileLen = tempFile.length();

            randomAccessFile.seek(tempFileLen);
            while (true) {
                int len = interceptor.getResponseBody().byteStream().read(buffer);
                if (len == -1) {
                    break;
                }

                randomAccessFile.write(buffer, 0, len);
                downloadByte += len;
                int progress = (int) ((downloadByte * 100) / totalByte);

                mainHandler.post(() -> {
                    downloadNotificationUtil.updateNotification(fileUrl.hashCode(),
                            progress, FileUtil.getFileName(fileUrl));
                    if (downloadListener != null) {
                        downloadListener.onProgress(progress);
                    }
                });
            }
            randomAccessFile.close();

            String fileName;
            if (TextUtils.isEmpty(saveFileName)) {
                // 优先级：Content-Disposition(filename*/filename) → URL 路径 → 时间戳+MIME 后缀
                fileName = FileUtil.autoRenameFileName(saveBasePath, resolveAutoFileName());
            } else {
                fileName = sanitizeFileName(saveFileName);
                if (TextUtils.isEmpty(fileName)) {
                    fileName = resolveAutoFileName();
                }
            }
            File newFile = new File(saveBasePath, fileName);
            // 重命名失败（如目标文件被占用）时仍以临时文件作为下载结果
            File resultFile = tempFile.renameTo(newFile) ? newFile : tempFile;
            mainHandler.post(() -> downloadNotificationUtil.cancelNotification(fileUrl.hashCode()));
            mainHandler.post(() -> {
                if (showToast) {
                    Toast.makeText(Config.getInstance().getApplication(),
                            "文件已保存至" + resultFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
                if (downloadListener != null) {
                    downloadListener.onFinish(resultFile);
                }
            });
            emitter.onNext(resultFile);
            emitter.onComplete();
        } catch (IOException e) {
            mainHandler.post(() -> {
                downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                if (downloadListener != null) {
                    downloadListener.onError(e);
                }
            });
            if (!emitter.isDisposed()) {
                emitter.onError(e);
            }
        }
    }

    /**
     * 自动推断保存文件名。
     * <ol>
     *   <li>{@code Content-Disposition} 的 {@code filename*} / {@code filename}</li>
     *   <li>URL 路径最后一段（解码后）</li>
     *   <li>无扩展名时尝试用 {@code Content-Type} 补后缀</li>
     *   <li>仍失败则 {@code 时间戳.bin/.xxx}</li>
     * </ol>
     */
    private String resolveAutoFileName() {
        MediaType mediaType = interceptor.getResponseBody() != null
                ? interceptor.getResponseBody().contentType() : null;

        String contentDisposition = findHeaderIgnoreCase(interceptor.getHeaders(), "Content-Disposition");
        String fromHeader = parseContentDispositionFileName(contentDisposition);
        if (!TextUtils.isEmpty(fromHeader)) {
            return ensureExtension(sanitizeFileName(fromHeader), mediaType);
        }

        String fromUrl = decodeUrlComponent(FileUtil.getFileNameByUrl(fileUrl));
        fromUrl = sanitizeFileName(fromUrl);
        if (!TextUtils.isEmpty(fromUrl)) {
            return ensureExtension(fromUrl, mediaType);
        }

        String ext = extensionFromMediaType(mediaType);
        return System.currentTimeMillis() + "." + (TextUtils.isEmpty(ext) ? "bin" : ext);
    }

    /**
     * 解析 Content-Disposition：优先 RFC 5987 {@code filename*}，再普通 {@code filename}。
     * 不对整段 header 做 toLowerCase，避免破坏真实文件名大小写与编码串。
     */
    static String parseContentDispositionFileName(String contentDispositionHeader) {
        if (TextUtils.isEmpty(contentDispositionHeader)) {
            return null;
        }
        Matcher star = CONTENT_DISPOSITION_FILENAME_STAR.matcher(contentDispositionHeader);
        if (star.find()) {
            String charsetName = star.group(1);
            String encoded = star.group(2);
            if (encoded != null) {
                encoded = trimQuotes(encoded.trim());
                String decoded = decodeRfc5987(encoded, charsetName);
                if (!TextUtils.isEmpty(decoded)) {
                    return decoded;
                }
            }
        }
        Matcher plain = CONTENT_DISPOSITION_FILENAME.matcher(contentDispositionHeader);
        if (plain.find()) {
            String quoted = plain.group(1);
            String bare = plain.group(2);
            String value = !TextUtils.isEmpty(quoted) ? quoted : bare;
            if (!TextUtils.isEmpty(value)) {
                return decodeUrlComponent(trimQuotes(value.trim()));
            }
        }
        return null;
    }

    /** 去掉路径分隔符与首尾空白，防止 {@code ../} 穿越 */
    static String sanitizeFileName(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String name = raw.trim();
        // 只保留最后一段，去掉任意目录成分
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replace("\0", "").trim();
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return null;
        }
        return name;
    }

    private static String ensureExtension(String fileName, MediaType mediaType) {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        if (fileName.contains(".")) {
            return fileName;
        }
        String ext = extensionFromMediaType(mediaType);
        if (TextUtils.isEmpty(ext)) {
            return fileName;
        }
        return fileName + "." + ext;
    }

    private static String extensionFromMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return null;
        }
        String mime = mediaType.type() + "/" + mediaType.subtype();
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime.toLowerCase(Locale.US));
        if (!TextUtils.isEmpty(ext)) {
            return ext;
        }
        // 常见 MIME 兜底（部分设备 MimeTypeMap 不全）
        switch (mime.toLowerCase(Locale.US)) {
            case "application/vnd.android.package-archive":
                return "apk";
            case "application/pdf":
                return "pdf";
            case "application/zip":
                return "zip";
            case "application/json":
                return "json";
            case "text/plain":
                return "txt";
            default:
                return null;
        }
    }

    private static String decodeRfc5987(String encoded, String charsetName) {
        Charset charset = StandardCharsets.UTF_8;
        if (!TextUtils.isEmpty(charsetName)) {
            try {
                charset = Charset.forName(charsetName.trim());
            } catch (Exception ignored) {
                charset = StandardCharsets.UTF_8;
            }
        }
        try {
            // filename* 使用百分号编码，按字节还原
            byte[] bytes = percentDecodeToBytes(encoded);
            return new String(bytes, charset).trim();
        } catch (Exception e) {
            return decodeUrlComponent(encoded);
        }
    }

    private static byte[] percentDecodeToBytes(String encoded) {
        String s = encoded.replace("+", " ");
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%' && i + 2 < s.length()) {
                int hi = Character.digit(s.charAt(i + 1), 16);
                int lo = Character.digit(s.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    out.write((hi << 4) + lo);
                    i += 2;
                    continue;
                }
            }
            out.write((byte) c);
        }
        return out.toByteArray();
    }

    private static String decodeUrlComponent(String value) {
        if (TextUtils.isEmpty(value)) {
            return value;
        }
        try {
            return Uri.decode(value);
        } catch (Exception e) {
            return value;
        }
    }

    private static String trimQuotes(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.length() >= 2) {
            if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                return v.substring(1, v.length() - 1);
            }
        }
        return v;
    }

    /** 忽略大小写查找响应头 */
    private static String findHeaderIgnoreCase(Headers headers, String headerName) {
        if (headers == null) {
            return null;
        }
        for (String name : headers.names()) {
            if (name.equalsIgnoreCase(headerName)) {
                return headers.get(name);
            }
        }
        return null;
    }
}


