package com.spendsee.data.local

import com.spendsee.data.local.entities.Category
import java.util.UUID

object DefaultCategories {

    // Income Categories (7)
    val incomeCategories = listOf(
        Category(
            id = UUID.randomUUID().toString(),
            name = "Salary",
            icon = "briefcase",
            colorHex = "#34C759",
            type = "income",
            isDefault = true,
            sortOrder = 0
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Business",
            icon = "trending_up",
            colorHex = "#32ADE6",
            type = "income",
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Investment",
            icon = "dollar_sign",
            colorHex = "#FFD60A",
            type = "income",
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Gifts",
            icon = "gift",
            colorHex = "#FF375F",
            type = "income",
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Refunds",
            icon = "refresh_cw",
            colorHex = "#FF9F0A",
            type = "income",
            isDefault = true,
            sortOrder = 4
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Rental",
            icon = "home",
            colorHex = "#BF5AF2",
            type = "income",
            isDefault = true,
            sortOrder = 5
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Other Income",
            icon = "plus_circle",
            colorHex = "#64D2FF",
            type = "income",
            isDefault = true,
            sortOrder = 6
        )
    )

    // Expense Categories (19)
    val expenseCategories = listOf(
        Category(
            id = UUID.randomUUID().toString(),
            name = "Food & Dining",
            icon = "coffee",
            colorHex = "#FF9500",
            type = "expense",
            isDefault = true,
            sortOrder = 0
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Transport",
            icon = "truck",
            colorHex = "#007AFF",
            type = "expense",
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Shopping",
            icon = "shopping_bag",
            colorHex = "#AF52DE",
            type = "expense",
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Entertainment",
            icon = "film",
            colorHex = "#FF2D55",
            type = "expense",
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Bills & Utilities",
            icon = "file_text",
            colorHex = "#FF3B30",
            type = "expense",
            isDefault = true,
            sortOrder = 4
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Healthcare",
            icon = "heart",
            colorHex = "#34C759",
            type = "expense",
            isDefault = true,
            sortOrder = 5
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Education",
            icon = "book",
            colorHex = "#5856D6",
            type = "expense",
            isDefault = true,
            sortOrder = 6
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Travel",
            icon = "map",
            colorHex = "#32ADE6",
            type = "expense",
            isDefault = true,
            sortOrder = 7
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Housing",
            icon = "home",
            colorHex = "#8E8E93",
            type = "expense",
            isDefault = true,
            sortOrder = 8
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Personal Care",
            icon = "user",
            colorHex = "#FF6482",
            type = "expense",
            isDefault = true,
            sortOrder = 9
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Insurance",
            icon = "shield",
            colorHex = "#30B0C7",
            type = "expense",
            isDefault = true,
            sortOrder = 10
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Subscriptions",
            icon = "repeat",
            colorHex = "#AC8E68",
            type = "expense",
            isDefault = true,
            sortOrder = 11
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Gifts & Donations",
            icon = "gift",
            colorHex = "#FF375F",
            type = "expense",
            isDefault = true,
            sortOrder = 12
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Pet Care",
            icon = "heart",
            colorHex = "#FFD60A",
            type = "expense",
            isDefault = true,
            sortOrder = 13
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Fitness",
            icon = "activity",
            colorHex = "#FF453A",
            type = "expense",
            isDefault = true,
            sortOrder = 14
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Maintenance",
            icon = "tool",
            colorHex = "#64D2FF",
            type = "expense",
            isDefault = true,
            sortOrder = 15
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Taxes",
            icon = "percent",
            colorHex = "#BF5AF2",
            type = "expense",
            isDefault = true,
            sortOrder = 16
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Investments",
            icon = "trending_up",
            colorHex = "#FFD60A",
            type = "expense",
            isDefault = true,
            sortOrder = 17
        ),
        Category(
            id = UUID.randomUUID().toString(),
            name = "Other Expenses",
            icon = "more_horizontal",
            colorHex = "#8E8E93",
            type = "expense",
            isDefault = true,
            sortOrder = 18
        )
    )

    fun getAllDefaultCategories(): List<Category> {
        return incomeCategories + expenseCategories
    }
}
