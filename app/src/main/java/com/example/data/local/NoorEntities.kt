package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // e.g., "ayah:1:1", "dua:morning_1", "hadith:bukhari_1", "name:1"
    val type: String, // AYAH, DUA, HADITH, NAME_OF_ALLAH, GUIDE
    val title: String,
    val subtitle: String,
    val arabicText: String = "",
    val routeHint: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbih_sessions")
data class TasbihSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleBn: String,
    val titleEn: String,
    val arabicText: String,
    val pronunciationBn: String,
    val meaningBn: String,
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val totalLifetimeCount: Long = 0,
    val isDefault: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbih_history")
data class TasbihHistoryEntity(
    @PrimaryKey val dateKey: String, // "yyyy-MM-dd"
    val totalCount: Long = 0,
    val sessionsCompleted: Int = 0
)

@Entity(tableName = "fasting_days")
data class FastingDayEntity(
    @PrimaryKey val dateKey: String, // "yyyy-MM-dd" or "ramadan-day-01"
    val isFasted: Boolean = false,
    val fastingType: String = "RAMADAN", // RAMADAN, QADA, NAFL
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_notes")
data class PersonalNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "সাধারণ",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_items")
data class RecentItemEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val routeHint: String,
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_tafsir")
data class CachedTafsirEntity(
    @PrimaryKey val id: String, // "surah:ayah"
    val surahNumber: Int,
    val ayahNumber: Int,
    val sourceName: String,
    val tafsirText: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_hadith")
data class CachedHadithEntity(
    @PrimaryKey val id: String, // "bukhari:1"
    val bookId: String,
    val bookNameBn: String,
    val hadithNumber: Int,
    val categoryBn: String,
    val text: String,
    val referenceBn: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_audio")
data class DownloadedAudioEntity(
    @PrimaryKey val id: String, // "reciter:surah"
    val reciterId: String,
    val surahNumber: Int,
    val localPath: String,
    val sizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val totalQuestions: Int,
    val categoryBn: String,
    val attemptedAt: Long = System.currentTimeMillis()
)
