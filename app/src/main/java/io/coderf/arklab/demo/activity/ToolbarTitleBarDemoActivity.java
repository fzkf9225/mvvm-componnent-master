package io.coderf.arklab.demo.activity;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityToolbarTitleBarBinding;

/**
 * 页内 {@link io.coderf.arklab.common.widget.customview.TitleBar}：XML 属性与代码 API。
 */
@AndroidEntryPoint
public class ToolbarTitleBarDemoActivity extends BaseActivity<EmptyViewModel, ActivityToolbarTitleBarBinding> {

    private boolean rightVisible = true;
    private boolean bindBackVisible = true;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_toolbar_title_bar;
    }

    @Override
    public String setTitleBar() {
        return "TitleBar 控件";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.titleBarXml.setOnRightClickListener(() -> showToast("XML：保存"));

        binding.titleBarNoBack.setOnRightClickListener(() -> showToast("XML：更多"));

        int onPrimary = ContextCompat.getColor(this, io.coderf.arklab.common.R.color.onPrimary);
        int yellow = ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_yellow);
        int red = ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_red);
        binding.titleBarCode.setTitle("代码配置标题");
        binding.titleBarCode
                .setTitleColor(onPrimary)
                .setBackIcon(R.drawable.ic_toolbar_close)
                .setBackIconTint(red)
                .setShowBackButton(true)
                .setRightText("完成")
                .setRightTextColor(yellow)
                .setOnBackClickListener(() -> showToast("代码：返回，不关闭页面"))
                .setOnRightClickListener(() -> showToast("代码：完成"));

        binding.btnToggleRight.setOnClickListener(v -> {
            rightVisible = !rightVisible;
            binding.titleBarCode.setRightText(rightVisible ? "完成" : null);
            binding.btnToggleRight.setText(rightVisible ? "清空上面这条的右侧文字" : "恢复右侧文字「完成」");
        });

        int onSurface = ContextCompat.getColor(this, io.coderf.arklab.common.R.color.cardOnSurface);
        int primary = ContextCompat.getColor(this, io.coderf.arklab.common.R.color.themeColor);
        binding.titleBarBind
                .bind("bind() 标题", () -> showToast("bind()：返回"))
                .setTitleColor(onSurface)
                .setRightText("关闭")
                .setRightTextColor(primary)
                .setOnRightClickListener(() -> showToast("bind()：关闭"));

        binding.btnToggleBack.setOnClickListener(v -> {
            bindBackVisible = !bindBackVisible;
            binding.titleBarBind.setShowBackButton(bindBackVisible);
            binding.btnToggleBack.setText(bindBackVisible
                    ? "隐藏 bind 这条的返回按钮"
                    : "显示 bind 这条的返回按钮");
        });
    }

    @Override
    public void initData(Bundle bundle) {
    }
}
