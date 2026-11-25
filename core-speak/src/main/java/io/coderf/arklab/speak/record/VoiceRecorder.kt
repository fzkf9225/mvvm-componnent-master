package io.coderf.arklab.speak.record

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import io.coderf.arklab.log.ArkLog
import java.io.File
import kotlin.math.min

/**
 * MediaRecorder 录音与分贝采样封装（AMR_NB）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
class VoiceRecorder(
    private val context: Context
) {
    private val log = ArkLog.channel(MODULE_ID, TAG)

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L
    private var amplitudeThread: HandlerThread? = null
    private var amplitudeHandler: Handler? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var sampling = false

    /** 振幅回调（约 0~200），用于驱动 [io.coderf.arklab.speak.widget.WaveformView] */
    var onAmplitude: ((Int) -> Unit)? = null
    /** 达到最大时长时回调（录音已停止） */
    var onMaxDuration: (() -> Unit)? = null

    val isRecording: Boolean
        get() = recorder != null && sampling

    fun start(output: File, maxDurationMs: Long = MAX_DURATION_MS): Boolean {
        stopInternal(deleteFile = false)
        outputFile = output
        val parent = output.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.setOutputFile(output.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
            startTimeMs = System.currentTimeMillis()
            startSampling(maxDurationMs)
            true
        } catch (e: Exception) {
            log.e(TAG, "start failed: $e")
            try {
                mediaRecorder.release()
            } catch (_: Exception) {
            }
            recorder = null
            false
        }
    }

    /**
     * 停止录音并返回时长（毫秒）。失败返回 -1。
     */
    fun stop(): Long {
        val file = outputFile
        val elapsed = System.currentTimeMillis() - startTimeMs
        stopInternal(deleteFile = false)
        if (file == null || !file.exists() || file.length() <= 0L) {
            return -1L
        }
        val duration = probeDurationMs(file).takeIf { it > 0 } ?: elapsed
        return duration
    }

    fun cancel() {
        val file = outputFile
        stopInternal(deleteFile = true)
        if (file != null && file.exists()) {
            file.delete()
        }
    }

    fun release() {
        cancel()
        onAmplitude = null
        onMaxDuration = null
    }

    private fun startSampling(maxDurationMs: Long) {
        sampling = true
        val thread = HandlerThread("ark-speak-amp").also { it.start() }
        amplitudeThread = thread
        val handler = Handler(thread.looper)
        amplitudeHandler = handler
        handler.post(object : Runnable {
            override fun run() {
                if (!sampling) return
                val r = recorder
                if (r == null) return
                val now = System.currentTimeMillis()
                if (now - startTimeMs >= maxDurationMs) {
                    mainHandler.post { onMaxDuration?.invoke() }
                    return
                }
                val amp = try {
                    r.maxAmplitude
                } catch (_: Exception) {
                    0
                }
                val db = min(200, amp / 35)
                mainHandler.post { onAmplitude?.invoke(db) }
                handler.postDelayed(this, SAMPLE_INTERVAL_MS)
            }
        })
    }

    private fun stopInternal(deleteFile: Boolean) {
        sampling = false
        amplitudeHandler?.removeCallbacksAndMessages(null)
        amplitudeThread?.quitSafely()
        amplitudeHandler = null
        amplitudeThread = null
        val r = recorder
        recorder = null
        if (r != null) {
            try {
                r.stop()
            } catch (_: RuntimeException) {
            }
            try {
                r.reset()
                r.release()
            } catch (_: Exception) {
            }
        }
        if (deleteFile) {
            outputFile?.delete()
            outputFile = null
        }
    }

    private fun probeDurationMs(file: File): Long {
        val player = MediaPlayer()
        return try {
            player.setDataSource(file.absolutePath)
            player.prepare()
            player.duration.toLong()
        } catch (e: Exception) {
            log.e(TAG, "probe duration failed: $e")
            -1L
        } finally {
            try {
                player.release()
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        const val MODULE_ID = "speak"
        private const val TAG = "VoiceRecorder"
        const val MIN_DURATION_MS = 1_000L
        const val MAX_DURATION_MS = 60_000L
        private const val SAMPLE_INTERVAL_MS = 80L
    }
}
