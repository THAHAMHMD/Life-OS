package com.bloomhaven.app.feature.planner

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(entities = [PlannerItemEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        @Volatile private var instance: PlannerDatabase? = null

        fun getInstance(context: Context): PlannerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "bloom_planner.db",
                ).build().also { instance = it }
            }
    }
}
