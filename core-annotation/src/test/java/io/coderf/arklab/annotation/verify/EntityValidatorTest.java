package io.coderf.arklab.annotation.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.coderf.arklab.annotation.annotation.Valid;
import io.coderf.arklab.annotation.annotation.VerifyAnyOf;
import io.coderf.arklab.annotation.annotation.VerifyCrossField;
import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.annotation.VerifyWhen;
import io.coderf.arklab.annotation.bean.VerifyResult;
import io.coderf.arklab.annotation.enums.ConditionOperator;
import io.coderf.arklab.annotation.enums.CrossFieldOperator;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;

/**
 * EntityValidatorTest 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class EntityValidatorTest {

    @Test
    public void validate_nullEntity_shouldFail() {
        VerifyResult result = EntityValidator.validate(null);
        assertFalse(result.isOk());
        assertEquals("校验对象不能为空", result.getErrorMsg());
    }

    @Test
    public void validate_whenConditionNotMet_shouldSkipFieldValidation() {
        ConditionalForm form = new ConditionalForm();
        form.type = "0";
        form.spouseName = "";

        VerifyResult result = EntityValidator.validate(form);
        assertTrue(result.isOk());
    }

    @Test
    public void validate_whenConditionMetAndFieldInvalid_shouldFail() {
        ConditionalForm form = new ConditionalForm();
        form.type = "1";
        form.spouseName = "";

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("spouseName", result.getFieldName());
    }

    @Test
    public void validate_crossFieldGreaterThanOrEqual_shouldFailWhenLess() {
        DateRangeForm form = new DateRangeForm();
        form.startDate = "2024-01-10";
        form.endDate = "2024-01-01";

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("endDate", result.getFieldName());
    }

    @Test
    public void validate_crossFieldGreaterThanOrEqual_shouldPassWhenValid() {
        DateRangeForm form = new DateRangeForm();
        form.startDate = "2024-01-01";
        form.endDate = "2024-01-10";

        VerifyResult result = EntityValidator.validate(form);
        assertTrue(result.isOk());
    }

    @Test
    public void validateAll_shouldCollectMultipleErrors() {
        MultiErrorForm form = new MultiErrorForm();
        form.first = "";
        form.second = "";

        VerifyResult result = EntityValidator.validateAll(form);
        assertFalse(result.isOk());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    public void validate_parentField_shouldBeValidated() {
        ChildForm form = new ChildForm();
        form.baseField = "";

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("baseField", result.getFieldName());
    }

    @Test
    public void validate_idCardType_shouldWork() {
        IdForm form = new IdForm();
        form.idCard = "123";

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
    }

    @Test
    public void validate_in_shouldPassWhenValueInList() {
        StatusForm form = new StatusForm();
        form.status = "2";

        VerifyResult result = EntityValidator.validate(form);
        assertTrue(result.isOk());
    }

    @Test
    public void validate_in_shouldFailWhenValueNotInList() {
        StatusForm form = new StatusForm();
        form.status = "9";

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("status", result.getFieldName());
    }

    @Test
    public void validate_notIn_shouldFailWhenValueInList() {
        BannedForm form = new BannedForm();
        form.status = "deleted";

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("status", result.getFieldName());
    }

    @Test
    public void validate_notIn_shouldPassWhenValueNotInList() {
        BannedForm form = new BannedForm();
        form.status = "active";

        VerifyResult result = EntityValidator.validate(form);
        assertTrue(result.isOk());
    }

    @Test
    public void validate_collectionSize_shouldFailWhenBelowMin() {
        TagForm form = new TagForm();
        form.tags = new ArrayList<>();

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("tags", result.getFieldName());
    }

    @Test
    public void validate_collectionSize_shouldFailWhenAboveMax() {
        TagForm form = new TagForm();
        form.tags = List.of("a", "b", "c", "d");

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("tags", result.getFieldName());
    }

    @Test
    public void validate_collectionSize_shouldPassWithinRange() {
        TagForm form = new TagForm();
        form.tags = List.of("a", "b");

        VerifyResult result = EntityValidator.validate(form);
        assertTrue(result.isOk());
    }

    @Test
    public void validate_collectionSize_nullShouldFailWhenMinSizePositive() {
        TagForm form = new TagForm();

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("tags", result.getFieldName());
    }

    @Test
    public void validate_arraySize_shouldFailWhenAboveMax() {
        CodeForm form = new CodeForm();
        form.codes = new String[]{"1", "2", "3"};

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("codes", result.getFieldName());
    }

    @Test
    public void validate_anyOf_shouldFailWhenAllBlank() {
        ContactForm form = new ContactForm();
        form.phone = "  ";
        form.email = "";

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("phone", result.getFieldName());
        assertEquals("请填写手机或邮箱", result.getErrorMsg());
    }

    @Test
    public void validate_anyOf_shouldPassWhenOneFilled() {
        ContactForm form = new ContactForm();
        form.email = "a@b.com";

        VerifyResult result = EntityValidator.validate(form);
        assertTrue(result.isOk());
    }

    @Test
    public void validate_anyOf_shouldSkipWhenGroupNotMatch() {
        ContactForm form = new ContactForm();
        form.phone = "13800138000";
        form.nickname = "";

        VerifyResult defaultResult = EntityValidator.validate(form, VerifyGroup.Default.class);
        assertTrue(defaultResult.isOk());

        VerifyResult createResult = EntityValidator.validate(form, VerifyGroup.Create.class);
        assertFalse(createResult.isOk());
        assertEquals("nickname", createResult.getFieldName());
    }

    @Test
    public void validateAll_anyOf_shouldCollectEachRule() {
        ContactForm form = new ContactForm();

        VerifyResult result = EntityValidator.validateAll(form, VerifyGroup.Create.class);
        assertFalse(result.isOk());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    public void validate_anyOf_missingField_shouldFail() {
        BrokenAnyOfForm form = new BrokenAnyOfForm();

        VerifyResult result = EntityValidator.validate(form);
        assertFalse(result.isOk());
        assertEquals("missing", result.getFieldName());
    }

    @Test
    public void validate_trim_shouldIgnoreEdgeWhitespace() {
        PhoneForm raw = new PhoneForm();
        raw.mobile = " 13800138000 ";
        assertFalse(EntityValidator.validate(raw).isOk());

        TrimPhoneForm trimmed = new TrimPhoneForm();
        trimmed.mobile = " 13800138000 ";
        assertTrue(EntityValidator.validate(trimmed).isOk());
    }

    @Test
    public void validate_trim_lengthUsesTrimmedText() {
        NickForm raw = new NickForm();
        raw.nick = "  ab  ";
        assertFalse(EntityValidator.validate(raw).isOk());

        TrimNickForm trimmed = new TrimNickForm();
        trimmed.nick = "  ab  ";
        assertTrue(EntityValidator.validate(trimmed).isOk());
        assertEquals("  ab  ", trimmed.nick);
    }

    @VerifyEntity
    static class ConditionalForm {
        @VerifyWhen(refField = "type", operator = ConditionOperator.EQUALS, value = "1")
        @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "请填写配偶姓名")
        String spouseName;

        String type;
    }

    @VerifyEntity
    static class DateRangeForm {
        String startDate;

        @VerifyCrossField(refField = "startDate", operator = CrossFieldOperator.GREATER_THAN_OR_EQUAL,
                errorMsg = "结束日期不能早于开始日期")
        @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "结束日期不能为空")
        String endDate;
    }

    @VerifyEntity
    static class MultiErrorForm {
        @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "first 为空")
        String first;

        @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "second 为空")
        String second;
    }

    @VerifyEntity
    static class ParentForm {
        @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "base 为空")
        String baseField;
    }

    @VerifyEntity
    static class ChildForm extends ParentForm {
    }

    @VerifyEntity
    static class IdForm {
        @VerifyParams(type = VerifyType.ID_CARD, errorMsg = "身份证格式错误")
        String idCard;
    }

    @VerifyEntity
    static class StatusForm {
        @VerifyParams(type = VerifyType.IN, values = {"1", "2", "3"}, errorMsg = "状态不合法")
        String status;
    }

    @VerifyEntity
    static class BannedForm {
        @VerifyParams(type = VerifyType.NOT_IN, values = {"deleted", "banned"}, errorMsg = "状态不可用")
        String status;
    }

    @VerifyEntity
    static class TagForm {
        @Valid(minSize = 1, maxSize = 3, errorMsg = "标签数量须在 1 到 3 之间")
        List<String> tags;
    }

    @VerifyEntity
    static class CodeForm {
        @Valid(maxSize = 2, errorMsg = "编码最多 2 个")
        String[] codes;
    }

    @VerifyEntity
    @VerifyAnyOf(fields = {"phone", "email"}, group = {VerifyGroup.Default.class, VerifyGroup.Create.class},
            errorMsg = "请填写手机或邮箱")
    @VerifyAnyOf(fields = {"nickname"}, group = VerifyGroup.Create.class, errorMsg = "请填写昵称")
    static class ContactForm {
        String phone;
        String email;
        String nickname;
    }

    @VerifyEntity
    @VerifyAnyOf(fields = {"missing"}, errorMsg = "字段不存在")
    static class BrokenAnyOfForm {
        String name;
    }

    @VerifyEntity
    static class PhoneForm {
        @VerifyParams(type = VerifyType.MOBILE_PHONE, errorMsg = "手机号格式不正确")
        String mobile;
    }

    @VerifyEntity
    static class TrimPhoneForm {
        @VerifyParams(type = VerifyType.MOBILE_PHONE, trim = true, errorMsg = "手机号格式不正确")
        String mobile;
    }

    @VerifyEntity
    static class NickForm {
        @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, maxLength = 4, errorMsg = "昵称过长")
        String nick;
    }

    @VerifyEntity
    static class TrimNickForm {
        @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, maxLength = 4, trim = true, errorMsg = "昵称过长")
        String nick;
    }
}
