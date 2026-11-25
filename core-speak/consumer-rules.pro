# ============================================================================
# core-speak consumer 混淆规则（随 AAR 发布，宿主 App 开启 R8 时自动生效）
#
# 包结构（io.coderf.arklab.speak.*）：
#   SpeakHelper  按住说话绑定器（attach 任意 View）——推荐接入入口
#   callback     OnSpeakListener / OnSpeakHoldUiListener
#   widget       SpeakButton（便捷封装）/ Overlay / Waveform / SpeakerView
#   style        SpeakUiStyle
#   permission   SpeakPermissionConfig / SpeakPermissionDialog
#   model        SpeakFinishAction / SpeakHitZone / VoiceRecordResult
#   record       VoiceRecorder
#   file         VoiceFileStore / DefaultVoiceFileStore
#   db           VoiceMessage / Dao / Database / Repository
#
# 若宿主遇到 NoSuchMethod / ClassNotFound，可临时放开：
#   -keep class io.coderf.arklab.speak.** { *; }
# ============================================================================

-dontwarn io.coderf.arklab.speak.**

# ---------- 推荐入口 ----------
-keep class io.coderf.arklab.speak.SpeakHelper { *; }
-keep class io.coderf.arklab.speak.callback.** { *; }
-keep interface io.coderf.arklab.speak.callback.** { *; }

# ---------- 自定义 View：XML 反射构造 ----------
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

# ---------- Room Entity / Dao / Database ----------
-keep class io.coderf.arklab.speak.db.** { *; }
-keepclassmembers class io.coderf.arklab.speak.db.** {
    <fields>;
    <init>(...);
}
