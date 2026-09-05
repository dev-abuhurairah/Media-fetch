package com.mediafetch.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mediafetch.core.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaLibraryDao {
    @Query("SELECT * FROM media_library ORDER BY addedAt DESC")
    fun getAllLibraryItems(): Flow<List<MediaLibraryEntity>>

    @Query("SELECT * FROM media_library WHERE mediaType = :type ORDER BY addedAt DESC")
    fun getLibraryItemsByType(type: MediaType): Flow<List<MediaLibraryEntity>>

    @Query("SELECT * FROM media_library WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteItems(): Flow<List<MediaLibraryEntity>>

    @Query("SELECT * FROM media_library WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY addedAt DESC")
    fun searchLibrary(query: String): Flow<List<MediaLibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaLibraryEntity)

    @Update
    suspend fun update(item: MediaLibraryEntity)

    @Query("DELETE FROM media_library WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM media_library")
    suspend fun clearAll()

    @Query("SELECT SUM(fileSizeBytes) FROM media_library")
    fun getTotalLibrarySizeBytes(): Flow<Long?>
}
