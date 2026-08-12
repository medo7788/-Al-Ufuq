package com.example.data.models

data class QuranSurah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val totalVerses: Int,
    val revelationType: String, // "مكية" or "مدنية"
    val juzNumber: Int = 1,
    val pageNumber: Int = 1,
    val ayahs: List<QuranVerse> = emptyList()
)

data class QuranVerse(
    val surahNumber: Int,
    val verseNumber: Int,
    val textArabic: String,
    val translationArabic: String = "",
    val tafsirShort: String = "",
    val juz: Int = 1,
    val page: Int = 1,
    val sajda: Boolean = false
)
