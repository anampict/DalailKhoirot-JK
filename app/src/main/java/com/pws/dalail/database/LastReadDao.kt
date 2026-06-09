package com.pws.dalail.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LastReadDao {

    /**
     * Simpan/update bab terakhir yang dibaca.
     * Selalu upsert ke id=1 sehingga hanya ada satu baris.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lastRead: LastReadEntity)

    /** Ambil data terakhir dibaca (nullable jika belum pernah membaca). */
    @Query("SELECT * FROM last_read WHERE id = 1 LIMIT 1")
    fun getLastRead(): Flow<LastReadEntity?>
}
