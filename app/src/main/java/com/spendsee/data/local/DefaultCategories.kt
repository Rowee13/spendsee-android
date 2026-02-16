package com.spendsee.data.local

import com.spendsee.data.local.entities.Category
import java.util.UUID

object DefaultCategories {

    // Income Categories (7) - Unique Material Icons matching iOS
    val incomeCategories = listOf(
        Category(
            id = UUID.randomUUID().toString(),
            name = "Salary",
            icon = "work",
            colorHex = "#34C759",
            type = "income",
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Lottery",
            icon = "emojievents",
            colorHex = "#FF9500",
            type = "income",
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Refunds",
            icon = "assignmentreturn",
            colorHex = "#5AC8FA",
            type = "income",
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Grants",
            icon = "volunteeractivism",
            colorHex = "#AF52DE",
            type = "income",
            isDefault = true,
            sortOrder = 4
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Bank Interest",
            icon = "accountbalance",
            colorHex = "#FFD60A",
            type = "income",
            isDefault = true,
            sortOrder = 5
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Investment",
            icon = "trendingup",
            colorHex = "#007AFF",
            type = "income",
            isDefault = true,
            sortOrder = 6
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Business",
            icon = "businesscenter",
            colorHex = "#FF2D55",
            type = "income",
            isDefault = true,
            sortOrder = 7
        )
    )

    // Expense Categories (19) - Unique Material Icons matching iOS
    val expenseCategories = listOf(
        Category(
            id = UUID.randomUUID().toString(),
            name = "Bills",
            icon = "receipt",
            colorHex = "#5AC8FA",
            type = "expense",
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Business",
            icon = "store",
            colorHex = "#FF2D55",
            type = "expense",
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Car",
            icon = "directionscar",
            colorHex = "#007AFF",
            type = "expense",
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Clothing",
            icon = "checkroom",
            colorHex = "#FF9500",
            type = "expense",
            isDefault = true,
            sortOrder = 4
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Education",
            icon = "school",
            colorHex = "#34C759",
            type = "expense",
            isDefault = true,
            sortOrder = 5
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Gadgets",
            icon = "laptop",
            colorHex = "#8E8E93",
            type = "expense",
            isDefault = true,
            sortOrder = 6
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Entertainment",
            icon = "movie",
            colorHex = "#AF52DE",
            type = "expense",
            isDefault = true,
            sortOrder = 7
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Food",
            icon = "restaurant",
            colorHex = "#FF3B30",
            type = "expense",
            isDefault = true,
            sortOrder = 8
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Loan Repayment",
            icon = "creditcard",
            colorHex = "#FF6482",
            type = "expense",
            isDefault = true,
            sortOrder = 9
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Grocery",
            icon = "shoppingcart",
            colorHex = "#34C759",
            type = "expense",
            isDefault = true,
            sortOrder = 10
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "House Rent",
            icon = "home",
            colorHex = "#FF9500",
            type = "expense",
            isDefault = true,
            sortOrder = 11
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Insurance",
            icon = "healthandsafety",
            colorHex = "#5AC8FA",
            type = "expense",
            isDefault = true,
            sortOrder = 12
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Shopping",
            icon = "shoppingbag",
            colorHex = "#FF2D55",
            type = "expense",
            isDefault = true,
            sortOrder = 13
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Sport",
            icon = "fitnesscenter",
            colorHex = "#32D74B",
            type = "expense",
            isDefault = true,
            sortOrder = 14
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Tax",
            icon = "calculate",
            colorHex = "#8E8E93",
            type = "expense",
            isDefault = true,
            sortOrder = 15
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Telephone",
            icon = "phone",
            colorHex = "#007AFF",
            type = "expense",
            isDefault = true,
            sortOrder = 16
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Transportation",
            icon = "directionstransit",
            colorHex = "#FFD60A",
            type = "expense",
            isDefault = true,
            sortOrder = 17
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Travel",
            icon = "flight",
            colorHex = "#5AC8FA",
            type = "expense",
            isDefault = true,
            sortOrder = 18
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Gift",
            icon = "cardgiftcard",
            colorHex = "#FF2D55",
            type = "expense",
            isDefault = true,
            sortOrder = 19
        )
    )

    fun getAllDefaultCategories(): List<Category> {
        return incomeCategories + expenseCategories
    }
}
