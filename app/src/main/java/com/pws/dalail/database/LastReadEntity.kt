package com.pws.dalail.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room untuk menyimpan bab terakhir yang dibaca.
 * Hanya ada satu baris (id = 1) yang selalu di-upsert (update/insert).
 */
@Entity(tableName = "last_read")
data class LastReadEntity(
    @PrimaryKey
    val id            : Int = 1,
    val chapterNumber : Int,
    val titleArabic   : String,
    val startPage     : Int,
    val endPage       : Int,
    val readAt        : Long = System.currentTimeMillis()
)
