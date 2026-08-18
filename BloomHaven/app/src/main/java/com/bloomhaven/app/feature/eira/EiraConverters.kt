package com.bloomhaven.app.feature.eira

import androidx.room.TypeConverter

class EiraConverters {
    @TypeConverter
    fun fromKind(value: EiraMessageKind): String = value.name

    @TypeConverter
    fun toKind(value: String): EiraMessageKind = EiraMessageKind.valueOf(value)
}
