package com.example.testmapsjob

import android.content.Context
import androidx.room.Room
import com.example.data.MapsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideMapsDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            MapsDatabase::class.java,
            "MapsDatabase"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideMapsDao(mapsDatabase: MapsDatabase) = mapsDatabase.mapsDao()
}