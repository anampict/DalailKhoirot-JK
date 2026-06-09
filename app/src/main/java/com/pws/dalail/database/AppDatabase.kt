package com.pws.dalail.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database singleton untuk aplikasi Dalailul Khairat.
 * Berisi tabel:
 *  - bookmarks  → bab-bab yang di-bookmark user
 *  - last_read  → satu bab terakhir yang dibuka user
 */
@Database(
    entities = [BookmarkEntity::class, LastReadEntity::class],
    version  = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun lastReadDao(): LastReadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dalail_khoirot.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
