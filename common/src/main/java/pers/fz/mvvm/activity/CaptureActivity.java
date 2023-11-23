package pers.fz.mvvm.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
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

import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureManager;

import java.util.Map;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.ActivityCaptureBinding;
import pers.fz.mvvm.util.common.QRCodeUtil;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.permission.PermissionsChecker;

/**
 * created by fz on 2023/11/8 11:10
 * describe:自定义扫码页面
 */
public class CaptureActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private CaptureManager capture;
    private ActivityCaptureBinding binding;
    private ActivityResultLauncher<String> openGalleryRequest;
    private PermissionsChecker mPermissionsChecker;
    private ActivityResultLauncher<String[]> permissionLauncher = null;

    private final String[] PERMISSIONS_READ = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final String[] PERMISSIONS_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_capture);
        //修改框框颜色
//        CustomViewfinderView customViewfinderView = binding.dbvCustom.findViewById(R.id.zxing_viewfinder_view);
//        customViewfinderView.setLineColor(ContextCompat.getColor(this,R.color.theme_orange));
//        customViewfinderView.setScanLineColor(ContextCompat.getColor(this,R.color.theme_orange));
        openGalleryRequest = registerForActivityResult(new ActivityResultContracts.GetContent(), activityResultCallback);
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
        binding.scanFlashLight.setOnClickListener(v -> switchFlashLight());
        binding.scanPhoto.setOnClickListener(v -> openGallery());
        capture = new CaptureManager(this, binding.dbvCustom);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    /**
     * 权限回调
     */
    ActivityResultCallback<Map<String, Boolean>> permissionCallback = result -> {
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            LogUtil.show(TAG, entry.getKey() + ":" + entry.getValue());
            if (Boolean.FALSE.equals(entry.getValue())) {
                Toast.makeText(this, "您拒绝了当前权限，无法打开相册", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    ActivityResultCallback<Uri> activityResultCallback = result -> {
        if (result == null) {
            return;
        }
        handleImage(result);
    };

    public PermissionsChecker getPermissionsChecker() {
        if (mPermissionsChecker == null) {
            mPermissionsChecker = new PermissionsChecker(this);
        }
        return mPermissionsChecker;
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getPermissionsChecker().lacksPermissions(PERMISSIONS_READ_TIRAMISU)) {
                permissionLauncher.launch(PERMISSIONS_READ_TIRAMISU);
                return;
            }
        } else {
            if (getPermissionsChecker().lacksPermissions(PERMISSIONS_READ)) {
                permissionLauncher.launch(PERMISSIONS_READ);
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

    /**
     * 检查设备是否支持闪光灯
     *
     * @return true:支持
     */
    private boolean isSupportFlashLight() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
        if (openGalleryRequest != null) {
            openGalleryRequest.unregister();
        }
        if (permissionLauncher != null) {
            permissionLauncher.unregister();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return binding.dbvCustom.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}