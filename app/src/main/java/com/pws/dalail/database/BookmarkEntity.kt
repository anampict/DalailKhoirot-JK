package com.pws.dalail.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room untuk menyimpan bookmark bab PDF.
 * Setiap baris mewakili satu bab yang di-bookmark.
 */
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val chapterNumber : Int,
    val titleArabic   : String,
    val startPage     : Int,
    val endPage       : Int,
    val savedAt       : Long = System.currentTimeMillis()
)
