package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.widget.feedback.ToastHelper;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityMaterialCardBinding;

/**
 * MaterialCardView 主流能力：三种表面、媒体卡、可选中、形状、描边与 Ripple。
 */
@AndroidEntryPoint
public class MaterialCardViewActivity extends BaseActivity<EmptyViewModel, ActivityMaterialCardBinding> {

    private static final String MEDIA_COVER =
            "https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_material_card;
    }

    @Override
    public String setTitleBar() {
        return "MaterialCardView";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        Glide.with(this).load(MEDIA_COVER).into(binding.cardMediaImage);

        binding.cardElevated.setOnClickListener(v ->
                ToastHelper.showShort(this, "Elevated：阴影分层，适合独立内容块"));
        binding.cardMediaImage.setOnClickListener(v ->
                ToastHelper.showShort(this, "媒体卡：点封面进详情"));
        binding.btnMediaShare.setOnClickListener(v ->
                ToastHelper.showShort(this, "分享"));
        binding.btnMediaBookmark.setOnClickListener(v ->
                ToastHelper.showShort(this, "已收藏"));

        binding.cardSettingNotify.setOnClickListener(v -> binding.cardSettingNotify.toggle());
        binding.cardPlanBasic.setOnClickListener(v -> selectPlan(true));
        binding.cardPlanPro.setOnClickListener(v -> selectPlan(false));
        binding.cardIconTop.setOnClickListener(v -> binding.cardIconTop.toggle());
        binding.cardIconBottom.setOnClickListener(v -> binding.cardIconBottom.toggle());

        binding.cardStroke.setOnClickListener(v ->
                ToastHelper.showShort(this, "自定义描边 + Ripple"));
        binding.cardRipple.setOnClickListener(v ->
                ToastHelper.showShort(this, "clickable=true 才有水波"));
    }

    @Override
    public void initData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        UseCase useCase = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? bundle.getParcelable("args", UseCase.class)
                : bundle.getParcelable("args");
        if (useCase != null) {
            toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        }
    }

    private void selectPlan(boolean basic) {
        binding.cardPlanBasic.setChecked(basic);
        binding.cardPlanPro.setChecked(!basic);
        ToastHelper.showShort(this, basic ? "套餐：基础版" : "套餐：专业版");
    }
}
