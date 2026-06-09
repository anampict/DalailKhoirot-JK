package com.pws.dalail.pdf

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pws.dalail.database.AppDatabase
import com.pws.dalail.database.BookmarkEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel untuk fitur Bookmark.
 *
 * Menyediakan:
 * - [allBookmarks] → daftar semua bookmark (reaktif via Flow)
 * - [toggleBookmark] → tambah atau hapus bookmark
 * - [isBookmarkedFlow] → cek status bookmark untuk satu bab
 */
class BookmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).bookmarkDao()

    /** Semua bookmark yang tersimpan, diurutkan dari terbaru. */
    val allBookmarks: StateFlow<List<BookmarkEntity>> = dao
        .getAllBookmarks()
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = emptyList()
        )

    /**
     * Toggle bookmark untuk sebuah bab.
     * Jika sudah ada → hapus. Jika belum ada → tambah.
     */
    fun toggleBookmark(
        chapterNumber : Int,
        titleArabic   : String,
        startPage     : Int,
        endPage       : Int,
        isCurrentlyBookmarked: Boolean
    ) {
        viewModelScope.launch {
            if (isCurrentlyBookmarked) {
                dao.delete(chapterNumber)
            } else {
                dao.insert(
                    BookmarkEntity(
                        chapterNumber = chapterNumber,
                        titleArabic   = titleArabic,
                        startPage     = startPage,
                        endPage       = endPage
                    )
                )
            }
        }
    }

    /** Hapus bookmark berdasarkan nomor bab. */
    fun removeBookmark(chapterNumber: Int) {
        viewModelScope.launch {
            dao.delete(chapterNumber)
        }
    }

    /** Flow status bookmark untuk satu bab tertentu (dipakai di PdfReaderScreen). */
    fun isBookmarkedFlow(chapterNumber: Int) = dao.isBookmarked(chapterNumber)
}
