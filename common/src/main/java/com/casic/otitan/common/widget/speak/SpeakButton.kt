package com.casic.otitan.common.widget.speak

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.casic.otitan.common.R
import com.casic.otitan.common.utils.common.DateUtil
import com.casic.otitan.common.utils.common.FileUtil
import com.casic.otitan.common.utils.log.LogUtil
import java.io.File
import kotlin.concurrent.Volatile
import kotlin.math.min
/**
 *
 * 功能说明：
 * 1. 长按录音，松开发送，上滑取消
 * 2. 录音时显示悬浮对话框，包含音量波形动画和操作提示
 * 3. 支持设置最短录音时间（1秒）和最长录音时间（60秒）
 * 4. 录音文件保存为AMR格式，按用户ID分目录存储
 * 5. 实时获取录音分贝值驱动动画，倒计时提醒
 *
 * 使用示例：
 * ```kotlin
 * // XML布局中
 * <com.casic.otitan.common.widget.speak.SpeakButton
 *     android:id="@+id/btnSpeak"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="按住说话" />
 *
 * // 代码中
 * btnSpeak.setUserId("user123") // 设置用户ID用于文件分类
 * btnSpeak.setOnFinishedRecordListener { bytes, duration ->
 *     // bytes: 录音文件字节数组
 *     // duration: 录音时长（秒）
 *     // 处理录音结果
 * }
 * ```
 * created by fz on 2024/11/5 16:40
 * describe：语音录制按钮控件
 * @param context 上下文
 * @param attrs 属性集
 * @param defStyle 默认样式
 */
class SpeakButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    androidx.appcompat.widget.AppCompatButton(context, attrs, defStyle) {
    companion object {
        const val TAG = "SpeakButton"
    }

    private var mFileName: String? = null

    private var finishedListener: OnFinishedRecordListener? = null

    /**
     * 最短录音时间
     */
    private val minIntervalTime = 1000

    /**
     * 最长录音时间
     */
    private val maxIntervalTime = 1000 * 60

    private var mStateTV: AppCompatTextView? = null

    @Volatile
    private var mRecorder: MediaRecorder? = null
    private var runningObtainDecibelThread = true
    private var mThread: ObtainDecibelThread? = null

    private var voiceView: VoiceView? = null

    private var downY = 0f

    fun setOnFinishedRecordListener(listener: OnFinishedRecordListener) {
        finishedListener = listener
    }

    private var startTime: Long = 0
    private var recordDialog: Dialog? = null

    @Volatile
    private var firstNotice = true
    private var moveY = 0

    private var userId: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        moveY = (event.y - downY).toInt()
        if (mStateTV != null && voiceView != null && moveY < 0 && moveY < -20) {
            mStateTV?.text = "松开手指,取消发送"
            voiceView?.setCancel(true)
        } else if (mStateTV != null && voiceView != null) {
            mStateTV?.text = "手指上滑,取消发送"
            voiceView?.setCancel(false)
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                //按下的时候，重新生成一个语音保存的地址，避免一直读写一个文件，可以引起错误
                downY = event.y
                text = "松开发送"
                initDialogAndStartRecord()
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                this.text = "按住说话"
                if (moveY < 0 && moveY < -20) {  //当手指向上滑，会cancel
                    cancelRecord()
                } else {
                    finishRecord()
                }
            }
        }
        return true
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    /**
     * 初始化录音对话框 并 开始录音
     */
    private fun initDialogAndStartRecord() {
        recordDialog = Dialog(context, R.style.like_toast_dialog_style)
        val view = inflate(context, R.layout.dialog_record, null)
        voiceView = view.findViewById(R.id.btn_wx_voice)
        mStateTV = view.findViewById(R.id.rc_audio_state_text)
        mStateTV?.visibility = VISIBLE
        mStateTV?.text = "手指上滑,取消发送"
        recordDialog?.setContentView(
            view, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        val window = recordDialog?.window
        if (window != null) {
            window.decorView.setPadding(0, 0, 0, 0)
            val layoutParams = window.attributes
            window.setBackgroundDrawableResource(android.R.color.transparent)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = layoutParams
        }

        if (startRecording()) {
            recordDialog?.show()
        }
    }

    /**
     * 放开手指，结束录音处理
     */
    private fun finishRecord() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post { this.finishRecord() }
            return
        }
        val intervalTime = System.currentTimeMillis() - startTime
        firstNotice = true
        val wavFileName = mFileName
        val file = File(wavFileName)
        stopRecording()
        if (!file.exists()) {
            //如果文件不存在，则返回
            //当我们到底最长时间，会在ObtainDecibelThread中，和onTouchEvent方法中，重复调用该方法
            //因此做一个检测
            Toast.makeText(context, "语音保存失败！", Toast.LENGTH_SHORT).show()
            return
        }
        if (intervalTime < minIntervalTime) {
            Toast.makeText(context, "说话时间太短", Toast.LENGTH_SHORT).show()
            mStateTV?.text = "说话时间太短"
            file.delete()
            return
        }

        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(wavFileName)
            mediaPlayer.prepare()
            mediaPlayer.duration
        } catch (e: Exception) {
            LogUtil.show(TAG, "mediaPlayer异常:$e")
        }

        if (finishedListener == null) {
            return
        }
        //不用转码成MP3的话直接返回
        val bytes = FileUtil.getFileToByte(file);
        finishedListener?.onFinishedRecord(bytes, mediaPlayer.duration / 1000)
    }

    /**
     * 取消录音对话框和停止录音
     */
    private fun cancelRecord() {
        stopRecording()
        try {
            val isDelete = mFileName?.let { File(it).delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun createdSubPath(context: Context, directory: String?, userId: String?): File {
        try {
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                val file = File(
                    context.getExternalFilesDir(directory),
                    userId ?: "All"
                )
                if (!file.exists()) {
                    file.mkdirs()
                }
                return file
            } else {
                return context.getExternalFilesDir(directory)!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        }
    }

    /**
     * 执行录音操作
     */
    //int num = 0 ;
    private fun startRecording(): Boolean {
        if (mRecorder != null) {
            mRecorder?.reset()
        } else {
            mRecorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
        }
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mFileName = createdSubPath(
            context,
            Environment.DIRECTORY_MUSIC,
            userId
        ).absolutePath + File.separator + "voice_" + DateUtil.getCurrentTime() + ".amr"
        LogUtil.show(TAG, "语音保存路径为：$mFileName")
        mRecorder?.setOutputFile(mFileName)
        try {
            mRecorder?.prepare()
            mRecorder?.start()
            startTime = System.currentTimeMillis()
        } catch (e: Exception) {
            LogUtil.show(TAG, "录音启动失败:$e")
            e.printStackTrace()
            mRecorder?.release()
            mRecorder = null
            toast("录音启动失败[" + e.message + "]")
            return false
        }
        runningObtainDecibelThread = true
        mThread = ObtainDecibelThread()
        mThread?.start()
        return true
    }

    private fun toast(content: String) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        runningObtainDecibelThread = false
        if (mThread != null) {
            mThread = null
        }

        if (mRecorder != null) {
            try {
                mRecorder?.stop() //停止时没有prepare，就会报stop failed
                mRecorder?.reset()
                mRecorder?.release()
                mRecorder = null
            } catch (pE: RuntimeException) {
                pE.printStackTrace()
            } finally {
            }
        }
        if (recordDialog != null) {
            recordDialog?.dismiss()
            recordDialog = null
        }
        if (voiceView != null) {
            voiceView?.quit()
            voiceView = null
        }
    }

    /**
     * 用来定时获取录音的声音大小，以驱动动画
     * 获取录音时间，提醒用户
     * 到达最大时间以后自动停止
     */
    private inner class ObtainDecibelThread : java.lang.Thread() {
        override fun run() {
            while (runningObtainDecibelThread) {
                if (mRecorder == null) {
                    break
                }
                // int x = recorder.getMaxAmplitude(); //振幅
                val maxAmplitude = mRecorder?.maxAmplitude
                var db = maxAmplitude?.div(35)

                //  Log.i("zzz", "分贝:" + db);
                db = min(200.0, db!!.toDouble()).toInt()

                val now = System.currentTimeMillis()
                val useTime = now - startTime
                if (useTime > maxIntervalTime) {
                    finishRecord()
                    return
                }
                //少于十秒则提醒
                val lessTime = (maxIntervalTime - useTime) / 1000
                if (lessTime < 10) {
                    voiceView?.setContent(lessTime.toString() + "秒后将结束录音")
//                    if (firstNotice) {
//                        firstNotice = false //第一次需要震动
//                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//                        vibrator.vibrate(500)
//                    }
                } else {
                    voiceView?.addVoiceSize(db)
                }

                try {
                    sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    interface OnFinishedRecordListener {
        fun onFinishedRecord(bytes: ByteArray?, time: Int)
    }
}
