package io.coderf.arklab.ui.form;

/**
 * 表单行校验契约。{@link FormConstraintLayout} 与 {@link FormMedia} 均实现，
 * 供 {@link FormHost#validateAll} 统一遍历。
 */
public interface FormValidatable {

    /**
     * @return 通过为 true；失败时实现方应已 {@code setError} 并返回 false
     */
    boolean validate();
}
