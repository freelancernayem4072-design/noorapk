package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        TasbihSessionEntity::class,
        TasbihHistoryEntity::class,
        FastingDayEntity::class,
        PersonalNoteEntity::class,
        RecentItemEntity::class,
        CachedTafsirEntity::class,
        CachedHadithEntity::class,
        DownloadedAudioEntity::class,
        QuizAttemptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NoorDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun fastingDao(): FastingDao
    abstract fun personalNoteDao(): PersonalNoteDao
    abstract fun recentItemDao(): RecentItemDao
    abstract fun tafsirCacheDao(): TafsirCacheDao
    abstract fun hadithCacheDao(): HadithCacheDao
    abstract fun audioDownloadDao(): AudioDownloadDao
    abstract fun quizAttemptDao(): QuizAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: NoorDatabase? = null

        fun getInstance(context: Context): NoorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoorDatabase::class.java,
                    "noor_life_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { INSTANCE = it }
            }
        }
    }
}
