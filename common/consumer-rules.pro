# common facade：桥接类 + 透出说明
# 实际 keep 规则主要由 api 依赖的 core-* consumer-rules 合并进来；
# 此处保留 facade 自身与历史包名兼容入口。

-keep class io.coderf.arklab.common.corebridge.** { *; }
-dontwarn io.coderf.arklab.common.corebridge.**

# 兼容仍按旧坐标只依赖 common AAR、且未拆 core 坐标的宿主
-keep class io.coderf.arklab.common.** { *; }
-dontwarn io.coderf.arklab.common.**
-keep class io.coderf.arklab.core.** { *; }
-dontwarn io.coderf.arklab.core.**
