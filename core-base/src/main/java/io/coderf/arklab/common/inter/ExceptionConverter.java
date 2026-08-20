package io.coderf.arklab.common.inter;

import io.coderf.arklab.common.base.BaseException;

/**
 * created by fz on 2025/10/11 10:41
 * describe:
 */
public interface ExceptionConverter {
    BaseException convert(Throwable e);
}

