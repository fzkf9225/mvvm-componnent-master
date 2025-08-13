package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.core.content.ContextCompat;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.Person;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityVerifyTopBinding;
import com.casic.titan.demo.viewmodel.VerifyViewModel;
import com.google.gson.Gson;

import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.annotation.inter.VerifyGroup;
import pers.fz.annotation.verify.EntityValidator;
import pers.fz.annotation.bean.VerifyResult;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.common.AttachmentUtil;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.widget.dialog.MenuDialog;

@AndroidEntryPoint
public class VerifyTopActivity extends BaseActivity<VerifyViewModel, ActivityVerifyTopBinding> {
    private UseCase useCase;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_verify_top;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.formImage.bindLifecycle(this);
        binding.formVideo.bindLifecycle(this);
        binding.formImageVideo.bindLifecycle(this);
        binding.formFile.bindLifecycle(this);
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
        mViewModel.liveData.observe(this, aBoolean -> {
            if (aBoolean) {
                setResult(RESULT_OK);
                finish();
            }
        });
        binding.verifySubmit.setOnClickListener(v -> {
            LogUtil.show("FormUi", "数据：" + new Gson().toJson(binding.getData()));
            showLoading("验证中...");
            binding.getData().setImageList(AttachmentUtil.toUriList(binding.formImage.getImages()));
            VerifyResult verifyResult = EntityValidator.validate(binding.getData(), VerifyGroup.Create.class);
            hideLoading();
            showToast((verifyResult.isOk() ? "验证成功" : "验证失败：") + StringUtil.filterNull(verifyResult.getErrorMsg()));
            if (!verifyResult.isOk()) {
                return;
            }
            mViewModel.add(binding.getData());
        });
        binding.tvSex.setOnClickListener(v ->
                new MenuDialog<>(this)
                        .setData("男", "女", "未知")
                        .setOnOptionBottomMenuClickListener((dialog, list, pos) -> {
                            binding.getData().setSex(list.get(pos).getPopupName());
                            dialog.dismiss();
                        })
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
        binding.setData(new Person("张三", "1999-06-05", "15210230000", "055162260000", "18", "72.00", "172", "tencent@qq.com", null));
    }

}