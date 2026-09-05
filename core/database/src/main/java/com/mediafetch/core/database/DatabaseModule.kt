package com.mediafetch.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MediaFetchDatabase {
        return Room.databaseBuilder(
            context,
            MediaFetchDatabase::class.java,
            "mediafetch_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: MediaFetchDatabase): DownloadDao = database.downloadDao()

    @Provides
    @Singleton
    fun provideMediaLibraryDao(database: MediaFetchDatabase): MediaLibraryDao = database.mediaLibraryDao()
}
