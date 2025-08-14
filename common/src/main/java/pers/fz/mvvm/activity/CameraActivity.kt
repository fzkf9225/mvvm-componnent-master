package pers.fz.mvvm.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.R
import pers.fz.mvvm.base.BaseActivity
import pers.fz.mvvm.databinding.ActivityShootBinding
import pers.fz.mvvm.listener.BackgroundCameraListener
import pers.fz.mvvm.util.log.LogUtil
import pers.fz.mvvm.viewmodel.EmptyViewModel
import pers.fz.mvvm.widget.camera.CameraView

/**
 * created by fz on 2024/10/22 17:21
 * describe:拍摄
 */
@AndroidEntryPoint
class CameraActivity : BaseActivity<EmptyViewModel, ActivityShootBinding>(), OnTouchListener {
    companion object {
        /**
         * 回调的时候用于返回图片、视频路径路径，路径类型为Uri
         */
        const val PATH = "path"

        /**
         * 回调的时候用于返回图片、视判断是图片还是视频，参考值：CameraView.IMAGE   CameraView.VIDEO
         */
        const val MEDIA_TYPE = "mediaType"

        /**
         * 配置视频最大长度
         */
        const val MAX_DURATION = "maxDuration"
    }

    /**
     * 视频最大拍摄时间
     */
    private var maxDuration: Int = 30

    override fun getLayoutId() = R.layout.activity_shoot

    override fun setTitleBar() = "拍摄"

    override fun hasToolBar() = false
    override fun initView(p0: Bundle?) {
        lifecycle.addObserver(binding.cameraFaceView)
        //设置拍照回调接口,拍照成功和错误回调
        binding.cameraFaceView.setBackgroundCameraListener(object : BackgroundCameraListener {
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
        returnBundle.putParcelable(PATH, uri)
        returnBundle.putString(MEDIA_TYPE, mediaType)
        setResult(Activity.RESULT_OK, intent.putExtras(returnBundle))
        finish()
    }

    override fun enableImmersionBar(): Boolean {
        return true
    }
    /**
     * 轻触屏幕聚焦
     * FOCUS_RECT_SIZE_WIDTH：默认聚焦矩形的宽度
     * @param v     点击的view
     * @param event 手势事件
     * @return false，如果返回true则拦截当前页面的onClick事件，需要利用模拟点击事件触发onClick事件view.performClick()
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        binding.cameraFaceView.focusCamera(x, y)
        return false
    }

    override fun initData(p0: Bundle?) {
        maxDuration = p0?.getInt(MAX_DURATION, 30) ?: 30
        binding.cameraFaceView.setVideoMaxDuration(maxDuration)
    }


    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(binding.cameraFaceView)
    }
}