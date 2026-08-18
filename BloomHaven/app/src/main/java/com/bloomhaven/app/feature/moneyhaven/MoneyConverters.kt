package com.bloomhaven.app.feature.moneyhaven

import androidx.room.TypeConverter

class MoneyConverters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
