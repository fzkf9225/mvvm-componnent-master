# ============================================================================
# core-speak consumer 混淆规则（随 AAR 发布，宿主 App 开启 R8 时自动生效）
#
# 包结构（io.coderf.arklab.speak.*）：
#   widget     SpeakButton / SpeakRecordOverlay / WaveformView / SpeakerView（XML 可引用）
#   style      SpeakUiStyle 配置
#   permission SpeakPermissionConfig / SpeakPermissionDialog
#   model      SpeakFinishAction / SpeakHitZone / VoiceRecordResult
#   record     VoiceRecorder
#   file       VoiceFileStore / DefaultVoiceFileStore
#   db         VoiceMessage / Dao / Database / Repository（Room Entity）
#
# 若宿主遇到 NoSuchMethod / ClassNotFound，可临时放开：
#   -keep class io.coderf.arklab.speak.** { *; }
# ============================================================================

-dontwarn io.coderf.arklab.speak.**

# ---------- 自定义 View：XML 反射构造，必须保留 ----------
-keep public class io.coderf.arklab.speak.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public *;
}

# ---------- 对外样式 / 权限 / 模型 / 录音 / 文件 ----------
-keep class io.coderf.arklab.speak.style.** { *; }
-keep class io.coderf.arklab.speak.permission.** { *; }
-keep class io.coderf.arklab.speak.model.** { *; }
-keep class io.coderf.arklab.speak.record.** { *; }
-keep class io.coderf.arklab.speak.file.** { *; }
-keep interface io.coderf.arklab.speak.file.** { *; }

# ---------- Room Entity / Dao / Database（反射 + 生成代码） ----------
-keep class io.coderf.arklab.speak.db.** { *; }
-keepclassmembers class io.coderf.arklab.speak.db.** {
    <fields>;
    <init>(...);
}

# ---------- 回调接口（宿主实现） ----------
-keep interface io.coderf.arklab.speak.widget.SpeakButton$OnSpeakListener { *; }
-keepclassmembers class * implements io.coderf.arklab.speak.widget.SpeakButton$OnSpeakListener {
    <methods>;
}
