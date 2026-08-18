package com.bloomhaven.app.feature.habits

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(entities = [HabitEntity::class, HabitLogEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HabitsDatabase : RoomDatabase() {
    abstract fun habitsDao(): HabitsDao

    companion object {
        @Volatile private var instance: HabitsDatabase? = null

        fun getInstance(context: Context): HabitsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitsDatabase::class.java,
                    "bloom_habits.db",
                ).build().also { instance = it }
            }
    }
}
