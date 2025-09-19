package pers.fz.mvvm.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureManager;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.databinding.ActivityCaptureBinding;
import pers.fz.mvvm.utils.common.QRCodeUtil;
import pers.fz.mvvm.utils.log.LogUtil;
import pers.fz.mvvm.utils.permission.PermissionManager;
import pers.fz.mvvm.utils.zxing.CustomViewfinderView;
import pers.fz.mvvm.viewmodel.EmptyViewModel;

/**
 * created by fz on 2023/11/8 11:10
 * describe:自定义扫码页面
 */
@AndroidEntryPoint
public class CaptureActivity extends BaseActivity<EmptyViewModel, ActivityCaptureBinding> {
    private CaptureManager capture;
    private ActivityResultLauncher<String> openGalleryRequest;

    private final String[] PERMISSIONS_READ = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final String[] PERMISSIONS_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES
    };
    private final String[] PERMISSIONS_READ_UPSIDE_DOWN_CAKE = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    public final static String SCAN_COLOR = "scanColor";
    private PermissionManager permissionManager;

    @Override
    protected boolean enableImmersionBar() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_capture;
    }

    @Override
    public String setTitleBar() {
        return "";
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        permissionManager = new PermissionManager(this);
        permissionManager.setOnGrantedCallback(map -> {

        });
        openGalleryRequest = registerForActivityResult(new ActivityResultContracts.GetContent(), activityResultCallback);
        binding.scanFlashLight.setOnClickListener(v -> switchFlashLight());
        binding.scanPhoto.setOnClickListener(v -> openGallery());
        capture = new CaptureManager(this, binding.dbvCustom);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    @Override
    public void initData(Bundle bundle) {
        int scanColor = bundle.getInt(SCAN_COLOR, ContextCompat.getColor(this, R.color.themeColor));
        //修改框框颜色
        CustomViewfinderView customViewfinderView = binding.dbvCustom.findViewById(R.id.zxing_viewfinder_view);
        customViewfinderView.setLineColor(scanColor);
        customViewfinderView.setScanLineColor(scanColor);
    }

    ActivityResultCallback<Uri> activityResultCallback = result -> {
        if (result == null) {
            return;
        }
        handleImage(result);
    };

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (permissionManager.lacksPermissions(PERMISSIONS_READ_UPSIDE_DOWN_CAKE)) {
                permissionManager.request(PERMISSIONS_READ_UPSIDE_DOWN_CAKE);
                return;
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            if (permissionManager.lacksPermissions(PERMISSIONS_READ_TIRAMISU)) {
                permissionManager.request(PERMISSIONS_READ_TIRAMISU);
                return;
            }
        } else {
            if (permissionManager.lacksPermissions(PERMISSIONS_READ)) {
                permissionManager.request(PERMISSIONS_READ);
                return;
            }
        }
        openGalleryRequest.launch("image/*");
    }

    private void handleImage(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            if (contentResolver == null) {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
                return;
            }
            ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if (parcelFileDescriptor == null) {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
            parcelFileDescriptor.close();
            if (bitmap == null) {
                Toast.makeText(this, "未找到二维码", Toast.LENGTH_SHORT).show();
                return;
            }
            Result result = QRCodeUtil.getRawResult(bitmap);
            if (result == null) {
                Toast.makeText(this, "未找到二维码", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intents.Scan.ACTION);
            intent.putExtra(Intents.Scan.RESULT, result.getText());
            this.setResult(Activity.RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "识别失败：" + e);
            // 处理识别失败
            Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    private void switchFlashLight() {
        if (!isSupportFlashLight()) {
            Toast.makeText(CaptureActivity.this, "您的设备不支持手电筒功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.scanFlashLight.isSelected()) {
            // 关闭闪光灯
            binding.scanFlashLight.setSelected(false);
            binding.dbvCustom.setTorchOff();
        } else {
            // 打开闪光灯
            binding.scanFlashLight.setSelected(true);
            binding.dbvCustom.setTorchOn();
        }
    }

    /**
     * 检查设备是否支持闪光灯
     *
     * @return true:支持
     */
    private boolean isSupportFlashLight() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
        if (openGalleryRequest != null) {
            openGalleryRequest.unregister();
        }
        if (permissionManager != null) {
            permissionManager.unregister();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return binding.dbvCustom.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

}