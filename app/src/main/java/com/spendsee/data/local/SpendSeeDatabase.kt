package com.spendsee.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendsee.data.local.dao.*
import com.spendsee.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaction::class,
        Account::class,
        Budget::class,
        BudgetItem::class,
        Category::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpendSeeDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun budgetItemDao(): BudgetItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: SpendSeeDatabase? = null

        fun getDatabase(context: Context): SpendSeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendSeeDatabase::class.java,
                    "spendsee_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(context: Context): SpendSeeDatabase = getDatabase(context)
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        suspend fun seedDatabase(database: SpendSeeDatabase) {
            val categoryDao = database.categoryDao()

            // Default Income Categories (7)
            val incomeCategories = listOf(
                Category(
                    name = "Salary",
                    icon = "payments",
                    colorHex = "#34C759",
                    type = "income",
                    isDefault = true,
                    sortOrder = 0
                ),
                Category(
                    name = "Lottery",
                    icon = "casino",
                    colorHex = "#FF9500",
                    type = "income",
                    isDefault = true,
                    sortOrder = 1
                ),
                Category(
                    name = "Refunds",
                    icon = "money_back",
                    colorHex = "#5AC8FA",
                    type = "income",
                    isDefault = true,
                    sortOrder = 2
                ),
                Category(
                    name = "Gift Received",
                    icon = "card_giftcard",
                    colorHex = "#FF2D55",
                    type = "income",
                    isDefault = true,
                    sortOrder = 3
                ),
                Category(
                    name = "Allowance",
                    icon = "account_balance_wallet",
                    colorHex = "#AF52DE",
                    type = "income",
                    isDefault = true,
                    sortOrder = 4
                ),
                Category(
                    name = "Awards",
                    icon = "emoji_events",
                    colorHex = "#FFD60A",
                    type = "income",
                    isDefault = true,
                    sortOrder = 5
                ),
                Category(
                    name = "Other Income",
                    icon = "add_circle",
                    colorHex = "#8E8E93",
                    type = "income",
                    isDefault = true,
                    sortOrder = 6
                )
            )

            // Default Expense Categories (19)
            val expenseCategories = listOf(
                Category(
                    name = "Bills",
                    icon = "receipt_long",
                    colorHex = "#FF3B30",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 0
                ),
                Category(
                    name = "Food",
                    icon = "restaurant",
                    colorHex = "#FF9500",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 1
                ),
                Category(
                    name = "Transport",
                    icon = "directions_car",
                    colorHex = "#007AFF",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 2
                ),
                Category(
                    name = "Shopping",
                    icon = "shopping_bag",
                    colorHex = "#AF52DE",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 3
                ),
                Category(
                    name = "Entertainment",
                    icon = "movie",
                    colorHex = "#FF2D55",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 4
                ),
                Category(
                    name = "Healthcare",
                    icon = "local_hospital",
                    colorHex = "#34C759",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 5
                ),
                Category(
                    name = "Education",
                    icon = "school",
                    colorHex = "#5AC8FA",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 6
                ),
                Category(
                    name = "Personal Care",
                    icon = "spa",
                    colorHex = "#FF2D55",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 7
                ),
                Category(
                    name = "Housing",
                    icon = "home",
                    colorHex = "#FF9500",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 8
                ),
                Category(
                    name = "Utilities",
                    icon = "bolt",
                    colorHex = "#FFD60A",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 9
                ),
                Category(
                    name = "Insurance",
                    icon = "shield",
                    colorHex = "#007AFF",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 10
                ),
                Category(
                    name = "Travel",
                    icon = "flight",
                    colorHex = "#5AC8FA",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 11
                ),
                Category(
                    name = "Gift Given",
                    icon = "redeem",
                    colorHex = "#FF2D55",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 12
                ),
                Category(
                    name = "Sports",
                    icon = "sports_soccer",
                    colorHex = "#34C759",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 13
                ),
                Category(
                    name = "Pets",
                    icon = "pets",
                    colorHex = "#FF9500",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 14
                ),
                Category(
                    name = "Subscriptions",
                    icon = "subscriptions",
                    colorHex = "#AF52DE",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 15
                ),
                Category(
                    name = "Fees",
                    icon = "attach_money",
                    colorHex = "#FF3B30",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 16
                ),
                Category(
                    name = "Taxes",
                    icon = "account_balance",
                    colorHex = "#8E8E93",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 17
                ),
                Category(
                    name = "Other Expenses",
                    icon = "more_horiz",
                    colorHex = "#8E8E93",
                    type = "expense",
                    isDefault = true,
                    sortOrder = 18
                )
            )

            // Insert all default categories
            categoryDao.insertAll(incomeCategories + expenseCategories)
        }
    }
}
