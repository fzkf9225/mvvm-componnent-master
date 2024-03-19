package com.casic.titan.demo.activity;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.core.content.ContextCompat;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.Person;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityVerifyBinding;

import java.util.Arrays;
import java.util.List;

import pers.fz.mvvm.annotations.EntityValidator;
import pers.fz.mvvm.annotations.VerifyResult;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.listener.OnOptionBottomMenuClickListener;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.viewmodel.MainViewModel;
import pers.fz.mvvm.wight.dialog.MenuDialog;

public class VerifyActivity extends BaseActivity<MainViewModel, ActivityVerifyBinding> {
    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_verify;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        getLifecycle().addObserver(binding.formImage);
        binding.editHobby.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null) {
                    binding.getData().setHobby(null);
                    return;
                }
                binding.getData().setHobby(Arrays.asList(s.toString().split("、")));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.verifySubmit.setOnClickListener(v -> {
            showLoading("验证中...");
            VerifyResult verifyResult = EntityValidator.validate(binding.getData());
            hideLoading();
            if (verifyResult.isOk()) {
                binding.tvVerifyResult.setTextColor(ContextCompat.getColor(this, pers.fz.mvvm.R.color.theme_green));
            } else {
                binding.tvVerifyResult.setTextColor(ContextCompat.getColor(this, pers.fz.mvvm.R.color.theme_red));
            }
            showToast((verifyResult.isOk() ? "验证成功" : "验证失败：") + StringUtil.FilterNull(verifyResult.getErrorMsg()));
            binding.tvVerifyResult.setText(String.format("%s,验证结果：%s", binding.getData().toString(), verifyResult.getErrorMsg()));
        });
        binding.tvSex.setOnClickListener(v ->
                new MenuDialog<>(this)
                        .setData("男", "女", "未知")
                        .setOnOptionBottomMenuClickListener((dialog, list, pos) -> binding.getData().setSex(list.get(pos).getName()))
                        .builder()
                        .show());
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        binding.setData(new Person("张三", "15210230000", "055162260000", "18", "72.00", "172", "tencent@qq.com", null));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLifecycle().removeObserver(binding.formImage);
    }
}