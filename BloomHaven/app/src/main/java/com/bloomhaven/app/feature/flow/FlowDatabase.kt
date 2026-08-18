package com.bloomhaven.app.feature.flow

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(entities = [WaterLogEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun flowDao(): FlowDao

    companion object {
        @Volatile private var instance: FlowDatabase? = null

        fun getInstance(context: Context): FlowDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlowDatabase::class.java,
                    "bloom_flow.db",
                ).build().also { instance = it }
            }
    }
}
