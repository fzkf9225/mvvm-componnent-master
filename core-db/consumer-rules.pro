# core-db：Room Entity / DAO / Database
-keep class io.coderf.arklab.common.dao.** { *; }
-keep class io.coderf.arklab.common.database.** { *; }
-keep class io.coderf.arklab.common.converter.** { *; }
-keep class io.coderf.arklab.common.repository.Room** { *; }
-keep class io.coderf.arklab.common.repository.Attachment** { *; }
-keep class io.coderf.arklab.core.db.** { *; }

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**
