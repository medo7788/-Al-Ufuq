package com.example.data.models

/**
 * A category from كتاب حصن المسلم (Hisn Al-Muslim) — e.g. "أذكار الصباح والمساء",
 * "دعاء دخول المسجد". Content sourced from a verified, openly-licensed transcription
 * of the book (see assets/hisn_almuslim.json); never AI-generated or paraphrased.
 */
data class HisnCategory(
    val id: Int,
    val category: String,
    val items: List<HisnItem>
)

data class HisnItem(
    val id: Int,
    val text: String,
    val count: Int
)
