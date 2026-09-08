package io.coderf.arklab.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.annotation.bean.FieldVerifyError;
import io.coderf.arklab.annotation.bean.VerifyResult;
import io.coderf.arklab.annotation.inter.VerifyGroup;
import io.coderf.arklab.annotation.verify.EntityValidator;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.MenuDialog;
import io.coderf.arklab.core.ui.delegate.ImeAdjustEnabled;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.Family;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityVerifyBinding;
import io.coderf.arklab.demo.viewmodel.VerifyViewModel;
import io.coderf.arklab.ui.enums.LabelAlignEnum;
import io.coderf.arklab.ui.enums.TextAlignEnum;
import io.coderf.arklab.ui.form.FormConstraintLayout;

/**
 * 表单注解校验综合示例。顶部配置可切换：
 * <ul>
 *     <li>标签对齐（left / top）与正文对齐</li>
 *     <li>校验分组 Default / Create / Editor</li>
 *     <li>{@link EntityValidator#validate} 遇错即停 或 {@link EntityValidator#validateAll} 收集全部</li>
 * </ul>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/8
 */
@AndroidEntryPoint
public class VerifyActivity extends BaseActivity<VerifyViewModel, ActivityVerifyBinding> {
    private UseCase useCase;

    {
        imeInsetPolicy = ImeAdjustEnabled.INSTANCE;
    }

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
        setupConfigPanel();
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
        binding.verifySubmit.setOnClickListener(v -> submit());
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
        toolbarBind.getToolbarConfig().setTitle(useCase != null ? useCase.getName() : "自定义注解测试");
        Person person = new Person("张三", "1999-06-05", "15210230000", "055162260000",
                "18", "72.00", "172", "tencent@qq.com", Arrays.asList("篮球", "音乐"));
        person.setSex("男");
        person.setEducationalExperienceDate("2024-01-01 ~ 2024-12-31");
        person.setSchoolStartTime("1999-09-01 09:00:00");
        person.setClassStartTime("08:30:00");
        person.setEducationLevel("本科");
        person.setAcceptNewsletter(true);
        person.setAgreeProtocol(true);
        person.setContactPreference("微信");
        person.setDailySteps(8000);
        person.setServiceRating(4.5f);
        person.setEmergencyContact("张三父亲");
        Family family = new Family("李四", "王五");
        person.setFamily(family);
        person.setFamilyList(Collections.singletonList(family));
        person.setIdCard("110101199003078515");
        person.setHomepage("https://www.example.com");
        person.setPostalCode("100000");
        person.setNickname("zhangsan");
        person.setRemark("编辑分组演示备注");
        binding.setData(person);
        binding.editHobby.setText("篮球、音乐");
        updateGroupHint();
    }

    private void setupConfigPanel() {
        binding.chipLabelAlign.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int align = checkedIds.contains(R.id.chip_align_top)
                    ? LabelAlignEnum.TOP.value
                    : LabelAlignEnum.LEFT.value;
            applyFormAlign(align, currentTextAlign());
            boolean left = align == LabelAlignEnum.LEFT.value;
            binding.chipTextLeft.setEnabled(left);
            binding.chipTextRight.setEnabled(left);
        });
        binding.chipTextAlign.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            applyFormAlign(currentLabelAlign(), currentTextAlign());
        });
        binding.chipVerifyGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            updateGroupHint();
        });
    }

    private void applyFormAlign(int labelAlign, int textAlign) {
        applyFormAlignRecursive(binding.formContainer, labelAlign, textAlign);
    }

    private void applyFormAlignRecursive(ViewGroup parent, int labelAlign, int textAlign) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof FormConstraintLayout form) {
                form.setFormAlign(labelAlign, textAlign);
            } else if (child instanceof ViewGroup viewGroup) {
                applyFormAlignRecursive(viewGroup, labelAlign, textAlign);
            }
        }
    }

    private int currentLabelAlign() {
        return binding.chipAlignTop.isChecked() ? LabelAlignEnum.TOP.value : LabelAlignEnum.LEFT.value;
    }

    private int currentTextAlign() {
        return binding.chipTextLeft.isChecked() ? TextAlignEnum.LEFT.value : TextAlignEnum.RIGHT.value;
    }

    private Class<?> currentVerifyGroup() {
        if (binding.chipGroupCreate.isChecked()) {
            return VerifyGroup.Create.class;
        }
        if (binding.chipGroupEditor.isChecked()) {
            return VerifyGroup.Editor.class;
        }
        return VerifyGroup.Default.class;
    }

    private boolean collectAllErrors() {
        return binding.chipModeAll.isChecked();
    }

    private void updateGroupHint() {
        Class<?> group = currentVerifyGroup();
        if (group == VerifyGroup.Create.class) {
            binding.tvVerifyHint.setText("Create：女→座机必填（@VerifyWhen）；成年→紧急联系人；订阅资讯的女性→配偶姓名（@VerifyWhenAll）；体重<身高；嵌套 @Valid 家庭；邮箱必填。");
        } else if (group == VerifyGroup.Editor.class) {
            binding.tvVerifyHint.setText("Editor：身份证 / 网址 / 邮编 / 昵称（ID_CARD、URL、POSTAL_CODE、REGEX、NOT_IN）；备注长度；@VerifyArray 家庭列表。");
        } else {
            binding.tvVerifyHint.setText("Default：姓名必须为张三（EQUALS）；座机必填；教育经历 / 开学 DATETIME / 上课 TIME；开学时间≥生日（@VerifyCrossFields）。");
        }
    }

    private void submit() {
        Person data = binding.getData();
        LogUtil.logger("FormUi", "图片上传是否成功：" + binding.formImage.getAdapter().isUploadingSuccess());
        LogUtil.logger("FormUi", "数据：" + new Gson().toJson(data));
        showLoading("验证中...", true);
        data.setImageList(AttachmentUtil.toUriList(binding.formImage.getImages()));
        Class<?> group = currentVerifyGroup();
        VerifyResult verifyResult = collectAllErrors()
                ? EntityValidator.validateAll(data, group)
                : EntityValidator.validate(data, group);
        hideLoading();
        applyFieldErrors(verifyResult);
        showToast(formatVerifyResult(verifyResult, group));
        if (!verifyResult.isOk()) {
            logVerifyErrors(verifyResult);
            return;
        }
        mViewModel.add(data, binding.formImage.getImages());
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

    private void applyFieldErrors(VerifyResult verifyResult) {
        clearFormErrors();
        if (verifyResult.isOk()) {
            return;
        }
        if (verifyResult.getErrors().isEmpty()) {
            setFieldError(verifyResult.getFieldName(), verifyResult.getErrorMsg());
            return;
        }
        for (FieldVerifyError error : verifyResult.getErrors()) {
            setFieldError(error.getFieldName(), error.getErrorMsg());
        }
    }

    private void clearFormErrors() {
        binding.editName.setError(null);
        binding.tvSex.setError(null);
        binding.formEducation.setError(null);
        binding.formAgree.setError(null);
        binding.tvBirthday.setError(null);
        binding.tvEducationalExperienceDate.setError(null);
        binding.tvSchoolStartTime.setError(null);
        binding.tvClassStartTime.setError(null);
        binding.editMobile.setError(null);
        binding.editTel.setError(null);
        binding.editAge.setError(null);
        binding.editWeight.setError(null);
        binding.editHeight.setError(null);
        binding.editEmail.setError(null);
        binding.editEmergencyContact.setError(null);
        binding.editSpouseName.setError(null);
        binding.editIdCard.setError(null);
        binding.editHomepage.setError(null);
        binding.editPostalCode.setError(null);
        binding.editNickname.setError(null);
        binding.editRemark.setError(null);
        binding.editWife.setError(null);
        binding.editHusband.setError(null);
        binding.editHobby.setError(null);
    }

    private void setFieldError(String fieldName, String errorMsg) {
        FormConstraintLayout field = resolveField(fieldName);
        if (field != null) {
            field.setError(errorMsg);
        }
    }

    @Nullable
    private FormConstraintLayout resolveField(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        return switch (fieldName) {
            case "name" -> binding.editName;
            case "sex" -> binding.tvSex;
            case "educationLevel" -> binding.formEducation;
            case "agreeProtocol" -> binding.formAgree;
            case "birthday" -> binding.tvBirthday;
            case "educationalExperienceDate" -> binding.tvEducationalExperienceDate;
            case "schoolStartTime" -> binding.tvSchoolStartTime;
            case "classStartTime" -> binding.tvClassStartTime;
            case "mobile" -> binding.editMobile;
            case "tel" -> binding.editTel;
            case "age" -> binding.editAge;
            case "weight" -> binding.editWeight;
            case "height" -> binding.editHeight;
            case "email" -> binding.editEmail;
            case "emergencyContact" -> binding.editEmergencyContact;
            case "spouseName" -> binding.editSpouseName;
            case "idCard" -> binding.editIdCard;
            case "homepage" -> binding.editHomepage;
            case "postalCode" -> binding.editPostalCode;
            case "nickname" -> binding.editNickname;
            case "remark" -> binding.editRemark;
            case "wife" -> binding.editWife;
            case "husband" -> binding.editHusband;
            case "hobby" -> binding.editHobby;
            default -> null;
        };
    }

    private static String formatVerifyResult(VerifyResult verifyResult, Class<?> group) {
        String groupName = group.getSimpleName();
        if (verifyResult.isOk()) {
            return "验证成功（" + groupName + "）";
        }
        if (verifyResult.getErrors().isEmpty()) {
            String fieldName = StringUtil.filterNull(verifyResult.getFieldName());
            String errorMsg = StringUtil.filterNull(verifyResult.getErrorMsg());
            if (fieldName.isEmpty()) {
                return "验证失败（" + groupName + "）：" + errorMsg;
            }
            return "验证失败（" + groupName + "）[" + fieldName + "]：" + errorMsg;
        }
        StringBuilder builder = new StringBuilder("验证失败（")
                .append(groupName)
                .append("，共 ")
                .append(verifyResult.getErrors().size())
                .append(" 项）：\n");
        for (FieldVerifyError error : verifyResult.getErrors()) {
            builder.append(error.getFieldName()).append("：").append(error.getErrorMsg()).append('\n');
        }
        return builder.toString().trim();
    }

    private static void logVerifyErrors(VerifyResult verifyResult) {
        if (!verifyResult.getErrors().isEmpty()) {
            for (FieldVerifyError error : verifyResult.getErrors()) {
                LogUtil.logger("VerifyDemo", error.getFieldName() + " -> " + error.getErrorMsg());
            }
            return;
        }
        if (verifyResult.getFieldName() != null) {
            LogUtil.logger("VerifyDemo", "首个失败字段：" + verifyResult.getFieldName());
        }
    }
}
