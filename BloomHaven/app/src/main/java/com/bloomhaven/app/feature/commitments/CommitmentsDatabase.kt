package com.bloomhaven.app.feature.commitments

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters

@Database(
    entities = [CommitmentEntity::class, CommitmentPaymentEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class CommitmentsDatabase : RoomDatabase() {
    abstract fun commitmentsDao(): CommitmentsDao

    companion object {
        @Volatile private var instance: CommitmentsDatabase? = null

        fun getInstance(context: Context): CommitmentsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CommitmentsDatabase::class.java,
                    "bloom_commitments.db",
                ).build().also { instance = it }
            }
    }
}
