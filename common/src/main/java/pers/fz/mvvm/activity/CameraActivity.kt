package pers.fz.mvvm.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.R
import pers.fz.mvvm.base.BaseActivity
import pers.fz.mvvm.databinding.ActivityShootBinding
import pers.fz.mvvm.listener.CameraResultListener
import pers.fz.mvvm.util.log.LogUtil
import pers.fz.mvvm.viewmodel.EmptyViewModel
import pers.fz.mvvm.widget.camera.CameraView

/**
 * created by fz on 2024/10/22 17:21
 * describe:拍摄
 */
@AndroidEntryPoint
class CameraActivity : BaseActivity<EmptyViewModel, ActivityShootBinding>() {
    companion object {
        object Result {
            /**
             * 回调的时候用于返回图片、视频路径路径，路径类型为Uri
             */
            const val PATH = "path"

            /**
             * 回调的时候用于返回图片、视判断是图片还是视频，参考值：CameraView.IMAGE   CameraView.VIDEO
             */
            const val MEDIA_TYPE = "mediaType"
        }

        object Params {
            /**
             * 配置视频最大长度
             */
            const val MAX_DURATION = "maxDuration"

            /**
             * 图片或视频保存路径
             */
            const val IMAGE_OUTPUT_PATH = "imageOutputPath"

            /**
             * 视频保存路径
             */
            const val VIDEO_OUTPUT_PATH = "videoOutputPath"

            /**
             * 图片或视频保存文件名
             */
            const val IMAGE_OUTPUT_FILE_NAME = "imageOutputFileName"

            /**
             * 视频保存文件名
             */
            const val VIDEO_OUTPUT_FILE_NAME = "videoOutputFileName"

            /**
             * 图片或视频保存文件类型
             */
            const val IMAGE_OUTPUT_FILE_MIME_TYPE = "imageOutputFileMimeType"

            /**
             * 视频保存文件类型
             */
            const val VIDEO_OUTPUT_FILE_MIME_TYPE = "videoOutputFileMimeType"

            /**
             * 视频最大录制时长（秒）
             */
            const val DEFAULT_MAX_DURATION = 30

            /**
             * 相机模式
             */
            const val BUTTON_FEATURES = "buttonFeatures"
        }
    }

    /**
     * 视频最大拍摄时间
     */
    private var maxDuration: Int = Params.DEFAULT_MAX_DURATION

    override fun getLayoutId() = R.layout.activity_shoot

    override fun setTitleBar() = "拍摄"

    override fun hasToolBar() = false
    @SuppressLint("ClickableViewAccessibility")
    override fun initView(p0: Bundle?) {
        lifecycle.addObserver(binding.cameraFaceView)
        //设置拍照回调接口,拍照成功和错误回调
        binding.cameraFaceView.setCameraResultListener(object : CameraResultListener {
            override fun captureSuccess(uri: Uri, mediaType: String?) {
                onFinish(uri, mediaType)
            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                showToast(message)
            }
        })
        binding.cameraFaceView.setLeftClickListener {
            finish()
        }
    }

    private fun onFinish(uri: Uri?, mediaType: String?) {
        LogUtil.show(CameraView.TAG, "uri:${uri.toString()}, mediaType:$mediaType")
        val returnBundle = intent.extras ?: Bundle();
        returnBundle.putParcelable(Result.PATH, uri)
        returnBundle.putString(Result.MEDIA_TYPE, mediaType)
        setResult(RESULT_OK, intent.putExtras(returnBundle))
        finish()
    }

    override fun enableImmersionBar(): Boolean {
        return true
    }

    override fun initData(p0: Bundle?) {
        maxDuration = p0?.getInt(Params.MAX_DURATION, Params.DEFAULT_MAX_DURATION)
            ?: Params.DEFAULT_MAX_DURATION
        binding.cameraFaceView.setVideoMaxDuration(maxDuration)
        binding.cameraFaceView.imageOutPutPath = p0?.getString(Params.IMAGE_OUTPUT_PATH)
        binding.cameraFaceView.imageOutPutFileName = p0?.getString(Params.IMAGE_OUTPUT_FILE_NAME)
        binding.cameraFaceView.imageOutPutFileMineType =
            p0?.getString(Params.IMAGE_OUTPUT_FILE_MIME_TYPE)
        binding.cameraFaceView.videoOutPutPath = p0?.getString(Params.VIDEO_OUTPUT_PATH)
        binding.cameraFaceView.videoOutPutFileName = p0?.getString(Params.VIDEO_OUTPUT_FILE_NAME)
        binding.cameraFaceView.videoOutPutFileMineType =
            p0?.getString(Params.VIDEO_OUTPUT_FILE_MIME_TYPE)
        binding.cameraFaceView.buttonFeatures =
            p0?.getInt(Params.BUTTON_FEATURES, CameraView.Companion.Mode.BUTTON_STATE_BOTH)
                ?: CameraView.Companion.Mode.BUTTON_STATE_BOTH
    }


    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(binding.cameraFaceView)
    }
}