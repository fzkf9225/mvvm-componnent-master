package com.casic.otitan.demo.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.casic.otitan.demo.R
import com.casic.otitan.demo.databinding.ActivityCustomCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import com.casic.otitan.media.helper.ConstantsHelper
import com.casic.otitan.common.activity.CameraActivity
import com.casic.otitan.common.base.BaseActivity
import com.casic.otitan.common.utils.permission.PermissionManager
import com.casic.otitan.common.viewmodel.EmptyViewModel
import com.casic.otitan.common.widget.camera.CameraView

@AndroidEntryPoint
class CustomCameraActivity : BaseActivity<EmptyViewModel, ActivityCustomCameraBinding>() {

    private var launcher: ActivityResultLauncher<Intent>? = null

    private var permissionManager: PermissionManager? = null
    override fun getLayoutId() = R.layout.activity_custom_camera

    override fun setTitleBar(): String? = "自定义相机"

    override fun initView(savedInstanceState: Bundle?) {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), {
            if (it.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }
            val bundle = it.data?.extras
            when (bundle?.getString(CameraActivity.Companion.Result.MEDIA_TYPE)) {
                CameraView.Companion.Result.IMAGE -> {
                    Glide.with(this)
                        .load(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) bundle.getParcelable(
                                CameraActivity.Companion.Result.PATH,
                                Uri::class.java
                            )
                            else bundle.getParcelable(CameraActivity.Companion.Result.PATH)
                        )
                        .into(binding.image)
                }

                CameraView.Companion.Result.VIDEO -> {
                    Glide.with(this)
                        .load(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) bundle.getParcelable(
                                CameraActivity.Companion.Result.PATH,
                                Uri::class.java
                            )
                            else bundle.getParcelable(CameraActivity.Companion.Result.PATH)
                        )
                        .into(binding.layoutVideo.imageVideo)
                }
            }
        })
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
            launcher?.launch(Intent(this, CameraActivity::class.java))
        }
    }

    override fun initData(bundle: Bundle?) {
        permissionManager = PermissionManager(this)
    }
}