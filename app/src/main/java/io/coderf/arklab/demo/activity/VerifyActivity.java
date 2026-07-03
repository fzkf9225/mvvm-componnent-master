package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.annotation.bean.FieldVerifyError;
import io.coderf.arklab.annotation.bean.VerifyResult;
import io.coderf.arklab.annotation.verify.EntityValidator;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.MenuDialog;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityVerifyBinding;
import io.coderf.arklab.demo.viewmodel.VerifyViewModel;

/**
 * Default 分组校验示例：
 * <ul>
 *     <li>{@link EntityValidator#validateAll(Object)} 收集全部错误</li>
 *     <li>{@code @Ignore} 字段：教育经历 / 开学时间 / 上课时间（跨字段日期、TIME 类型）</li>
 *     <li>Room 持久化字段 schema 不变，仅扩展非持久化演示字段</li>
 * </ul>
 */
@AndroidEntryPoint
public class VerifyActivity extends BaseActivity<VerifyViewModel, ActivityVerifyBinding> {
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
        setupDemoFormControls();
        binding.formImage.setBaseView(this);
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
            LogUtil.logger("FormUi", "图片上传是否成功：" + binding.formImage.getAdapter().isUploadingSuccess());
            LogUtil.logger("FormUi", "数据：" + new Gson().toJson(binding.getData()));
            showLoading("验证中...", true);
            binding.getData().setImageList(AttachmentUtil.toUriList(binding.formImage.getImages()));
            // Default 分组 + validateAll：一次返回所有失败项（含 fieldName）
            VerifyResult verifyResult = EntityValidator.validateAll(binding.getData());
            hideLoading();
            showToast(formatVerifyResult(verifyResult));
            if (!verifyResult.isOk()) {
                logVerifyErrors(verifyResult);
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
        // @Ignore 演示字段：开学时间需 >= 生日；上课时间需符合 HH:mm 格式
        person.setEducationalExperienceDate("2024-01-01 ~ 2024-12-31");
        person.setSchoolStartTime("1999-09-01 09:00:00");
        person.setClassStartTime("08:30:00");
        person.setEducationLevel("本科");
        person.setAcceptNewsletter(true);
        person.setAgreeProtocol(true);
        person.setContactPreference("微信");
        person.setDailySteps(8000);
        person.setServiceRating(4.5f);
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

    private static String formatVerifyResult(VerifyResult verifyResult) {
        if (verifyResult.isOk()) {
            return "验证成功";
        }
        if (verifyResult.getErrors().isEmpty()) {
            return "验证失败：" + StringUtil.filterNull(verifyResult.getErrorMsg());
        }
        StringBuilder builder = new StringBuilder("验证失败（共 ").append(verifyResult.getErrors().size()).append(" 项）：\n");
        for (FieldVerifyError error : verifyResult.getErrors()) {
            builder.append(error.getFieldName()).append("：").append(error.getErrorMsg()).append('\n');
        }
        return builder.toString().trim();
    }

    private static void logVerifyErrors(VerifyResult verifyResult) {
        for (FieldVerifyError error : verifyResult.getErrors()) {
            LogUtil.logger("VerifyDemo", error.getFieldName() + " -> " + error.getErrorMsg());
        }
    }
}
