package com.mediafetch.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DownloadEntity::class, MediaLibraryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MediaFetchDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun mediaLibraryDao(): MediaLibraryDao
}
