package io.coderf.arklab.annotation.verify;

import org.junit.Test;

import io.coderf.arklab.annotation.annotation.VerifyCrossField;
import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyField;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.annotation.VerifyWhen;
import io.coderf.arklab.annotation.bean.VerifyResult;
import io.coderf.arklab.annotation.enums.ConditionOperator;
import io.coderf.arklab.annotation.enums.CrossFieldOperator;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}
