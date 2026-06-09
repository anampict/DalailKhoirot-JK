package com.pws.dalail.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    /** Tambah bookmark. Jika sudah ada (same chapterNumber), replace. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    /** Hapus bookmark berdasarkan nomor bab. */
    @Query("DELETE FROM bookmarks WHERE chapterNumber = :chapterNumber")
    suspend fun delete(chapterNumber: Int)

    /** Ambil semua bookmark, diurutkan dari paling baru. */
    @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    /** Cek apakah sebuah bab sudah di-bookmark (Flow reaktif). */
    @Query("SELECT COUNT(*) > 0 FROM bookmarks WHERE chapterNumber = :chapterNumber")
    fun isBookmarked(chapterNumber: Int): Flow<Boolean>
}
