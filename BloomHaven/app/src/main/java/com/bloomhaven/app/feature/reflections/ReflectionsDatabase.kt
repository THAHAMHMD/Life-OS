package com.bloomhaven.app.feature.reflections

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(entities = [ReflectionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class, MoodConverters::class)
abstract class ReflectionsDatabase : RoomDatabase() {
    abstract fun reflectionsDao(): ReflectionsDao

    companion object {
        @Volatile private var instance: ReflectionsDatabase? = null

        fun getInstance(context: Context): ReflectionsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReflectionsDatabase::class.java,
                    "bloom_reflections.db",
                ).build().also { instance = it }
            }
    }
}
