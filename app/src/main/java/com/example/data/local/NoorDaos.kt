package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE type = :type ORDER BY createdAt DESC")
    fun observeByType(type: String): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun observeExists(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    suspend fun exists(id: String): Boolean
}

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_sessions ORDER BY isDefault DESC, id ASC")
    fun observeSessions(): Flow<List<TasbihSessionEntity>>

    @Query("SELECT * FROM tasbih_history ORDER BY dateKey DESC LIMIT 30")
    fun observeHistory(): Flow<List<TasbihHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<TasbihSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TasbihSessionEntity): Long

    @Update
    suspend fun updateSession(session: TasbihSessionEntity)

    @Query("DELETE FROM tasbih_sessions WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustomSession(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: TasbihHistoryEntity)

    @Query("SELECT * FROM tasbih_history WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getHistoryForDate(dateKey: String): TasbihHistoryEntity?

    @Query("SELECT COUNT(*) FROM tasbih_sessions")
    suspend fun sessionCount(): Int
}

@Dao
interface FastingDao {
    @Query("SELECT * FROM fasting_days ORDER BY dateKey ASC")
    fun observeAll(): Flow<List<FastingDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FastingDayEntity)
}

@Dao
interface PersonalNoteDao {
    @Query("SELECT * FROM personal_notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PersonalNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: PersonalNoteEntity)

    @Query("DELETE FROM personal_notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RecentItemDao {
    @Query("SELECT * FROM recent_items ORDER BY viewedAt DESC LIMIT 15")
    fun observeRecents(): Flow<List<RecentItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RecentItemEntity)

    @Query("DELETE FROM recent_items")
    suspend fun clearAll()
}

@Dao
interface TafsirCacheDao {
    @Query("SELECT * FROM cached_tafsir WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun observeBySurah(surahNumber: Int): Flow<List<CachedTafsirEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedTafsirEntity>)

    @Query("SELECT COUNT(*) FROM cached_tafsir WHERE surahNumber = :surahNumber")
    suspend fun countForSurah(surahNumber: Int): Int

    @Query("DELETE FROM cached_tafsir")
    suspend fun clearAll()
}

@Dao
interface HadithCacheDao {
    @Query("SELECT * FROM cached_hadith ORDER BY hadithNumber ASC")
    fun observeAll(): Flow<List<CachedHadithEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedHadithEntity>)

    @Query("SELECT COUNT(*) FROM cached_hadith")
    suspend fun count(): Int

    @Query("DELETE FROM cached_hadith")
    suspend fun clearAll()
}

@Dao
interface AudioDownloadDao {
    @Query("SELECT * FROM downloaded_audio ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadedAudioEntity>>

    @Query("SELECT * FROM downloaded_audio WHERE reciterId = :reciterId AND surahNumber = :surahNumber LIMIT 1")
    suspend fun find(reciterId: String, surahNumber: Int): DownloadedAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedAudioEntity)

    @Query("DELETE FROM downloaded_audio WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloaded_audio")
    suspend fun clearAll()
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY attemptedAt DESC LIMIT 20")
    fun observeAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuizAttemptEntity)
}
