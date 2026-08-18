package com.bloomhaven.app.feature.moneyhaven

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bloomhaven.app.core.data.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        TransactionAuditEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        InvestmentEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class, MoneyConverters::class)
abstract class MoneyDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsDao(): SavingsDao
    abstract fun debtDao(): DebtDao
    abstract fun investmentDao(): InvestmentDao

    companion object {
        @Volatile private var instance: MoneyDatabase? = null

        fun getInstance(context: Context): MoneyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoneyDatabase::class.java,
                    "bloom_money_haven.db",
                ).build().also {
                    instance = it
                    seedDefaultCategories(context, it)
                }
            }

        private fun seedDefaultCategories(context: Context, db: MoneyDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = db.categoryDao()
                DEFAULT_EXPENSE_CATEGORIES.forEach { name ->
                    if (dao.countByName(name, TransactionType.EXPENSE) == 0) {
                        dao.insert(CategoryEntity(name = name, type = TransactionType.EXPENSE, isDefault = true))
                    }
                }
                DEFAULT_INCOME_CATEGORIES.forEach { name ->
                    if (dao.countByName(name, TransactionType.INCOME) == 0) {
                        dao.insert(CategoryEntity(name = name, type = TransactionType.INCOME, isDefault = true))
                    }
                }
            }
        }
    }
}
