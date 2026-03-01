package com.mikeisesele.clearr.ui.feature.budget.utils

data class BudgetCategoryPreset(
    val name: String,
    val icon: String,
    val colorToken: String
)

val budgetCategoryPresets = listOf(
    BudgetCategoryPreset("Housing", "🏠", "Violet"),
    BudgetCategoryPreset("Food", "🍔", "Orange"),
    BudgetCategoryPreset("Transport", "🚗", "Blue"),
    BudgetCategoryPreset("Savings", "💰", "Amber"),
    BudgetCategoryPreset("Entertainment", "🎬", "Purple"),
    BudgetCategoryPreset("Utilities", "💡", "Violet"),
    BudgetCategoryPreset("Shopping", "🛍", "Orange"),
    BudgetCategoryPreset("Education", "📚", "Blue"),
    BudgetCategoryPreset("Custom", "✦", "Teal")
)
