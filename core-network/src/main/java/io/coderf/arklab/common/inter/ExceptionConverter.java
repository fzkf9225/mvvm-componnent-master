package io.coderf.arklab.common.inter;

import io.coderf.arklab.common.base.BaseException;

/**
 * ExceptionConverter 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/10/11 10:41
 */
public interface ExceptionConverter {
    BaseException convert(Throwable e);
}
