package io.coderf.arklab.speak

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import io.coderf.arklab.log.ArkLog
import io.coderf.arklab.speak.callback.OnSpeakHoldUiListener
import io.coderf.arklab.speak.callback.OnSpeakListener
import io.coderf.arklab.speak.file.DefaultVoiceFileStore
import io.coderf.arklab.speak.file.VoiceFileStore
import io.coderf.arklab.speak.model.SpeakFinishAction
import io.coderf.arklab.speak.model.SpeakHitZone
import io.coderf.arklab.speak.model.VoiceRecordResult
import io.coderf.arklab.speak.permission.SpeakPermissionConfig
import io.coderf.arklab.speak.permission.SpeakPermissionDialog
import io.coderf.arklab.speak.record.VoiceRecorder
import io.coderf.arklab.speak.style.SpeakUiStyle
import io.coderf.arklab.speak.widget.SpeakRecordOverlay
import java.io.File

/**
 * 按住说话能力绑定器：把「权限 → 录音 → 上滑热区 → 松手回调」挂到**任意** [View]。
 *
 * 推荐接入方式（底栏图标、自定义按钮等）：
 * ```
 * val speak = SpeakHelper.create(context)
 * speak.setOnSpeakListener { action, result -> ... }
 * speak.attach(aiIconView)
 * // onDestroy：speak.detach()
 * ```
 *
 * XML 便捷控件 [io.coderf.arklab.speak.widget.SpeakButton] 内部同样委托本类。
 *
 * @param context 用于初始化默认样式 / 文件存储；[attach] 后以目标 View 的 context 为准
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 17:50
 */
class SpeakHelper(
    context: Context
) {
    /** Application Context，录音与权限检查避免持有 Activity */
    private val appContext = context.applicationContext
    private val log = ArkLog.channel(VoiceRecorder.MODULE_ID, TAG)

    /** 弹窗 / Toast 用；[attach] 后更新为绑定 View 的 context */
    private var hostContext: Context = context
    /** 当前绑定的手势目标；null 表示未 attach */
    private var target: View? = null

    private var style: SpeakUiStyle = SpeakUiStyle.defaults(context)
    private var permissionConfig: SpeakPermissionConfig = SpeakPermissionConfig.defaults(context)
    private var fileStore: VoiceFileStore = DefaultVoiceFileStore(context)
    private var recorder: VoiceRecorder? = null
    /** 录音面板内容 View */
    private var overlay: SpeakRecordOverlay? = null
    /** 全屏半透明 Dialog，承载 [overlay] */
    private var recordDialog: Dialog? = null

    /** 写入 [VoiceRecordResult.userId] / 文件分目录 */
    private var userId: String? = null
    /** 本条录音业务 id */
    private var voiceId: String? = null
    private var outputFile: File? = null
    /** 手指当前所在热区（Edit / Send / Cancel） */
    private var currentZone: SpeakHitZone = SpeakHitZone.Edit
    /** 正在收尾（防 UP/最大时长重复 finish） */
    private var finishing = false
    /** 是否已成功进入录音（权限未过时为 false） */
    private var recordingActive = false
    /** 权限说明弹窗展示中，避免 DOWN 重复弹出 */
    private var permissionDialogShowing = false

    private var listener: OnSpeakListener? = null
    private var holdUiListener: OnSpeakHoldUiListener? = null
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    /** ActivityResultRegistry 注册 key，与 unregister 对应 */
    private var permissionKey: String? = null

    /**
     * 设置面板视觉与文案。
     * @param uiStyle 若录音中途更新，会同步到当前 [overlay]
     */
    fun setSpeakUiStyle(uiStyle: SpeakUiStyle): SpeakHelper {
        style = uiStyle
        overlay?.applyStyle(uiStyle)
        return this
    }

    /**
     * 麦克风权限前置说明配置。
     * @param config [SpeakPermissionConfig.showRationaleDialog] 为 false 时直接申请系统权限
     */
    fun setPermissionConfig(config: SpeakPermissionConfig): SpeakHelper {
        permissionConfig = config
        return this
    }

    /**
     * 自定义语音文件落盘实现；默认 [DefaultVoiceFileStore]。
     * @param store 创建 / 删除录音文件
     */
    fun setVoiceFileStore(store: VoiceFileStore): SpeakHelper {
        fileStore = store
        return this
    }

    /**
     * 业务用户 id，用于分目录与结果回传。
     * @param userId 可空；空则落到默认 `All` 目录
     */
    fun setUserId(userId: String?): SpeakHelper {
        this.userId = userId
        return this
    }

    /**
     * 松手结果回调（Send / Edit / Cancel）。
     * @param listener 传 null 清除
     */
    fun setOnSpeakListener(listener: OnSpeakListener?): SpeakHelper {
        this.listener = listener
        return this
    }

    /**
     * Kotlin 便捷重载。
     * @param block 第一个参数为动作，第二个为录音结果（Cancel 时为 null）
     */
    fun setOnSpeakListener(block: (SpeakFinishAction, VoiceRecordResult?) -> Unit): SpeakHelper {
        listener = OnSpeakListener { action, result -> block(action, result) }
        return this
    }

    /**
     * 按住态 UI 反馈（如按钮文案）；挂到 ImageView 可忽略。
     * @param listener 传 null 清除
     */
    fun setOnHoldUiListener(listener: OnSpeakHoldUiListener?): SpeakHelper {
        holdUiListener = listener
        return this
    }

    /** @return 是否已 [attach] 到某个 View */
    fun isAttached(): Boolean = target != null

    /**
     * 将按住说话手势绑定到 [view]；重复调用会先 [detach] 再绑定。
     * 权限申请依赖 [ComponentActivity]（从 view.context 向上查找）。
     *
     * @param view 任意可触摸 View（底栏图标、Button、ImageView 等）
     */
    @SuppressLint("ClickableViewAccessibility")
    fun attach(view: View): SpeakHelper {
        if (target === view) {
            registerPermissionLauncherIfNeeded()
            return this
        }
        detach()
        target = view
        hostContext = view.context
        registerPermissionLauncherIfNeeded()
        view.isClickable = true
        view.setOnTouchListener { v, event -> handleTouch(v, event) }
        view.addOnAttachStateChangeListener(attachStateListener)
        return this
    }

    /** 解除绑定，并清理录音会话与权限 Launcher。 */
    fun detach() {
        val view = target
        if (view != null) {
            view.setOnTouchListener(null)
            view.removeOnAttachStateChangeListener(attachStateListener)
        }
        target = null
        unregisterPermissionLauncher()
        cleanupSession(deleteFile = true)
        notifyHoldUi(false)
    }

    /** 目标 View 进出窗口时同步注册 / 清理权限与会话 */
    private val attachStateListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            registerPermissionLauncherIfNeeded()
        }

        override fun onViewDetachedFromWindow(v: View) {
            unregisterPermissionLauncher()
            cleanupSession(deleteFile = true)
            notifyHoldUi(false)
        }
    }

    /**
     * 处理按住 / 滑动 / 松手。
     * @return 始终消费事件，避免与单击导航冲突
     */
    private fun handleTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (permissionDialogShowing) return true
                view.parent?.requestDisallowInterceptTouchEvent(true)
                if (!hasMicPermission()) {
                    notifyHoldUi(false)
                    beginPermissionFlow()
                    return true
                }
                notifyHoldUi(true)
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
                view.parent?.requestDisallowInterceptTouchEvent(false)
                notifyHoldUi(false)
                if (!recordingActive) {
                    return true
                }
                finishByZone(currentZone)
            }
        }
        return true
    }

    /** @param holding true=按住录音中；false=松手或未开始 */
    private fun notifyHoldUi(holding: Boolean) {
        holdUiListener?.onHoldStateChanged(holding)
    }

    /** 向 [ComponentActivity] 注册麦克风权限 Launcher（幂等）。 */
    private fun registerPermissionLauncherIfNeeded() {
        if (permissionLauncher != null) return
        val activity = findComponentActivity(hostContext) ?: return
        val key = "speak_mic_${System.identityHashCode(this)}"
        permissionKey = key
        permissionLauncher = activity.activityResultRegistry.register(
            key,
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                toast(hostContext.getString(R.string.speak_permission_granted_retry))
            } else {
                toast(hostContext.getString(R.string.speak_permission_denied))
            }
        }
    }

    private fun unregisterPermissionLauncher() {
        permissionLauncher?.unregister()
        permissionLauncher = null
        permissionKey = null
    }

    private fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 无权限时的引导：可选说明弹窗 → 系统授权。
     * 授权成功后需用户再次按住（不自动开录，避免误触）。
     */
    private fun beginPermissionFlow() {
        if (permissionDialogShowing) return
        registerPermissionLauncherIfNeeded()
        if (!permissionConfig.showRationaleDialog) {
            launchSystemPermission()
            return
        }
        permissionDialogShowing = true
        SpeakPermissionDialog.show(
            context = hostContext,
            config = permissionConfig,
            onPositive = {
                permissionDialogShowing = false
                launchSystemPermission()
            },
            onNegative = {
                permissionDialogShowing = false
                toast(hostContext.getString(R.string.speak_permission_denied))
            }
        )
    }

    private fun launchSystemPermission() {
        val launcher = permissionLauncher
        if (launcher == null) {
            toast(hostContext.getString(R.string.speak_permission_denied))
            return
        }
        launcher.launch(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * 创建文件、启动 [VoiceRecorder]、展示面板。
     * @param downEvent ACTION_DOWN，用于面板热区按下原点
     */
    private fun startRecordSession(downEvent: MotionEvent) {
        finishing = false
        recordingActive = true
        currentZone = if (style.editEnabled) SpeakHitZone.Edit else SpeakHitZone.Send
        voiceId = DefaultVoiceFileStore.newVoiceId()
        outputFile = fileStore.createRecordingFile(userId)
        val file = outputFile ?: return
        val voiceRecorder = VoiceRecorder(appContext)
        recorder = voiceRecorder
        voiceRecorder.onAmplitude = { amp -> overlay?.setAmplitude(amp) }
        voiceRecorder.onMaxDuration = {
            currentZone = SpeakHitZone.Send
            finishByZone(SpeakHitZone.Send)
        }
        if (!voiceRecorder.start(file)) {
            toast(style.startFailed)
            cleanupSession(deleteFile = true)
            notifyHoldUi(false)
            return
        }
        showOverlay(downEvent)
    }

    /**
     * 弹出全屏录音 Overlay。
     * @param downEvent 用于换算面板本地按下坐标
     */
    private fun showOverlay(downEvent: MotionEvent) {
        dismissOverlay(immediate = true)
        val dialog = Dialog(hostContext, android.R.style.Theme_Translucent_NoTitleBar)
        val content = SpeakRecordOverlay(hostContext).also {
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

    /**
     * 按热区收尾。
     * @param zone Edit 在未开启编辑时会降级为 Send
     */
    private fun finishByZone(zone: SpeakHitZone) {
        if (finishing) return
        finishing = true
        recordingActive = false
        when (zone) {
            SpeakHitZone.Cancel -> cancelRecord()
            SpeakHitZone.Send -> completeRecord(SpeakFinishAction.Send)
            SpeakHitZone.Edit -> {
                if (style.editEnabled) {
                    completeRecord(SpeakFinishAction.Edit)
                } else {
                    completeRecord(SpeakFinishAction.Send)
                }
            }
        }
    }

    /** 撤回：删文件并回调 [SpeakFinishAction.Cancel]。 */
    private fun cancelRecord() {
        recorder?.cancel()
        recorder = null
        outputFile = null
        dismissOverlay {
            listener?.onSpeakFinished(SpeakFinishAction.Cancel, null)
            finishing = false
        }
    }

    /**
     * 停止录音并校验时长；成功则回调 [action] + [VoiceRecordResult]。
     * @param action Send 或 Edit
     */
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

    /**
     * 关闭录音面板。
     * @param immediate true 立即 dismiss；false 先播退出动画
     * @param end dismiss 完成后回调（用于再通知 [listener]）
     */
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

    /**
     * 中断当前录音会话。
     * @param deleteFile 是否删除未完成的落盘文件
     */
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

    /**
     * 从 Context 包装链解析 [ComponentActivity]（注册权限用）。
     * @return 非 ComponentActivity 的普通 Activity 返回 null
     */
    private fun findComponentActivity(context: Context): ComponentActivity? {
        var ctx: Context? = context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) return ctx
            if (ctx is Activity && ctx !is ComponentActivity) return null
            ctx = ctx.baseContext
        }
        return null
    }

    private fun toast(msg: String) {
        Toast.makeText(hostContext, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "SpeakHelper"

        /**
         * 工厂方法（Java 友好）。
         * @param context 见主构造说明
         */
        @JvmStatic
        fun create(context: Context): SpeakHelper = SpeakHelper(context)
    }
}
