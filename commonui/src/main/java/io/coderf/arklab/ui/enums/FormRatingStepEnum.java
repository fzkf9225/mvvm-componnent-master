package io.coderf.arklab.ui.enums;

/**
 * FormRating 评分步进类型。
 */
public enum FormRatingStepEnum {
    INTEGER(0, 1f),
    HALF(1, 0.5f),
    ANY(2, 0.01f);

    public final int value;
    public final float stepSize;

    FormRatingStepEnum(int value, float stepSize) {
        this.value = value;
        this.stepSize = stepSize;
    }

    public static float resolveStepSize(int value) {
        for (FormRatingStepEnum item : values()) {
            if (item.value == value) {
                return item.stepSize;
            }
        }
        return HALF.stepSize;
    }
}
