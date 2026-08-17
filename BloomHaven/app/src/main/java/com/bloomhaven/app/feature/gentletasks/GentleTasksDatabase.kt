package com.bloomhaven.app.feature.gentletasks

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GentleTasksDatabase : RoomDatabase() {
    abstract fun gentleTasksDao(): GentleTasksDao

    companion object {
        @Volatile private var instance: GentleTasksDatabase? = null

        fun getInstance(context: Context): GentleTasksDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GentleTasksDatabase::class.java,
                    "bloom_gentle_tasks.db",
                ).build().also { instance = it }
            }
    }
}
