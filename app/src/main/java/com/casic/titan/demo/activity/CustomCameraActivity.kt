package com.casic.titan.demo.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.casic.titan.demo.R
import com.casic.titan.demo.databinding.ActivityCustomCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.activity.CameraActivity
import pers.fz.mvvm.base.BaseActivity
import pers.fz.mvvm.viewmodel.EmptyViewModel
import pers.fz.mvvm.widget.camera.CameraView

@AndroidEntryPoint
class CustomCameraActivity : BaseActivity<EmptyViewModel, ActivityCustomCameraBinding>() {

    private var launcher: ActivityResultLauncher<Intent>? = null
    override fun getLayoutId() = R.layout.activity_custom_camera

    override fun setTitleBar(): String?  = "自定义相机"

    override fun initView(savedInstanceState: Bundle?) {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), {
            if (it.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }
            val bundle = it.data?.extras
            when(bundle?.getString(CameraActivity.MEDIA_TYPE)){
                CameraView.IMAGE ->{
                    Glide.with(this)
                        .load(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) bundle.getParcelable(CameraActivity.PATH, Uri::class.java)
                        else bundle.getParcelable(CameraActivity.PATH))
                        .into(binding.image)
                }
                CameraView.VIDEO ->{
                    Glide.with(this)
                        .load(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) bundle.getParcelable(CameraActivity.PATH, Uri::class.java)
                        else bundle.getParcelable(CameraActivity.PATH))
                        .into(binding.layoutVideo.imageVideo)
                }
            }
        })
        binding.buttonCamera.setOnClickListener {
            launcher?.launch(Intent(this, CameraActivity::class.java))
        }
    }

    override fun initData(bundle: Bundle?) {

    }
}