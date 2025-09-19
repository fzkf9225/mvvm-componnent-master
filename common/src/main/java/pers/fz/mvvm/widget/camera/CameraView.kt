package pers.fz.mvvm.widget.camera;

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shuyu.gsyvideoplayer.GSYVideoManager
import pers.fz.mvvm.R
import pers.fz.mvvm.databinding.CameraViewBinding
import pers.fz.mvvm.listener.CameraResultListener
import pers.fz.mvvm.listener.CaptureListener
import pers.fz.mvvm.listener.TypeListener
import pers.fz.mvvm.utils.common.DateUtil
import pers.fz.mvvm.utils.common.FileUtil
import pers.fz.mvvm.utils.log.LogUtil
import java.io.File
import java.util.Objects
import java.util.concurrent.Executors

/**
 * Create by fz on 2024/10/22 17:30
 * describe:自定义拍照
 */
class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr),
    DefaultLifecycleObserver {
    private var cameraResultListener: CameraResultListener? = null

    /**
     * 返回按钮点击事件
     */
    private var leftClickListener: OnClickListener? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    private var recording: Recording? = null

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var cameraExecutor = Executors.newSingleThreadExecutor()

    private var binding: CameraViewBinding? = null

    private val handler = Handler(Looper.getMainLooper())

    private var owner: LifecycleOwner? = null

    /**
     * 底部按钮图片资源
     */
    private var iconSrc: Int

    /**
     * 左侧按钮图片资源
     */
    private var iconLeft: Int

    /**
     * 右侧按钮图片资源
     */
    private var iconRight: Int

    /**
     * 相机
     */
    private var camera: Camera? = null

    /**
     * 图片或视频路径
     */
    private var uri: Uri? = null

    /**
     * 图片或视频保存类型
     */
    private var mediaType: String? = null

    /**
     * 图片或视频保存路径
     */
    var imageOutPutPath: String? = null

    /**
     * 视频保存文件名
     */
    var videoOutPutPath: String? = null

    /**
     * 图片或视频保存文件名
     */
    var imageOutPutFileName: String? = null

    /**
     * 视频保存文件名
     */
    var videoOutPutFileName: String? = null

    /**
     * 图片或视频保存文件类型
     */
    var imageOutPutFileMineType: String? = null

    /**
     * 视频保存文件类型
     */
    var videoOutPutFileMineType: String? = null

    /**
     * 视频最大录制时长
     */
    private var maxDuration: Int = 30

    /**
     * 相机模式
     */
    var buttonFeatures: Int = Mode.BUTTON_STATE_BOTH

    init {
        if (attrs == null) {
            iconSrc = R.drawable.ic_camera
            iconLeft = 0
            iconRight = 0
            initView()
        } else {
            val a: TypedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CustomCameraView,
                defStyleAttr,
                0
            )
            iconSrc = a.getResourceId(R.styleable.CustomCameraView_iconSrc, R.drawable.ic_camera)
            iconLeft = a.getResourceId(R.styleable.CustomCameraView_iconLeft, 0)
            iconRight = a.getResourceId(R.styleable.CustomCameraView_iconRight, 0)
            a.recycle()
        }
        initView()
    }

    /**
     * 设置录制视频最大时长
     * @param maxDuration 录制视频最大时长,单位秒
     */
    fun setVideoMaxDuration(maxDuration: Int) {
        this.maxDuration = maxDuration
        binding?.captureLayout?.btnCapture?.setDuration(maxDuration * 1000)
    }

    /**
     * 初始化控件和相关设置
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        setWillNotDraw(false)
        binding = CameraViewBinding.inflate(LayoutInflater.from(context), this, true)
        layoutParams =
            Constraints.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        binding?.imageSwitch?.setImageResource(iconSrc)

        binding?.imageFlash?.setOnClickListener {
            switchFlash()
        }

        binding?.captureLayout?.setButtonFeatures(buttonFeatures)
        binding?.captureLayout?.setIconSrc(iconLeft, iconRight)
        //切换摄像头
        binding?.imageSwitch?.setOnClickListener { _: View? -> flipCamera() }
        binding?.captureLayout?.setCaptureListener(object : CaptureListener {
            override fun takePictures() {
                binding?.imageSwitch?.visibility = GONE
                binding?.imageFlash?.visibility = GONE
                this@CameraView.takePictures()
            }

            override fun recordShort(time: Long) {
                Toast.makeText(context, "录制时间过短!", Toast.LENGTH_LONG).show()
                resetState(true)
            }

            override fun recordStart() {
                binding?.imageSwitch?.visibility = GONE
                binding?.imageFlash?.visibility = GONE
                captureVideo()
            }

            override fun recordEnd(time: Long) {
//                machine.stopRecord(false, time);
                recording?.stop()
            }

            override fun recordZoom(zoom: Float) {
//                machine.zoom(zoom, CameraInterface.TYPE_RECORDER);
            }

            override fun recordError() {
            }
        })
        //拍照拍照成功后的点击事件，即保存图片还是抛弃当前图片，确认 取消
        binding?.captureLayout?.setTypeListener(object : TypeListener {
            override fun cancel() {
                resetState(true)
            }

            override fun confirm() {
                if (uri != null) {
                    binding?.photoView?.visibility = GONE
                    binding?.videoPlayer?.visibility = GONE
                    binding?.mCameraView?.visibility = VISIBLE
                    GSYVideoManager.releaseAllVideos()
                    cameraResultListener?.captureSuccess(uri, mediaType)
                    //                    scanPhotoAlbum(photoFile);
                    resetState(false)
                }
            }
        })
        //未拍照模式下的返回按钮点击事件
        binding?.captureLayout?.setLeftClickListener {
            leftClickListener?.onClick(it)
        }
        // 设置触摸监听
        binding?.flCamera?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val rawX = event.rawX.toInt()
                val rawY = event.rawY.toInt()
                focusCamera(rawX, rawY)
                true
            } else {
                false
            }
        }
    }


    /**
     * 录像
     */
    private fun captureVideo() {
        if (this.videoCapture == null) {
            return
        }
        val contentValues = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                videoOutPutFileName ?: ("VIDEO_" + DateUtil.getCurrentTime() + ".mp4")
            )
            put(MediaStore.MediaColumns.MIME_TYPE, videoOutPutFileMineType ?: "video/mp4")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                videoOutPutPath
                    ?: (Environment.DIRECTORY_DCIM + File.separator + FileUtil.getDefaultBasePath(
                        context
                    ) + File.separator + "video")
            )
        }
        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setDurationLimitMillis(this.maxDuration * 1000L)
            .setContentValues(contentValues)
            .build()
        // 开始录像
        recording = this.videoCapture?.output?.prepareRecording(context, mediaStoreOutputOptions)
            ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Finalize -> {
                        // 录制结束
                        if (recordEvent.hasError()) {
                            // 录制失败
                            LogUtil.show(
                                TAG,
                                "onError:${recordEvent.cause},${recordEvent.cause?.message}"
                            )
                            cameraResultListener?.onError(
                                recordEvent.error,
                                recordEvent.cause?.message ?: "录制失败",
                                recordEvent.cause
                            )
                        } else {
                            // 录制成功
                            handler.post {
                                binding?.mCameraView?.visibility = GONE
                                binding?.photoView?.visibility = GONE
                                binding?.videoPlayer?.visibility = VISIBLE
                                uri = recordEvent.outputResults.outputUri
                                mediaType = Result.VIDEO
                                binding?.videoPlayer?.setUp(uri.toString(), false, "")

                                //增加封面
                                val imageView = ImageView(context)
                                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                                Glide.with(context)
                                    .load(uri)
                                    .apply(
                                        RequestOptions().placeholder(pers.fz.mvvm.R.mipmap.ic_default_image)
                                            .error(pers.fz.mvvm.R.mipmap.ic_default_image)
                                    )
                                    .into(imageView)
                                binding?.videoPlayer?.thumbImageView = imageView
                                //增加title
                                binding?.videoPlayer?.titleTextView?.visibility = GONE
                                //设置返回键
                                binding?.videoPlayer?.backButton?.visibility = GONE
                                //设置全屏按钮不可见，则不可以修改全屏和竖屏
                                binding?.videoPlayer?.fullscreenButton?.visibility = GONE
                                //是否可以滑动调整
                                binding?.videoPlayer?.setIsTouchWiget(true)
                                //设置返回按键功能
                                binding?.videoPlayer?.startPlayLogic()
                                binding?.captureLayout?.startTypeBtnAnimator()
                            }
                        }
                    }
                }
            }
    }

    /**
     * 拍照
     */
    private fun takePictures() {
        val outputOptions = ImageCapture
            .OutputFileOptions
            .Builder(initTakePicPath())
            .build()
        imageCapture?.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    LogUtil.e(TAG, "Photo capture failed: ${exc.message}")
                    cameraResultListener?.onError(
                        exc.imageCaptureError,
                        Objects.requireNonNull<String>(exc.message),
                        exc.cause
                    )
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    uri = output.savedUri
                    mediaType = Result.IMAGE
                    //显示拍照获得的图片，并且隐藏相机
                    handler.post {
                        binding?.photoView?.let {
                            Glide.with(context)
                                .load(uri)
                                .into(it)
                        }
                        binding?.mCameraView?.visibility = GONE
                        binding?.videoPlayer?.visibility = GONE
                        binding?.photoView?.visibility = VISIBLE
                        binding?.captureLayout?.startTypeBtnAnimator()
                    }
                }
            })
    }

    /**
     * 切换摄像头
     */
    private fun flipCamera() {
        if (hasFrontCamera() && hasBackCamera()) {
            owner?.let {
                startCamera(it, true)
            }
        }
    }

    fun getBinding(): CameraViewBinding? {
        return binding
    }

    /**
     * 获取相机选择器
     */
    private fun getCameraSelector(isFlipCamera: Boolean): CameraSelector {
        return if (isFlipCamera) {
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        } else {
            if (hasBackCamera()) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
        }
    }

    /**
     * 初始化相机预览
     */
    private fun startCamera(owner: LifecycleOwner, isFlipCamera: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()
            cameraSelector = getCameraSelector(isFlipCamera)
            videoCapture = VideoCapture.withOutput(
                Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            )
            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    owner, cameraSelector,
                    Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(binding?.mCameraView?.surfaceProvider) },
                    imageCapture, videoCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 切换闪光灯
     */
    private fun switchFlash() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                //方法一
                when (imageCapture?.flashMode) {
                    ImageCapture.FLASH_MODE_AUTO -> {
                        binding?.imageFlash?.setImageResource(R.drawable.ic_flash_off)
                        imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                    }

                    ImageCapture.FLASH_MODE_ON -> {
                        binding?.imageFlash?.setImageResource(R.drawable.ic_flash_auto)
                        imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                    }

                    ImageCapture.FLASH_MODE_OFF -> {
                        binding?.imageFlash?.setImageResource(R.drawable.ic_flash_on)
                        imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                    }

                    else -> {
                        Toast.makeText(context, "当前设备不支持闪光灯", Toast.LENGTH_SHORT).show()
                    }
                }
                //方法二
//                if (it.cameraInfo.torchState.getValue() == 0) {
//                    it.cameraControl.enableTorch(true)
//                } else {
//                    it.cameraControl.enableTorch(false)
//                }
            } else {
                Toast.makeText(context, "当前设备不支持闪光灯", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 是否有后置摄像头
     */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /**
     * 是否有前置摄像头
     */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    /**
     * 初始化图片保存路径，当前保存在本地文件中：android---->media------->包名----->图片
     * @return file文件
     */
    private fun initTakePicPath(): File =
        if (imageOutPutPath.isNullOrEmpty())
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                imageOutPutFileName ?: ("IMAGE_" + DateUtil.getCurrentTime() + ".jpg")
            )
        else
            File(
                imageOutPutPath + File.separator + (imageOutPutFileName
                    ?: ("IMAGE_" + DateUtil.getCurrentTime() + ".jpg"))
            )

    /**
     * 设置点击事件，监听拍照成功错误等时间
     *
     * @param cameraResultListener 回调接口
     */
    fun setCameraResultListener(cameraResultListener: CameraResultListener?) {
        this.cameraResultListener = cameraResultListener
    }

    /**
     * 关闭相机界面按钮
     *
     * @param clickListener 底部拍照后的左边返回按钮监听时间
     */
    fun setLeftClickListener(clickListener: OnClickListener?) {
        this.leftClickListener = clickListener
    }

    /**
     * 重置状态，true：删除已拍照的图片，false：只是重置状态，保存当前图片
     */
    private fun resetState(isClear: Boolean) {
        if (uri != null && isClear) {
            deleteImage(uri)
            mediaType = null
        }
        binding?.photoView?.visibility = GONE
        binding?.videoPlayer?.visibility = GONE
        binding?.imageSwitch?.visibility = VISIBLE
        binding?.imageFlash?.visibility = VISIBLE
        binding?.mCameraView?.visibility = VISIBLE
        binding?.captureLayout?.resetCaptureLayout()
        if (binding?.videoPlayer?.isInPlayingState == true) {
            binding?.videoPlayer?.onVideoPause()
        }
        binding?.focusView?.visibility = GONE
    }

    private fun deleteImage(imageUri: Uri?) {
        try {
            if (imageUri?.scheme == "content") {
                context.contentResolver.delete(imageUri, null, null)
            } else if (imageUri?.scheme == "file") {
                // 文件Uri，直接删除文件
                val file = imageUri.path?.let { File(it) }
                file?.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.show(TAG, "删除照片失败！")
        }
    }

    /**
     * 判断坐标点是否在CameraView控件内
     *
     * @param pointX 坐标x
     * @param pointY 坐标Y
     * @return true：在view内，false：不在
     */
    private fun inRangeOfView(pointX: Int, pointY: Int): Boolean {
        val location = IntArray(2)
        binding?.flCamera?.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        return pointX >= x && pointX <= (x + (binding?.flCamera?.width ?: 0)) &&
                pointY >= y && pointY <= (y + (binding?.flCamera?.height ?: 0))
    }

    /**
     * 根据手势触碰的位置获取CameraView上的焦点，进行手动聚焦
     * FOCUS_RECT_SIZE_WIDTH：默认聚焦矩形的宽度
     *
     * @param pointX X轴坐标
     * @param pointY Y轴坐标
     */
    fun focusCamera(pointX: Int, pointY: Int) {
        // 转换屏幕坐标到 View 内的相对坐标
        val location = IntArray(2)
        binding?.flCamera?.getLocationOnScreen(location)
        val viewX = pointX - location[0]
        val viewY = pointY - location[1]

        if (!inRangeOfView(pointX, pointY)) {
            return
        }

        val rect = Rect(
            viewX - FOCUS_RECT_SIZE_WIDTH, viewY - FOCUS_RECT_SIZE_WIDTH,
            viewX + FOCUS_RECT_SIZE_WIDTH, viewY + FOCUS_RECT_SIZE_WIDTH
        )

        // 显示对焦框
        binding?.focusView?.visibility = VISIBLE
        binding?.focusView?.setFocusRect(rect)

        // 请求相机对焦（需要将相对坐标转换为相机所需的坐标系统）
        val cameraRect = Rect(
            (viewX - FOCUS_RECT_SIZE_WIDTH) * 2000 / width,
            (viewY - FOCUS_RECT_SIZE_WIDTH) * 2000 / height,
            (viewX + FOCUS_RECT_SIZE_WIDTH) * 2000 / width,
            (viewY + FOCUS_RECT_SIZE_WIDTH) * 2000 / height
        )
        binding?.mCameraView?.requestFocus(FOCUS_DOWN, cameraRect)
    }


    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // 释放资源
        GSYVideoManager.releaseAllVideos()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        this.owner = null
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        binding?.videoPlayer?.onVideoPause()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        binding?.videoPlayer?.onVideoResume()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        this.owner = owner;
        startCamera(owner, false)
    }

    /**
     * 拍照模式
     */
    public fun captureOnly() {
        buttonFeatures = Mode.BUTTON_STATE_ONLY_CAPTURE
    }

    /**
     * 录像模式
     */
    public fun recordOnly() {
        buttonFeatures = Mode.BUTTON_STATE_ONLY_RECORDER
    }

    /**
     * 设置模式
     * @param mode 参考mode
     */
    public fun setMode(mode: Int) {
        buttonFeatures = mode
    }

    /**
     * 拍照和录像
     */
    public fun cameraBoth() {
        buttonFeatures = Mode.BUTTON_STATE_BOTH
    }

    companion object {
        const val TAG: String = "CameraView"

        object Result {
            /**
             * 返回的时候用，用于判断是拍照
             */
            const val IMAGE = "image"

            /**
             * 返回的时候用，用于判断是视频
             */
            const val VIDEO = "video"
        }

        object Mode {
            //只能拍照
            const val BUTTON_STATE_ONLY_CAPTURE: Int = 0x101

            //只能录像
            const val BUTTON_STATE_ONLY_RECORDER: Int = 0x102

            // 选择拍照 拍视频 或者都有
            const val BUTTON_STATE_BOTH: Int = 0x103
        }

        /**
         * 轻触屏幕的范围，即聚焦点的范围大小
         */
        private const val FOCUS_RECT_SIZE_WIDTH = 50
    }
}
