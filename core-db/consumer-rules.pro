# core-db：Room Entity / DAO / Database / 请求配置
-keep class io.coderf.arklab.common.dao.** { *; }
-keep class io.coderf.arklab.common.database.** { *; }
-keep class io.coderf.arklab.common.converter.** { *; }
-keep class io.coderf.arklab.common.annotation.** { *; }
-keep class io.coderf.arklab.common.repository.Room** { *; }
-keep class io.coderf.arklab.common.repository.Attachment** { *; }
-keep class io.coderf.arklab.core.db.** { *; }

# 从 core-base 迁入的 Room 请求配置（含 Builder 内部类）
-keep class io.coderf.arklab.common.bean.RoomRequestOptions { *; }
-keep class io.coderf.arklab.common.bean.RoomRequestOptions$* { *; }

# 业务侧继承的 Dao / Repository，避免 R8 裁掉抽象方法
-keep class * extends io.coderf.arklab.common.dao.BaseRoomDao { *; }
-keep class * extends io.coderf.arklab.common.repository.RoomRepositoryImpl { *; }

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-dontwarn androidx.room.paging.**
