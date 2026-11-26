package io.coderf.arklab.media.compress

import android.content.Context
import android.net.Uri
import io.coderf.arklab.media.utils.LogUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import java.util.regex.Pattern

/**
 * 实况 / Motion Photo 与图片压缩的兼容层（core-media 内自洽，不依赖 core-base）。
 *
 * 压缩重编码会剥掉 JPEG 尾部内嵌 MP4；本工具负责：
 * 1. 识别并抽出内嵌视频 / Apple sidecar
 * 2. 压缩静图后把视频追加回 JPEG，或把 sidecar 拷到同名旁路
 *
 * 与 core-base [io.coderf.arklab.common.utils.common.MotionPhotoResolver] 探测策略对齐（XMP MicroVideoOffset / Item:Length / 尾部 ftyp）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 14:04
 */
internal object MotionPhotoCompat {

    private const val TAG = "MotionPhotoCompat"
    private const val CACHE_DIR = "media_motion_photo_cache"

    private val MICRO_VIDEO_OFFSET: Pattern = Pattern.compile(
        "MicroVideoOffset(?:=|\"\\s*:)\\s*\"?(\\d+)",
        Pattern.CASE_INSENSITIVE,
    )
    private val ITEM_LENGTH: Pattern = Pattern.compile(
        "Item:Length(?:=|\"\\s*:)\\s*\"?(\\d+)",
        Pattern.CASE_INSENSITIVE,
    )
    private val MOTION_PHOTO_FLAG: Pattern = Pattern.compile(
        "MotionPhoto(?:=|\"\\s*:)\\s*\"?1\\b",
        Pattern.CASE_INSENSITIVE,
    )
    private val MICRO_VIDEO_FLAG: Pattern = Pattern.compile(
        "MicroVideo(?:=|\"\\s*:)\\s*\"?1\\b",
        Pattern.CASE_INSENSITIVE,
    )

    /**
     * 压缩前探测到的实况素材，供压缩后写回。
     *
     * @property embeddedVideoFile 从源图抽出的尾部 MP4（Google Motion Photo）
     * @property sidecarVideoFile 同目录配对视频（Apple Live Photo）
     */
    data class MotionKeep(
        val embeddedVideoFile: File? = null,
        val sidecarVideoFile: File? = null,
    ) {
        /** 内嵌视频必须落在 JPEG 容器，压缩时需强制 JPEG */
        val shouldForceJpeg: Boolean get() = embeddedVideoFile != null

        val hasMotion: Boolean
            get() = embeddedVideoFile != null || sidecarVideoFile != null
    }

    /**
     * 探测源 Uri 是否带实况视频，并抽出可复用的视频文件。
     *
     * @param context 应用 Context
     * @param sourceUri 源静图
     * @return 无可保留实况时 [MotionKeep] 字段均为 null
     */
    fun prepare(context: Context, sourceUri: Uri): MotionKeep {
        val pathHint = sourceUri.toString()
        val sidecar = findSidecarVideoFile(pathHint)
        val embedded = extractEmbeddedMotionToCache(context, sourceUri)
        if (sidecar == null && embedded == null) {
            return MotionKeep()
        }
        LogUtil.logger(
            TAG,
            "检测到实况：sidecar=${sidecar != null}, embedded=${embedded != null}, uri=$sourceUri",
        )
        return MotionKeep(embeddedVideoFile = embedded, sidecarVideoFile = sidecar)
    }

    /**
     * XMP 是否声明为 Motion/MicroVideo（不一定能抽出视频）。
     * 抽出失败时应跳过压缩，避免把实况压成普通静图。
     */
    fun looksLikeMotionPhoto(context: Context, sourceUri: Uri): Boolean {
        return try {
            val fileSize = queryLength(context, sourceUri)
            if (fileSize <= 0L) return false
            val xmp = readXmpNearHead(context, sourceUri, minOf(fileSize, 512L * 1024L).toInt())
                ?: return false
            MOTION_PHOTO_FLAG.matcher(xmp).find() ||
                MICRO_VIDEO_FLAG.matcher(xmp).find() ||
                MICRO_VIDEO_OFFSET.matcher(xmp).find()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 将实况视频写回压缩后的静图文件。
     *
     * - 内嵌：追加到 JPEG 文件末尾（[MotionPhotoResolver] 可用 ftyp 扫描还原）
     * - sidecar：复制为与静图同名的 .MOV/.MP4
     *
     * @param outputStill 已写好的静图文件
     * @param keep [prepare] 的结果
     */
    fun preserveAfterCompress(outputStill: File, keep: MotionKeep) {
        if (!keep.hasMotion) return
        keep.embeddedVideoFile?.let { video ->
            if (video.exists() && video.length() > 0) {
                appendFile(outputStill, video)
                LogUtil.logger(TAG, "已追加内嵌实况视频到 ${outputStill.name} (+${video.length()} bytes)")
            }
        }
        keep.sidecarVideoFile?.let { sidecar ->
            if (!sidecar.exists()) return@let
            val parent = outputStill.parentFile ?: return@let
            val base = outputStill.nameWithoutExtension
            val ext = sidecar.extension.ifEmpty { "MOV" }
            val dest = File(parent, "$base.$ext")
            if (dest.absolutePath != sidecar.absolutePath) {
                sidecar.copyTo(dest, overwrite = true)
                LogUtil.logger(TAG, "已复制 sidecar 实况视频到 ${dest.name}")
            }
        }
    }

    private fun findSidecarVideoFile(stillPath: String): File? {
        if (stillPath.startsWith("content://") ||
            stillPath.startsWith("http://") ||
            stillPath.startsWith("https://")
        ) {
            return null
        }
        var localPath = stillPath
        if (stillPath.startsWith("file://")) {
            localPath = Uri.parse(stillPath).path ?: return null
        }
        val stillFile = File(localPath)
        val parent = stillFile.parentFile ?: return null
        if (!parent.isDirectory) return null
        val name = stillFile.name
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val suffixes = arrayOf(".MOV", ".mov", ".MP4", ".mp4", ".M4V", ".m4v")
        for (suffix in suffixes) {
            val candidate = File(parent, base + suffix)
            if (candidate.isFile && candidate.length() > 0L) {
                return candidate
            }
        }
        return null
    }

    private fun extractEmbeddedMotionToCache(context: Context, sourceUri: Uri): File? {
        return try {
            val pathHint = sourceUri.toString()
            val cacheFile = buildCacheFile(context, pathHint)
            if (cacheFile.exists() && cacheFile.length() > 0L) {
                return cacheFile
            }
            val embedded = locateEmbeddedMotion(context, sourceUri) ?: return null
            if (embedded.length <= 0L || embedded.startOffset < 0L) return null
            val parent = cacheFile.parentFile
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                return null
            }
            openStillStream(context, sourceUri)?.use { input ->
                skipFully(input, embedded.startOffset)
                FileOutputStream(cacheFile).use { out ->
                    val buffer = ByteArray(8192)
                    var remaining = embedded.length
                    while (remaining > 0) {
                        val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                        if (read < 0) break
                        out.write(buffer, 0, read)
                        remaining -= read
                    }
                }
            } ?: return null
            if (!cacheFile.exists() || cacheFile.length() < 32L) {
                cacheFile.delete()
                return null
            }
            cacheFile
        } catch (e: Exception) {
            LogUtil.logger(TAG, "extractEmbeddedMotionToCache failed: $e")
            null
        }
    }

    private data class EmbeddedMotion(val startOffset: Long, val length: Long)

    private fun locateEmbeddedMotion(context: Context, sourceUri: Uri): EmbeddedMotion? {
        val fileSize = queryLength(context, sourceUri)
        if (fileSize <= 0L) return null
        val xmp = readXmpNearHead(context, sourceUri, minOf(fileSize, 512L * 1024L).toInt())
        if (!xmp.isNullOrEmpty()) {
            val micro = MICRO_VIDEO_OFFSET.matcher(xmp)
            if (micro.find()) {
                val offsetFromEnd = micro.group(1)?.toLongOrNull() ?: -1L
                if (offsetFromEnd > 0L && offsetFromEnd < fileSize) {
                    return EmbeddedMotion(fileSize - offsetFromEnd, offsetFromEnd)
                }
            }
            val flagged = MOTION_PHOTO_FLAG.matcher(xmp).find() || MICRO_VIDEO_FLAG.matcher(xmp).find()
            val lengthMatcher = ITEM_LENGTH.matcher(xmp)
            var videoLength = -1L
            while (lengthMatcher.find()) {
                val value = lengthMatcher.group(1)?.toLongOrNull() ?: continue
                if (value > 1024L && value < fileSize) {
                    videoLength = maxOf(videoLength, value)
                }
            }
            if (videoLength > 0L && (flagged || videoLength > fileSize / 10L)) {
                return EmbeddedMotion(fileSize - videoLength, videoLength)
            }
        }
        // 勿用「最后一个 ftyp」：视频 mdat 内常有伪匹配，会抽到无音轨残段
        val ftypOffset = findFirstValidFtypOffset(context, sourceUri, fileSize)
        if (ftypOffset >= 4L) {
            return EmbeddedMotion(ftypOffset - 4L, fileSize - (ftypOffset - 4L))
        }
        return null
    }

    /**
     * 从头扫描第一个合法 ISO BMFF `ftyp` box（前 4 字节 size 在 16–256）。
     * Motion Photo = JPEG + MP4 时，该位置即为内嵌视频起点（含 moov/音轨）。
     *
     * @return `ftyp` 四字符起始下标；未找到返回 -1
     */
    private fun findFirstValidFtypOffset(context: Context, sourceUri: Uri, fileSize: Long): Long {
        if (fileSize < 16L) return -1L
        val ftyp = byteArrayOf('f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte())
        val chunkSize = 1024 * 1024
        val overlap = 8
        val buffer = ByteArray(chunkSize + overlap)
        openStillStream(context, sourceUri)?.use { input ->
            var filePos = 0L
            var carry = 0
            while (filePos < fileSize) {
                val toRead = minOf(chunkSize, (fileSize - filePos).toInt())
                var filled = 0
                while (filled < toRead) {
                    val read = input.read(buffer, carry + filled, toRead - filled)
                    if (read < 0) break
                    filled += read
                }
                if (filled <= 0 && carry == 0) break
                val window = carry + filled
                // 从 carry 区域起扫，保证跨块的 size+ftyp 也能命中
                val scanStart = if (filePos == 0L) 4 else 0
                var i = scanStart
                while (i <= window - 4) {
                    if (buffer[i] == ftyp[0] && buffer[i + 1] == ftyp[1] &&
                        buffer[i + 2] == ftyp[2] && buffer[i + 3] == ftyp[3] &&
                        i >= 4
                    ) {
                        val size = ((buffer[i - 4].toInt() and 0xFF) shl 24) or
                            ((buffer[i - 3].toInt() and 0xFF) shl 16) or
                            ((buffer[i - 2].toInt() and 0xFF) shl 8) or
                            (buffer[i - 1].toInt() and 0xFF)
                        // 标准 ftyp box 很小；排除 mdat 内偶然字节序列
                        if (size in 16..256) {
                            return filePos - carry + i
                        }
                    }
                    i++
                }
                // 保留末尾 overlap 供下一块拼接
                if (window >= overlap) {
                    System.arraycopy(buffer, window - overlap, buffer, 0, overlap)
                    carry = overlap
                } else {
                    carry = 0
                }
                filePos += filled
            }
        }
        return -1L
    }

    private fun readXmpNearHead(context: Context, sourceUri: Uri, maxBytes: Int): String? {
        openStillStream(context, sourceUri)?.use { input ->
            val buffer = ByteArray(maxBytes)
            var total = 0
            while (total < maxBytes) {
                val read = input.read(buffer, total, maxBytes - total)
                if (read < 0) break
                total += read
            }
            if (total <= 0) return null
            val ascii = String(buffer, 0, total, StandardCharsets.ISO_8859_1)
            var xmpStart = ascii.indexOf("http://ns.adobe.com/xap/1.0/")
            if (xmpStart < 0) {
                xmpStart = ascii.indexOf("<x:xmpmeta")
            }
            if (xmpStart < 0) return null
            val xmpEnd = ascii.indexOf("</x:xmpmeta>", xmpStart)
            return if (xmpEnd > xmpStart) {
                ascii.substring(xmpStart, xmpEnd + "</x:xmpmeta>".length)
            } else {
                ascii.substring(xmpStart, minOf(ascii.length, xmpStart + 64 * 1024))
            }
        }
        return null
    }

    private fun openStillStream(context: Context, sourceUri: Uri): InputStream? {
        val path = sourceUri.toString()
        return when {
            path.startsWith("content://") || path.startsWith("android.resource://") ->
                context.contentResolver.openInputStream(sourceUri)
            path.startsWith("file://") -> {
                val local = sourceUri.path ?: return null
                FileInputStream(File(local))
            }
            path.startsWith("/") -> FileInputStream(File(path))
            else -> {
                try {
                    context.contentResolver.openInputStream(sourceUri)
                } catch (_: Exception) {
                    val asFile = File(path)
                    if (asFile.isFile) FileInputStream(asFile) else null
                }
            }
        }
    }

    private fun queryLength(context: Context, sourceUri: Uri): Long {
        val path = sourceUri.toString()
        if (path.startsWith("content://")) {
            context.contentResolver.openAssetFileDescriptor(sourceUri, "r")?.use { afd ->
                return afd.length
            }
            return -1L
        }
        var local = path
        if (path.startsWith("file://")) {
            local = sourceUri.path ?: return -1L
        }
        val file = File(local)
        return if (file.isFile) file.length() else -1L
    }

    private fun skipFully(input: InputStream, count: Long) {
        var remaining = count
        while (remaining > 0) {
            val skipped = input.skip(remaining)
            if (skipped > 0) {
                remaining -= skipped
                continue
            }
            if (input.read() < 0) break
            remaining--
        }
    }

    private fun buildCacheFile(context: Context, stillPath: String): File {
        val dir = File(context.cacheDir, CACHE_DIR)
        val key = sha1Hex(stillPath)
        return File(dir, "$key.mp4")
    }

    private fun sha1Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val hash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        val sb = StringBuilder(hash.size * 2)
        for (b in hash) {
            sb.append(String.format(Locale.US, "%02x", b))
        }
        return sb.toString()
    }

    private fun appendFile(target: File, appendix: File) {
        FileOutputStream(target, /* append = */ true).use { out ->
            FileInputStream(appendix).use { input ->
                input.copyTo(out)
            }
        }
    }
}
