package io.coderf.arklab.demo.activity

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.helper.CameraHelper
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.utils.permission.PermissionManager
import io.coderf.arklab.common.viewmodel.EmptyViewModel
import io.coderf.arklab.common.widget.camera.CameraView
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.databinding.ActivityCustomCameraBinding
import io.coderf.arklab.media.helper.ConstantsHelper

@AndroidEntryPoint
class CustomCameraActivity : BaseActivity<EmptyViewModel, ActivityCustomCameraBinding>() {

    private var cameraHelper: CameraHelper? = null

    private var permissionManager: PermissionManager? = null
    override fun getLayoutId() = R.layout.activity_custom_camera

    override fun setTitleBar(): String? = "自定义相机"

    override fun initView(savedInstanceState: Bundle?) {
        cameraHelper = CameraHelper(this, object : CameraHelper.Callback {
            override fun onSuccess(uri: Uri, mediaType: String) {
                when (mediaType) {
                    CameraView.Companion.Result.IMAGE -> Glide.with(this@CustomCameraActivity).load(uri).into(binding.image)
                    CameraView.Companion.Result.VIDEO -> Glide.with(this@CustomCameraActivity).load(uri).into(binding.layoutVideo.imageVideo)
                }
            }

            override fun onCancel() {
            }
        })
        binding.layoutVideo.imageVideo.setBgColor(
            Color.WHITE
        )
        binding.layoutVideo.imageVideo.setRadius(DensityUtil.dp2px(this, 12f))
        binding.buttonCamera.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (permissionManager?.lacksPermissions(
                        *ConstantsHelper.PERMISSIONS_CAMERA_R
                    ) == true
                ) {
                    permissionManager?.request(*ConstantsHelper.PERMISSIONS_CAMERA_R)
                    return@setOnClickListener
                }
            } else {
                if (permissionManager?.lacksPermissions(
                        *ConstantsHelper.PERMISSIONS_CAMERA
                    ) == true
                ) {
                    permissionManager?.request(*ConstantsHelper.PERMISSIONS_CAMERA)
                    return@setOnClickListener
                }
            }
            cameraHelper?.launch()
        }
    }

    override fun initData(bundle: Bundle?) {
        permissionManager = PermissionManager(this)
    }
}
