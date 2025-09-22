package com.casic.otitan.common.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import dagger.hilt.android.AndroidEntryPoint;
import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseActivity;
import com.casic.otitan.common.bean.base.ToolbarConfig;
import com.casic.otitan.common.databinding.TextureViewPlayerActivityBinding;
import com.casic.otitan.common.viewmodel.VideoPlayerViewModel;
import com.casic.otitan.common.widget.dialog.MessageDialog;

/**
 * Create by CherishTang on 2019/11/27
 * describe:视频播放，仿腾讯视频,Oppo R11上播放会偶尔闪退返回，几率时间，原因未知
 */
@AndroidEntryPoint
public class VideoPlayerActivity extends BaseActivity<VideoPlayerViewModel, TextureViewPlayerActivityBinding> {
    public final static String VIDEO_PATH = "videoPath";
    public final static String VIDEO_TITLE = "videoTitle";
    public final static String CACHE_ENABLE = "cacheEnable";
    private boolean cacheEnable = true;
    @Override
    protected int getLayoutId() {
        return R.layout.texture_view_player_activity;
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this).setStatusBarColor(android.R.color.black).setLightMode(false).applyStatusBar();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    @Override
    public void initData(Bundle bundle) {
        String videoPath = bundle.getString(VIDEO_PATH);
        cacheEnable = bundle.getBoolean(CACHE_ENABLE,cacheEnable);
        if (TextUtils.isEmpty(videoPath)) {
            new MessageDialog(this)
                    .setCanOutSide(false)
                    .setMessage("视频播放地址错误！")
                    .setPositiveText("确定")
                    .setOnPositiveClickListener(dialog -> {
                        dialog.dismiss();
                        VideoPlayerActivity.this.finish();
                    })
                    .builder()
                    .show();
            return;
        }

        binding.videoPlayer.setUp(videoPath, cacheEnable, bundle.getString(VIDEO_TITLE));
        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(videoPath)
                .into(imageView);
        binding.videoPlayer.setThumbImageView(imageView);
        //增加title
        binding.videoPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        //设置返回键
        binding.videoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置全屏按钮不可见，则不可以修改全屏和竖屏
        binding.videoPlayer.getFullscreenButton().setVisibility(View.GONE);
        //是否可以滑动调整
        binding.videoPlayer.setIsTouchWiget(true);
        //设置返回按键功能
        binding.videoPlayer.getBackButton().setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.videoPlayer.startPlayLogic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    protected OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            //释放所有
            binding.videoPlayer.setVideoAllCallBack(null);
        }
    };

    public static void show(Context context, Bundle bundle) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, String title, String url) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_PATH, url);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }


    public static void show(Context context, String title, String url,boolean cacheEnable) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_PATH, url);
        bundle.putBoolean(CACHE_ENABLE, cacheEnable);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
