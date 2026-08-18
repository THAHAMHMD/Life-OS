package com.bloomhaven.app.feature.healthhaven

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(
    entities = [
        WorkoutEntity::class,
        SleepEntity::class,
        BodyMeasurementEntity::class,
        MedicalAppointmentEntity::class,
        MedicineEntity::class,
        HealthNoteEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class HealthHavenDatabase : RoomDatabase() {
    abstract fun healthHavenDao(): HealthHavenDao

    companion object {
        @Volatile private var instance: HealthHavenDatabase? = null

        fun getInstance(context: Context): HealthHavenDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HealthHavenDatabase::class.java,
                    "bloom_health_haven.db",
                ).build().also { instance = it }
            }
    }
}
