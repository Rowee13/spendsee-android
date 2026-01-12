package com.spendsee.data.models

enum class Feature(val displayName: String, val description: String, val isPremium: Boolean = true) {
    CUSTOM_CATEGORIES(
        "Custom Categories",
        "Create unlimited custom categories with unique icons",
        true
    ),
    MARK_BUDGET_AS_PAID(
        "Quick Pay Budget",
        "Mark budgets as paid with one tap",
        true
    ),
    BUDGET_NOTIFICATIONS(
        "Budget Notifications",
        "Get reminded of upcoming budget payments",
        true
    ),
    PASSCODE_PROTECTION(
        "Passcode Protection",
        "Secure your financial data with Face ID/Touch ID",
        true
    ),
    RECEIPT_SCANNING(
        "Receipt Scanning",
        "Capture and extract data from receipts automatically",
        true
    );

    companion object {
        fun getAllPremiumFeatures(): List<Feature> {
            return entries.filter { it.isPremium }
        }
    }
}
