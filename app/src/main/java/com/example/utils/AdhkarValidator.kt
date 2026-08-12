package com.example.utils

import com.example.data.models.AdhkarItem

object AdhkarValidator {

    val VALID_CATEGORIES = setOf(
        "أذكار الصباح",
        "أذكار المساء",
        "أذكار بعد الصلاة",
        "أذكار النوم والاستيقاظ",
        "أذكار السفر والركوب",
        "أدعية من القرآن والسنة"
    )

    fun validateItem(item: AdhkarItem): Boolean {
        if (item.id <= 0) return false
        if (item.category.isBlank() || item.category !in VALID_CATEGORIES) return false
        if (item.textArabic.isBlank()) return false
        if (item.targetCount <= 0) return false
        if (item.reference.isBlank()) return false
        return true
    }

    fun validateDataset(items: List<AdhkarItem>): Boolean {
        if (items.isEmpty()) return false
        val seenIds = mutableSetOf<Int>()

        for (item in items) {
            if (!validateItem(item)) return false
            if (seenIds.contains(item.id)) return false
            seenIds.add(item.id)
        }
        return true
    }

    fun validateCategories(categories: List<String>, items: List<AdhkarItem>): Boolean {
        if (categories.isEmpty()) return false
        val itemCategories = items.map { it.category }.toSet()
        for (category in categories) {
            if (category !in VALID_CATEGORIES) return false
            if (category !in itemCategories) return false
        }
        return true
    }
}
