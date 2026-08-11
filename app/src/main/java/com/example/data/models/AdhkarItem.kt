package com.example.data.models

data class AdhkarItem(
    val id: Int,
    val category: String, // "أذكار الصباح", "أذكار المساء", "أذكار الصلاة", "أذكار النوم", "أذكار السفر"
    val textArabic: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val reference: String = "",
    val benefit: String = ""
)

data class AdhkarCategory(
    val titleArabic: String,
    val itemCount: Int,
    val estimatedDurationMinutes: Int,
    val iconName: String,
    val isCompletedToday: Boolean = false
)
