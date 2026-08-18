package com.bloomhaven.app.feature.petrova

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(
    entities = [
        PetrovaProfileEntity::class,
        PetrovaFeedingEntity::class,
        PetrovaGroomingEntity::class,
        PetrovaVetVisitEntity::class,
        PetrovaMedicineEntity::class,
        PetrovaWeightEntity::class,
        PetrovaExpenseEntity::class,
        PetrovaMemoryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class PetrovaDatabase : RoomDatabase() {
    abstract fun petrovaDao(): PetrovaDao

    companion object {
        @Volatile private var instance: PetrovaDatabase? = null

        fun getInstance(context: Context): PetrovaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PetrovaDatabase::class.java,
                    "bloom_petrova.db",
                ).build().also { instance = it }
            }
    }
}
