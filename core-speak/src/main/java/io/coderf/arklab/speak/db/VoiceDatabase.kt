package io.coderf.arklab.speak.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.coderf.arklab.core.db.RoomLog

/**
 * 语音模块独立 Room 库（与业务库隔离，可被宿主 [VoiceMessageRepository] 复用）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
@Database(entities = [VoiceMessage::class], version = 1, exportSchema = false)
abstract class VoiceDatabase : RoomDatabase() {

    abstract fun voiceMessageDao(): VoiceMessageDao

    companion object {
        @Volatile
        private var instance: VoiceDatabase? = null

        private const val DB_NAME = "io_coderf_arklab_speak_voice"

        @JvmStatic
        fun getInstance(context: Context): VoiceDatabase {
            return instance ?: synchronized(this) {
                instance ?: RoomLog.attachTo(
                    Room.databaseBuilder(
                        context.applicationContext,
                        VoiceDatabase::class.java,
                        DB_NAME
                    ).fallbackToDestructiveMigration()
                ).build().also { instance = it }
            }
        }
    }
}
