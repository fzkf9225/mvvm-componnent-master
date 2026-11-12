package io.coderf.arklab.common.utils.download.core;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.coderf.arklab.core.utils.FileUtil;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

/**
 * 下载保存文件名解析（Rx / 协程写盘共用）。
 * <p>
 * 优先级：{@code Content-Disposition} → URL 路径 → 时间戳 + MIME 后缀。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
public final class DownloadFileNames {

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

    private DownloadFileNames() {
    }

    /**
     * 解析最终落盘文件名（含去重重命名）。
     *
     * @param saveFileName 业务指定名，可空
     */
    public static String resolveSaveFileName(String saveBasePath,
                                             String saveFileName,
                                             String fileUrl,
                                             Headers headers,
                                             ResponseBody body) {
        if (!TextUtils.isEmpty(saveFileName)) {
            String sanitized = sanitizeFileName(saveFileName);
            if (!TextUtils.isEmpty(sanitized)) {
                return sanitized;
            }
        }
        return FileUtil.autoRenameFileName(saveBasePath, resolveAutoFileName(fileUrl, headers, body));
    }

    /**
     * 自动推断保存文件名（不含目录去重）。
     */
    public static String resolveAutoFileName(String fileUrl, Headers headers, ResponseBody body) {
        MediaType mediaType = body != null ? body.contentType() : null;

        String contentDisposition = findHeaderIgnoreCase(headers, "Content-Disposition");
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
     */
    public static String parseContentDispositionFileName(String contentDispositionHeader) {
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
    public static String sanitizeFileName(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String name = raw.trim();
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
    public static String findHeaderIgnoreCase(Headers headers, String headerName) {
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
