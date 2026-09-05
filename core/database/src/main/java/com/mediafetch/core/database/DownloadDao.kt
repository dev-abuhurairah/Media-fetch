package com.mediafetch.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mediafetch.core.model.DownloadState
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY updatedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE state IN (:states) ORDER BY updatedAt DESC")
    fun getDownloadsByState(states: List<DownloadState>): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    fun observeDownloadById(id: String): Flow<DownloadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("UPDATE downloads SET state = :state, progress = :progress, downloadedBytes = :downloaded, totalBytes = :total, speedBytesPerSec = :speed, remainingSeconds = :eta, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProgress(
        id: String,
        state: DownloadState,
        progress: Float,
        downloaded: Long,
        total: Long,
        speed: Long,
        eta: Long,
        updatedAt: Long
    )

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloads WHERE state IN (:states)")
    suspend fun clearByStates(states: List<DownloadState>)

    @Query("SELECT COUNT(*) FROM downloads WHERE state = 'DOWNLOADING'")
    suspend fun getActiveDownloadCount(): Int
}
