package io.coderf.arklab.log;

import androidx.annotation.Nullable;

/**
 * Logger 格式与本地文件日志的全局配置。
 * <p>
 * 由宿主在 Application 中调用一次 {@link ArkLog#init(android.content.Context, LogConfig)}。
 * 各业务/框架模块只管理自己的 {@link LogChannel} debug 开关即可。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/21 8:52
 */
public final class LogConfig {
    private final boolean showThreadInfo;
    private final int methodCount;
    private final int methodOffset;
    private final String globalTag;
    private final FileLogLevel fileLogLevel;
    private final String fileNamePrefix;
    private final String fileDirName;

    private LogConfig(Builder builder) {
        this.showThreadInfo = builder.showThreadInfo;
        this.methodCount = builder.methodCount;
        this.methodOffset = builder.methodOffset;
        this.globalTag = builder.globalTag;
        this.fileLogLevel = builder.fileLogLevel;
        this.fileNamePrefix = builder.fileNamePrefix;
        this.fileDirName = builder.fileDirName;
    }

    /** @return 是否显示线程信息 */
    public boolean isShowThreadInfo() {
        return showThreadInfo;
    }

    /** @return 堆栈方法层数 */
    public int getMethodCount() {
        return methodCount;
    }

    /** @return 跳过封装层数 */
    public int getMethodOffset() {
        return methodOffset;
    }

    /** @return 全局默认 tag */
    public String getGlobalTag() {
        return globalTag;
    }

    /** @return 初始化时的落盘层级 */
    public FileLogLevel getFileLogLevel() {
        return fileLogLevel;
    }

    /** @return 落盘文件名前缀 */
    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    /** @return 落盘子目录名 */
    public String getFileDirName() {
        return fileDirName;
    }

    /** @return 新 Builder */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean showThreadInfo = true;
        private int methodCount = 0;
        private int methodOffset = 0;
        private String globalTag = "ArkLab";
        private FileLogLevel fileLogLevel = FileLogLevel.NONE;
        private String fileNamePrefix = "log";
        private String fileDirName = "log";

        /**
         * @param showThreadInfo 是否显示线程信息，默认 {@code true}
         */
        public Builder showThreadInfo(boolean showThreadInfo) {
            this.showThreadInfo = showThreadInfo;
            return this;
        }

        /**
         * @param methodCount 堆栈方法层数，默认 {@code 0}
         */
        public Builder methodCount(int methodCount) {
            this.methodCount = methodCount;
            return this;
        }

        /**
         * @param methodOffset 跳过封装层数，默认 {@code 0}
         */
        public Builder methodOffset(int methodOffset) {
            this.methodOffset = methodOffset;
            return this;
        }

        /**
         * @param globalTag 全局默认 tag，空则保持原值
         */
        public Builder globalTag(@Nullable String globalTag) {
            if (globalTag != null && !globalTag.trim().isEmpty()) {
                this.globalTag = globalTag.trim();
            }
            return this;
        }

        /**
         * 初始化时是否自动开启本地文件日志；默认 {@link FileLogLevel#NONE}。
         *
         * @param fileLogLevel 落盘层级，{@code null} 视为 NONE
         */
        public Builder fileLogLevel(@Nullable FileLogLevel fileLogLevel) {
            this.fileLogLevel = fileLogLevel == null ? FileLogLevel.NONE : fileLogLevel;
            return this;
        }

        /**
         * 本地日志文件名前缀，最终文件形如 {@code prefix-yyyy-MM-dd.log}。
         *
         * @param fileNamePrefix 前缀，空则保持原值
         */
        public Builder fileNamePrefix(@Nullable String fileNamePrefix) {
            if (fileNamePrefix != null && !fileNamePrefix.trim().isEmpty()) {
                this.fileNamePrefix = fileNamePrefix.trim();
            }
            return this;
        }

        /**
         * 相对 externalCacheDir 的子目录名，默认 {@code log}。
         *
         * @param fileDirName 子目录名，空则保持原值
         */
        public Builder fileDirName(@Nullable String fileDirName) {
            if (fileDirName != null && !fileDirName.trim().isEmpty()) {
                this.fileDirName = fileDirName.trim();
            }
            return this;
        }

        /** @return 不可变配置 */
        public LogConfig build() {
            return new LogConfig(this);
        }
    }
}
