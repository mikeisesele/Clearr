package com.mikeisesele.clearr.ui.feature.budget

internal data class CategoryPreset(
    val name: String,
    val icon: String,
    val colorToken: String
)

internal val categoryPresets = listOf(
    CategoryPreset("Housing", "🏠", "Violet"),
    CategoryPreset("Food", "🍔", "Orange"),
    CategoryPreset("Transport", "🚗", "Blue"),
    CategoryPreset("Savings", "💰", "Amber"),
    CategoryPreset("Entertainment", "🎬", "Purple"),
    CategoryPreset("Utilities", "💡", "Violet"),
    CategoryPreset("Shopping", "🛍", "Orange"),
    CategoryPreset("Education", "📚", "Blue"),
    CategoryPreset("Custom", "✦", "Teal")
)
