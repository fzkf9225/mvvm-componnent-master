package com.casic.otitan.common.inter;

import com.casic.otitan.common.base.BaseException;

/**
 * created by fz on 2025/10/11 10:41
 * describe:
 */
public interface ExceptionConverter {
    BaseException convert(Throwable e);
}

