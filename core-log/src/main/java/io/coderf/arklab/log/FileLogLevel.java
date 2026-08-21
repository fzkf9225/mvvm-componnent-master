package io.coderf.arklab.log;

/**
 * 本地日志读写层级（进程级，由 {@link ArkLog} 统一管理）。
 *
 * <ul>
 *   <li>{@link #NONE}：不读写本地日志</li>
 *   <li>{@link #DEBUG}：D / I / W / E</li>
 *   <li>{@link #TEST}：I / W / E</li>
 *   <li>{@link #RELEASE}：W / E</li>
 * </ul>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/21 8:53
 */
public enum FileLogLevel {
    /** 不读写本地日志 */
    NONE,
    /** 调试：D / I / W / E */
    DEBUG,
    /** 测试：I / W / E */
    TEST,
    /** 发布：W / E */
    RELEASE;

    /**
     * 当前层级是否允许写入该 logcat 级别字符。
     *
     * @param level logcat 级别，如 {@code D}/{@code I}/{@code W}/{@code E}
     * @return 是否允许落盘
     */
    public boolean allows(String level) {
        if (level == null) {
            return false;
        }
        return switch (this) {
            case DEBUG ->
                    "D".equals(level) || "I".equals(level) || "W".equals(level) || "E".equals(level);
            case TEST -> "I".equals(level) || "W".equals(level) || "E".equals(level);
            case RELEASE -> "W".equals(level) || "E".equals(level);
            default -> false;
        };
    }
}
