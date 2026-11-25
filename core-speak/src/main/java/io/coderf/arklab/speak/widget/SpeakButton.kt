package io.coderf.arklab.speak.widget

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import io.coderf.arklab.log.ArkLog
import io.coderf.arklab.speak.R
import io.coderf.arklab.speak.file.DefaultVoiceFileStore
import io.coderf.arklab.speak.file.VoiceFileStore
import io.coderf.arklab.speak.model.SpeakFinishAction
import io.coderf.arklab.speak.model.SpeakHitZone
import io.coderf.arklab.speak.model.VoiceRecordResult
import io.coderf.arklab.speak.permission.SpeakPermissionConfig
import io.coderf.arklab.speak.permission.SpeakPermissionDialog
import io.coderf.arklab.speak.record.VoiceRecorder
import io.coderf.arklab.speak.style.SpeakUiStyle
import java.io.File

/**
 * 仿微信输入法「按住说话」按钮（宿主接入入口）。
 *
 * 典型用法：`setSpeakUiStyle` / `setPermissionConfig` / `setOnSpeakListener`，
 * 松手按热区回调 [SpeakFinishAction]（Send / Cancel / Edit）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
class SpeakButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyle) {

    private val log = ArkLog.channel(VoiceRecorder.MODULE_ID, TAG)

    private var style: SpeakUiStyle = SpeakUiStyle.defaults(context)
    private var permissionConfig: SpeakPermissionConfig = SpeakPermissionConfig.defaults(context)
    private var fileStore: VoiceFileStore = DefaultVoiceFileStore(context)
    private var recorder: VoiceRecorder? = null
    private var overlay: SpeakRecordOverlay? = null
    private var recordDialog: Dialog? = null

    private var userId: String? = null
    private var voiceId: String? = null
    private var outputFile: File? = null
    private var currentZone: SpeakHitZone = SpeakHitZone.Edit
    private var finishing = false
    private var recordingActive = false
    /** 权限说明弹窗展示中，避免重复弹出 */
    private var permissionDialogShowing = false

    private var listener: OnSpeakListener? = null
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var permissionKey: String? = null

    init {
        if (text.isNullOrEmpty()) {
            setText(R.string.speak_hold_to_talk)
        }
        attrs?.let { parseAttrs(it) }
    }

    private fun parseAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SpeakButton)
        try {
            val builder = SpeakUiStyle.Builder(context)
            if (a.hasValue(R.styleable.SpeakButton_speakWaveformColor)) {
                builder.waveformColor = a.getColor(
                    R.styleable.SpeakButton_speakWaveformColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakAccentColor)) {
                builder.accentColor = a.getColor(
                    R.styleable.SpeakButton_speakAccentColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakHintTextColor)) {
                builder.hintTextColor = a.getColor(
                    R.styleable.SpeakButton_speakHintTextColor,
                    SpeakUiStyle.DEFAULT_HINT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakInactiveTextColor)) {
                builder.inactiveTextColor = a.getColor(
                    R.styleable.SpeakButton_speakInactiveTextColor,
                    SpeakUiStyle.DEFAULT_INACTIVE
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakGlowColor)) {
                builder.glowColor = a.getColor(
                    R.styleable.SpeakButton_speakGlowColor,
                    SpeakUiStyle.DEFAULT_GLOW
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakHintTextSize)) {
                builder.hintTextSizePx = a.getDimension(
                    R.styleable.SpeakButton_speakHintTextSize,
                    builder.hintTextSizePx
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakActionTextSize)) {
                val size = a.getDimension(
                    R.styleable.SpeakButton_speakActionTextSize,
                    builder.editTextSizePx
                )
                builder.editTextSizePx = size
                builder.cancelTextSizePx = size
                builder.sendTextSizePx = size
            }
            if (a.hasValue(R.styleable.SpeakButton_speakCancelTextColor)) {
                builder.cancelTextColor = a.getColor(
                    R.styleable.SpeakButton_speakCancelTextColor,
                    SpeakUiStyle.DEFAULT_CANCEL
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakSendTextColor)) {
                builder.sendTextColor = a.getColor(
                    R.styleable.SpeakButton_speakSendTextColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakEditTextColor)) {
                builder.editTextColor = a.getColor(
                    R.styleable.SpeakButton_speakEditTextColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakEditArcHeight)) {
                builder.editArcHeightPx = a.getDimension(
                    R.styleable.SpeakButton_speakEditArcHeight,
                    builder.editArcHeightPx
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakEditEnabled)) {
                builder.editEnabled = a.getBoolean(
                    R.styleable.SpeakButton_speakEditEnabled,
                    true
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakShowEditArc)) {
                builder.showEditArc = a.getBoolean(
                    R.styleable.SpeakButton_speakShowEditArc,
                    true
                )
            }
            style = builder.build()
        } finally {
            a.recycle()
        }
    }

    fun setSpeakUiStyle(uiStyle: SpeakUiStyle) {
        style = uiStyle
        overlay?.applyStyle(uiStyle)
    }

    fun setPermissionConfig(config: SpeakPermissionConfig) {
        permissionConfig = config
    }

    fun setVoiceFileStore(store: VoiceFileStore) {
        fileStore = store
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    fun setOnSpeakListener(listener: OnSpeakListener?) {
        this.listener = listener
    }

    fun setOnSpeakListener(block: (SpeakFinishAction, VoiceRecordResult?) -> Unit) {
        listener = OnSpeakListener { action, result -> block(action, result) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerPermissionLauncherIfNeeded()
    }

    override fun onDetachedFromWindow() {
        unregisterPermissionLauncher()
        cleanupSession(deleteFile = true)
        super.onDetachedFromWindow()
    }

    private fun registerPermissionLauncherIfNeeded() {
        if (permissionLauncher != null) return
        val activity = findComponentActivity() ?: return
        val key = "speak_mic_${System.identityHashCode(this)}"
        permissionKey = key
        permissionLauncher = activity.activityResultRegistry.register(
            key,
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                toast(context.getString(R.string.speak_permission_granted_retry))
            } else {
                toast(context.getString(R.string.speak_permission_denied))
            }
        }
    }

    private fun unregisterPermissionLauncher() {
        permissionLauncher?.unregister()
        permissionLauncher = null
        permissionKey = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (permissionDialogShowing) return true
                if (!hasMicPermission()) {
                    text = context.getString(R.string.speak_hold_to_talk)
                    beginPermissionFlow()
                    return true
                }
                text = context.getString(R.string.speak_release_to_send)
                startRecordSession(event)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!recordingActive) return true
                val o = overlay ?: return true
                val locOverlay = IntArray(2)
                o.getLocationOnScreen(locOverlay)
                val localX = event.rawX - locOverlay[0]
                val localY = event.rawY - locOverlay[1]
                val zone = o.resolveZone(localX, localY)
                if (zone != currentZone) {
                    currentZone = zone
                    o.setZone(zone, animate = true)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                text = context.getString(R.string.speak_hold_to_talk)
                if (!recordingActive) {
                    return true
                }
                finishByZone(currentZone)
            }
        }
        return true
    }

    private fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun beginPermissionFlow() {
        if (permissionDialogShowing) return
        registerPermissionLauncherIfNeeded()
        if (!permissionConfig.showRationaleDialog) {
            launchSystemPermission()
            return
        }
        permissionDialogShowing = true
        SpeakPermissionDialog.show(
            context = context,
            config = permissionConfig,
            onPositive = {
                permissionDialogShowing = false
                launchSystemPermission()
            },
            onNegative = {
                permissionDialogShowing = false
                toast(context.getString(R.string.speak_permission_denied))
            }
        )
    }

    private fun launchSystemPermission() {
        val launcher = permissionLauncher
        if (launcher == null) {
            toast(context.getString(R.string.speak_permission_denied))
            return
        }
        launcher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecordSession(downEvent: MotionEvent) {
        finishing = false
        recordingActive = true
        currentZone = SpeakHitZone.Edit
        voiceId = DefaultVoiceFileStore.newVoiceId()
        outputFile = fileStore.createRecordingFile(userId)
        val file = outputFile ?: return
        val voiceRecorder = VoiceRecorder(context.applicationContext)
        recorder = voiceRecorder
        voiceRecorder.onAmplitude = { amp -> overlay?.setAmplitude(amp) }
        voiceRecorder.onMaxDuration = {
            currentZone = SpeakHitZone.Send
            finishByZone(SpeakHitZone.Send)
        }
        if (!voiceRecorder.start(file)) {
            toast(style.startFailed)
            cleanupSession(deleteFile = true)
            return
        }
        showOverlay(downEvent)
    }

    private fun showOverlay(downEvent: MotionEvent) {
        dismissOverlay(immediate = true)
        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        val content = SpeakRecordOverlay(context).also {
            it.applyStyle(style)
            overlay = it
        }
        dialog.setContentView(content)
        dialog.setCancelable(false)
        dialog.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.12f)
        }
        recordDialog = dialog
        dialog.show()
        content.post {
            val loc = IntArray(2)
            content.getLocationOnScreen(loc)
            content.onTouchDown(downEvent.rawX - loc[0], downEvent.rawY - loc[1])
        }
        content.showEnterAnimation()
    }

    private fun finishByZone(zone: SpeakHitZone) {
        if (finishing) return
        finishing = true
        recordingActive = false
        when (zone) {
            SpeakHitZone.Cancel -> cancelRecord()
            SpeakHitZone.Send -> completeRecord(SpeakFinishAction.Send)
            SpeakHitZone.Edit -> completeRecord(SpeakFinishAction.Edit)
        }
    }

    private fun cancelRecord() {
        recorder?.cancel()
        recorder = null
        outputFile = null
        dismissOverlay {
            listener?.onSpeakFinished(SpeakFinishAction.Cancel, null)
            finishing = false
        }
    }

    private fun completeRecord(action: SpeakFinishAction) {
        val id = voiceId
        val file = outputFile
        val duration = recorder?.stop() ?: -1L
        recorder = null
        if (id == null || file == null || !file.exists() || duration < 0) {
            fileStore.delete(file?.absolutePath)
            toast(style.saveFailed)
            dismissOverlay {
                listener?.onSpeakFinished(SpeakFinishAction.Cancel, null)
                finishing = false
            }
            return
        }
        if (duration < VoiceRecorder.MIN_DURATION_MS) {
            fileStore.delete(file.absolutePath)
            toast(style.tooShort)
            dismissOverlay {
                listener?.onSpeakFinished(SpeakFinishAction.Cancel, null)
                finishing = false
            }
            return
        }
        val result = VoiceRecordResult(
            id = id,
            filePath = file.absolutePath,
            durationMs = duration,
            sizeBytes = file.length(),
            userId = userId
        )
        log.loggerD(TAG, "record done action=$action path=${result.filePath} ms=${result.durationMs}")
        dismissOverlay {
            listener?.onSpeakFinished(action, result)
            finishing = false
        }
        outputFile = null
        voiceId = null
    }

    private fun dismissOverlay(immediate: Boolean = false, end: (() -> Unit)? = null) {
        val dialog = recordDialog
        val o = overlay
        recordDialog = null
        overlay = null
        if (dialog == null) {
            end?.invoke()
            return
        }
        if (immediate || o == null) {
            try {
                dialog.dismiss()
            } catch (_: Exception) {
            }
            end?.invoke()
        } else {
            o.hideWithAnimation {
                try {
                    dialog.dismiss()
                } catch (_: Exception) {
                }
                end?.invoke()
            }
        }
    }

    private fun cleanupSession(deleteFile: Boolean) {
        recordingActive = false
        recorder?.release()
        recorder = null
        if (deleteFile) {
            fileStore.delete(outputFile?.absolutePath)
        }
        outputFile = null
        voiceId = null
        dismissOverlay(immediate = true)
        finishing = false
    }

    private fun findComponentActivity(): ComponentActivity? {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) return ctx
            if (ctx is Activity && ctx !is ComponentActivity) return null
            ctx = ctx.baseContext
        }
        return null
    }

    private fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun interface OnSpeakListener {
        fun onSpeakFinished(action: SpeakFinishAction, result: VoiceRecordResult?)
    }

    companion object {
        private const val TAG = "SpeakButton"
    }
}
