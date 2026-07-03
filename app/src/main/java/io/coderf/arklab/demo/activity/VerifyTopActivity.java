package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.annotation.bean.VerifyResult;
import io.coderf.arklab.annotation.inter.VerifyGroup;
import io.coderf.arklab.annotation.verify.EntityValidator;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.MenuDialog;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.Family;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityVerifyTopBinding;
import io.coderf.arklab.demo.viewmodel.VerifyViewModel;

/**
 * Create 分组校验示例：
 * <ul>
 *     <li>{@link VerifyWhen}：性别为「女」时座机必填；年龄≥18 时紧急联系人必填</li>
 *     <li>{@link io.coderf.arklab.annotation.annotation.VerifyCrossField}：体重 &lt; 身高（数值比较）</li>
 *     <li>{@link io.coderf.arklab.annotation.annotation.Valid}：嵌套校验 {@link Family}</li>
 *     <li>紧急联系人为 {@code @Ignore} 字段，不影响 Room 表结构</li>
 * </ul>
 */
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
        setupDemoFormControls();
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
            LogUtil.logger("FormUi", "数据：" + new Gson().toJson(binding.getData()));
            showLoading("验证中...", true);
            binding.getData().setImageList(AttachmentUtil.toUriList(binding.formImage.getImages()));
            // Create 分组：条件校验 + 跨字段 + 嵌套 @Valid
            VerifyResult verifyResult = EntityValidator.validate(binding.getData(), VerifyGroup.Create.class);
            hideLoading();
            showToast(formatCreateVerifyResult(verifyResult));
            if (!verifyResult.isOk()) {
                if (!TextUtils.isEmpty(verifyResult.getFieldName())) {
                    LogUtil.logger("VerifyDemo", "首个失败字段：" + verifyResult.getFieldName());
                }
                return;
            }
            mViewModel.add(binding.getData(), binding.formImage.getImages());
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
        Person person = new Person("张三", "1999-06-05", "15210230000", "055162260000",
                "18", "72.00", "172", "tencent@qq.com", null);
        person.setSex("男");
        person.setEmergencyContact("张三父亲");
        person.setFamily(new Family("李四", "王五"));
        person.setEducationLevel("硕士");
        person.setAcceptNewsletter(false);
        person.setAgreeProtocol(true);
        person.setContactPreference("电话");
        person.setDailySteps(10000);
        person.setServiceRating(5f);
        binding.setData(person);
    }

    private void setupDemoFormControls() {
        List<PopupWindowBean<Object>> educationItems = Arrays.asList(
                new PopupWindowBean<>("1", "本科"),
                new PopupWindowBean<>("2", "硕士"),
                new PopupWindowBean<>("3", "博士")
        );
        binding.formEducation.setSpinnerItems(educationItems);

        List<PopupWindowBean<Object>> contactItems = Arrays.asList(
                new PopupWindowBean<>("phone", "phone", "电话"),
                new PopupWindowBean<>("email", "email", "邮件"),
                new PopupWindowBean<>("wechat", "wechat", "微信")
        );
        binding.formContact.setRadioItems(contactItems);
    }

    private static String formatCreateVerifyResult(VerifyResult verifyResult) {
        if (verifyResult.isOk()) {
            return "验证成功（Create 分组）";
        }
        String fieldName = StringUtil.filterNull(verifyResult.getFieldName());
        String errorMsg = StringUtil.filterNull(verifyResult.getErrorMsg());
        if (TextUtils.isEmpty(fieldName)) {
            return "验证失败：" + errorMsg;
        }
        return "验证失败[" + fieldName + "]：" + errorMsg;
    }

}

