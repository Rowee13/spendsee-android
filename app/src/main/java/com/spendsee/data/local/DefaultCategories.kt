package com.spendsee.data.local

import com.spendsee.data.local.entities.Category
import java.util.UUID

object DefaultCategories {

    // Income Categories (7) - Matching iOS
    val incomeCategories = listOf(
        Category(
            id = UUID.randomUUID().toString(),
            name = "Salary",
            icon = "briefcase",
            colorHex = "#34C759",
            type = "income",
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Lottery",
            icon = "award",
            colorHex = "#FF9500",
            type = "income",
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Refunds",
            icon = "rotate_ccw",
            colorHex = "#5AC8FA",
            type = "income",
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Grants",
            icon = "gift",
            colorHex = "#AF52DE",
            type = "income",
            isDefault = true,
            sortOrder = 4
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Bank Interest",
            icon = "dollar_sign",
            colorHex = "#FFD60A",
            type = "income",
            isDefault = true,
            sortOrder = 5
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Investment",
            icon = "trending_up",
            colorHex = "#007AFF",
            type = "income",
            isDefault = true,
            sortOrder = 6
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Business",
            icon = "briefcase",
            colorHex = "#FF2D55",
            type = "income",
            isDefault = true,
            sortOrder = 7
        )
    )

    // Expense Categories (19) - Matching iOS
    val expenseCategories = listOf(
        Category(
            id = UUID.randomUUID().toString(),
            name = "Bills",
            icon = "file_text",
            colorHex = "#5AC8FA",
            type = "expense",
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Business",
            icon = "briefcase",
            colorHex = "#FF2D55",
            type = "expense",
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Car",
            icon = "truck",
            colorHex = "#007AFF",
            type = "expense",
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Clothing",
            icon = "shopping_bag",
            colorHex = "#FF9500",
            type = "expense",
            isDefault = true,
            sortOrder = 4
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Education",
            icon = "book",
            colorHex = "#34C759",
            type = "expense",
            isDefault = true,
            sortOrder = 5
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Gadgets",
            icon = "monitor",
            colorHex = "#8E8E93",
            type = "expense",
            isDefault = true,
            sortOrder = 6
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Entertainment",
            icon = "tv",
            colorHex = "#AF52DE",
            type = "expense",
            isDefault = true,
            sortOrder = 7
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Food",
            icon = "coffee",
            colorHex = "#FF3B30",
            type = "expense",
            isDefault = true,
            sortOrder = 8
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Loan Repayment",
            icon = "credit_card",
            colorHex = "#FF6482",
            type = "expense",
            isDefault = true,
            sortOrder = 9
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Grocery",
            icon = "shopping_cart",
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
            icon = "shield",
            colorHex = "#5AC8FA",
            type = "expense",
            isDefault = true,
            sortOrder = 12
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Shopping",
            icon = "shopping_bag",
            colorHex = "#FF2D55",
            type = "expense",
            isDefault = true,
            sortOrder = 13
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Sport",
            icon = "activity",
            colorHex = "#32D74B",
            type = "expense",
            isDefault = true,
            sortOrder = 14
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Tax",
            icon = "percent",
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
            icon = "truck",
            colorHex = "#FFD60A",
            type = "expense",
            isDefault = true,
            sortOrder = 17
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Travel",
            icon = "map",
            colorHex = "#5AC8FA",
            type = "expense",
            isDefault = true,
            sortOrder = 18
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Gift",
            icon = "gift",
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
