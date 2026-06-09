package com.pws.dalail.pdf

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pws.dalail.database.AppDatabase
import com.pws.dalail.database.LastReadEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel untuk fitur "Terakhir Dibaca" di Dashboard.
 *
 * Menyediakan:
 * - [lastRead] → data bab terakhir yang dibaca (reaktif via Flow)
 * - [saveLastRead] → simpan/update bab yang sedang dibuka
 */
class LastReadViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).lastReadDao()

    /** Data terakhir dibaca, null jika user belum pernah membuka bab apapun. */
    val lastRead: StateFlow<LastReadEntity?> = dao
        .getLastRead()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /**
     * Dipanggil saat user membuka sebuah bab (dari ChapterDetailScreen / PdfReaderScreen).
     * Selalu overwrite data lama karena hanya ada satu baris (id = 1).
     */
    fun saveLastRead(
        chapterNumber : Int,
        titleArabic   : String,
        startPage     : Int,
        endPage       : Int
    ) {
        viewModelScope.launch {
            dao.upsert(
                LastReadEntity(
                    chapterNumber = chapterNumber,
                    titleArabic   = titleArabic,
                    startPage     = startPage,
                    endPage       = endPage
                )
            )
        }
    }
}
