package com.spendsee.managers

/**
 * Enum representing all features in the app
 * Premium features require purchase to unlock
 */
enum class Feature(val isPremium: Boolean, val displayName: String, val description: String) {
    // Premium Features
    CUSTOM_CATEGORIES(
        isPremium = true,
        displayName = "Custom Categories",
        description = "Create unlimited custom categories with personalized icons and colors"
    ),
    MARK_BUDGET_AS_PAID(
        isPremium = true,
        displayName = "Mark Budget as Paid",
        description = "Track budget payment status and completion dates"
    ),
    BUDGET_NOTIFICATIONS(
        isPremium = true,
        displayName = "Budget Notifications",
        description = "Get reminded about upcoming budget payments"
    ),
    PASSCODE_PROTECTION(
        isPremium = true,
        displayName = "Passcode Protection",
        description = "Secure your financial data with passcode and biometric authentication"
    ),
    RECEIPT_SCANNING(
        isPremium = true,
        displayName = "Receipt Scanning",
        description = "Scan receipts with your camera to automatically create transactions"
    ),
    WIDGETS(
        isPremium = true,
        displayName = "Home Screen Widgets",
        description = "View your balance and budgets right from your home screen"
    ),
    PREMIUM_THEMES(
        isPremium = true,
        displayName = "Premium Themes",
        description = "Unlock 7 beautiful color themes: Ocean, Sunset, Forest, Lavender, Mint, Rose, and Monochrome"
    ),
    EXPORT_DATA(
        isPremium = true,
        displayName = "Export Data",
        description = "Export your financial data as JSON for backup and analysis"
    ),

    // Free Features
    BASIC_TRANSACTIONS(
        isPremium = false,
        displayName = "Transactions",
        description = "Track income, expenses, and transfers"
    ),
    BASIC_BUDGETS(
        isPremium = false,
        displayName = "Budgets",
        description = "Create and manage monthly budgets"
    ),
    ACCOUNTS(
        isPremium = false,
        displayName = "Accounts",
        description = "Manage multiple accounts with real-time balance tracking"
    ),
    ANALYTICS(
        isPremium = false,
        displayName = "Analytics",
        description = "View spending analytics and budget performance"
    ),
    DEFAULT_CATEGORIES(
        isPremium = false,
        displayName = "Default Categories",
        description = "Use 26 pre-defined income and expense categories"
    );

    companion object {
        fun getPremiumFeatures(): List<Feature> = values().filter { it.isPremium }
        fun getFreeFeatures(): List<Feature> = values().filter { !it.isPremium }
    }
}
