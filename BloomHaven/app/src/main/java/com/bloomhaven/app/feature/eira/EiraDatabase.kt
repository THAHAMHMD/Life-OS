package com.bloomhaven.app.feature.eira

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(entities = [EiraMessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class, EiraConverters::class)
abstract class EiraDatabase : RoomDatabase() {
    abstract fun eiraDao(): EiraDao

    companion object {
        @Volatile private var instance: EiraDatabase? = null

        fun getInstance(context: Context): EiraDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EiraDatabase::class.java,
                    "bloom_eira.db",
                ).build().also { instance = it }
            }
    }
}
