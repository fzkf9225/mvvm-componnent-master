package io.coderf.arklab.demo.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;

import io.coderf.arklab.common.bean.BannerBean;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.core.utils.DensityUtil;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityToolbarPreviewBinding;
import io.coderf.arklab.demo.enums.ToolbarDemoStyle;

/**
 * 按 {@link ToolbarDemoStyle} 套用一套 ToolbarConfig，必要时再挂菜单或运行时按钮。
 */
@AndroidEntryPoint
public class ToolbarPreviewActivity extends BaseActivity<EmptyViewModel, ActivityToolbarPreviewBinding> {

    public static final String EXTRA_STYLE = "toolbar_demo_style";

    private static final String[] TITLES = {
            "运行时标题",
            "第二行标题",
            "Toolbar Live"
    };

    private ToolbarDemoStyle style = ToolbarDemoStyle.DEFAULT;
    private int titleIndex;
    private boolean brandColor;
    private boolean showBack = true;
    private int gravityIndex;
    private boolean bold;
    private boolean tall;
    @Nullable
    private Boolean iconsLight;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_toolbar_preview;
    }

    @Override
    public String setTitleBar() {
        return style().toolbarTitle;
    }

    @Override
    protected boolean hasToolBar() {
        return !style().noToolbar;
    }

    @Override
    protected boolean enableImmersionBar() {
        return style().immersion;
    }

    @Override
    public ToolbarConfig createdToolbarConfig() {
        ToolbarConfig config = new ToolbarConfig(this)
                .setEnableImmersionBar(enableImmersionBar())
                .setTitle(setTitleBar());
        style().apply(this, config);
        return config.applyStatusBar();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ToolbarDemoStyle current = style();
        binding.tvName.setText(current.listName);
        binding.tvSummary.setText(current.summary);
        binding.tvDetail.setText(current.detail);
        binding.banner.setVisibility(current.contentUnder && !current.gallery ? View.VISIBLE : View.GONE);
        if (current.bannerCaption != null) {
            binding.tvBanner.setText(current.bannerCaption);
        }
        binding.imageGallery.setVisibility(current.gallery ? View.VISIBLE : View.GONE);
        if (current.gallery) {
            bindImmersiveImages();
        }
        binding.btnPageBack.setVisibility(current.noToolbar ? View.VISIBLE : View.GONE);
        if (current.noToolbar) {
            binding.btnPageBack.setOnClickListener(
                    v -> getOnBackPressedDispatcher().onBackPressed());
        }
        binding.runtimePanel.setVisibility(current.runtime ? View.VISIBLE : View.GONE);
        if (current.menu) {
            int menuRes = current.lightMenu ? R.menu.menu_toolbar_overlay : R.menu.menu_toolbar_demo;
            inflateToolbarMenu(menuRes, this::onToolbarMenu);
            if (current.lightMenu) {
                tintOverflowIconLight();
                MaterialToolbar toolbar = getToolbar();
                if (toolbar != null) {
                    toolbar.post(this::tintOverflowIconLight);
                }
            }
        }
        if (current.hideSystemBars) {
            coverStatusBar();
        }
        if (current.runtime) {
            binding.btnTitle.setOnClickListener(v -> cycleTitle());
            binding.btnColor.setOnClickListener(v -> toggleBrandColor());
            binding.btnBack.setOnClickListener(v -> toggleBack());
            binding.btnGravity.setOnClickListener(v -> cycleGravity());
            binding.btnBold.setOnClickListener(v -> toggleBold());
            binding.btnHeight.setOnClickListener(v -> toggleHeight());
            binding.btnIcons.setOnClickListener(v -> cycleStatusBarIcons());
        }
    }

    @Override
    public void initData(Bundle bundle) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (style().hideSystemBars) {
            hideStatusBarIcons();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && style().hideSystemBars) {
            hideStatusBarIcons();
        }
    }

    /**
     * 只去掉状态栏图标。高度是否保留由 {@link ToolbarConfig#setKeepStatusBarHeight(boolean)} 决定。
     */
    private void coverStatusBar() {
        hideStatusBarIcons();
        MaterialToolbar toolbar = getToolbar();
        if (toolbar != null) {
            ViewCompat.requestApplyInsets(toolbar);
        }
    }

    private void hideStatusBarIcons() {
        Window window = getWindow();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        controller.hide(WindowInsetsCompat.Type.statusBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void bindImmersiveImages() {
        List<String> urls = Arrays.asList(
                "https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800",
                "https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg",
                "https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42",
                "https://q8.itc.cn/images01/20240208/45d5ee19361f4f8fa824e93ebfc42a8a.jpeg"
        );
        binding.immersiveBanner.initView(Arrays.asList(
                new BannerBean(urls.get(0)),
                new BannerBean(urls.get(1)),
                new BannerBean(urls.get(2)),
                new BannerBean(R.mipmap.ic_launcher)
        ));
        Glide.with(this).load(urls.get(1)).into(binding.immersiveImage1);
        Glide.with(this).load(urls.get(2)).into(binding.immersiveImage2);
        Glide.with(this).load(urls.get(3)).into(binding.immersiveImage3);
    }

    private void tintOverflowIconLight() {
        MaterialToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow == null) {
            return;
        }
        int color = ContextCompat.getColor(this, io.coderf.arklab.common.R.color.onPrimary);
        Drawable tinted = DrawableCompat.wrap(overflow.mutate());
        DrawableCompat.setTint(tinted, color);
        toolbar.setOverflowIcon(tinted);
    }

    private boolean onToolbarMenu(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toolbar_done) {
            showToast("菜单：完成");
            return true;
        }
        if (id == R.id.action_toolbar_search) {
            showToast("菜单：搜索");
            return true;
        }
        if (id == R.id.action_toolbar_share) {
            showToast("菜单：分享");
            return true;
        }
        if (id == R.id.action_toolbar_settings) {
            showToast("菜单：设置");
            return true;
        }
        return false;
    }

    private void cycleTitle() {
        titleIndex = (titleIndex + 1) % TITLES.length;
        config().setTitle(TITLES[titleIndex]);
        binding.btnTitle.setText("当前标题：" + TITLES[titleIndex]);
    }

    private void toggleBrandColor() {
        brandColor = !brandColor;
        int bg = brandColor
                ? io.coderf.arklab.common.R.color.themeColor
                : io.coderf.arklab.common.R.color.cardSurface;
        int text = brandColor
                ? io.coderf.arklab.common.R.color.onPrimary
                : io.coderf.arklab.common.R.color.cardOnSurface;
        config().setBgColor(bg)
                .setTextColor(text)
                .setBackIconColor(0)
                .applyStatusBar();
        binding.btnColor.setText(brandColor ? "恢复默认表面色" : "切换为品牌色");
    }

    private void toggleBack() {
        showBack = !showBack;
        config().setShowBackButton(showBack);
        binding.btnBack.setText(showBack ? "隐藏返回按钮" : "显示返回按钮");
    }

    private void cycleGravity() {
        gravityIndex = (gravityIndex + 1) % 3;
        ToolbarConfig config = config();
        if (gravityIndex == 1) {
            config.setTitleGravityStart();
            binding.btnGravity.setText("标题对齐：靠左");
        } else if (gravityIndex == 2) {
            config.setTitleGravityEnd();
            binding.btnGravity.setText("标题对齐：靠右");
        } else {
            config.setTitleGravityCenter();
            binding.btnGravity.setText("标题对齐：居中");
        }
    }

    private void toggleBold() {
        bold = !bold;
        config().setTitleBold(bold);
        binding.btnBold.setText(bold ? "取消加粗" : "标题加粗");
    }

    private void toggleHeight() {
        tall = !tall;
        config().setHeight(tall ? DensityUtil.dp2px(this, 80f) : 0);
        applyToolbarHeight();
        binding.btnHeight.setText(tall ? "恢复默认高度" : "高度改为 80dp");
    }

    private void cycleStatusBarIcons() {
        if (iconsLight == null) {
            iconsLight = Boolean.TRUE;
            binding.btnIcons.setText("状态栏图标：强制浅色");
        } else if (iconsLight) {
            iconsLight = Boolean.FALSE;
            binding.btnIcons.setText("状态栏图标：强制深色");
        } else {
            iconsLight = null;
            binding.btnIcons.setText("状态栏图标：自动");
        }
        config().setStatusBarIconsLight(iconsLight).applyStatusBar();
    }

    @NonNull
    private ToolbarConfig config() {
        return toolbarBind.getToolbarConfig();
    }

    @NonNull
    private ToolbarDemoStyle style() {
        if (style == null) {
            style = ToolbarDemoStyle.DEFAULT;
        }
        if (getIntent() != null) {
            style = ToolbarDemoStyle.from(getIntent().getStringExtra(EXTRA_STYLE));
        }
        return style;
    }
}
