package io.coderf.arklab.common.utils.log;

/**
 * 本地日志读写层级
 *
 * @author fz
 * @since 1.0
 */
public enum FileLogLevel {
    /**
     * 不读写本地日志
     */
    NONE,
    /**
     * 调试模式：D / I / W / E
     */
    DEBUG,
    /**
     * 测试模式：I / W / E
     */
    TEST,
    /**
     * 发布模式：W / E
     */
    RELEASE;

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
