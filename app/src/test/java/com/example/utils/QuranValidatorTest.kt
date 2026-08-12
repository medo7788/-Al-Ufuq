package com.example.utils

import com.example.data.models.QuranSurah
import com.example.data.models.QuranVerse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranValidatorTest {

    @Test
    fun `validateSurahCount returns false when count is not 114`() {
        val incompleteList = listOf(
            QuranSurah(1, "الفاتحة", "Al-Fatiha", 7, "مكية"),
            QuranSurah(2, "البقرة", "Al-Baqarah", 286, "مدنية")
        )
        assertFalse(QuranValidator.validateSurahCount(incompleteList))
    }

    @Test
    fun `validateSurahCount returns true for valid 114 surahs`() {
        val fullList = (1..114).map { num ->
            val expectedAyahs = QuranValidator.getExpectedAyahCount(num)
            QuranSurah(
                number = num,
                nameArabic = "سورة $num",
                nameEnglish = "Surah $num",
                totalVerses = expectedAyahs,
                revelationType = "مكية"
            )
        }
        assertTrue(QuranValidator.validateSurahCount(fullList))
    }

    @Test
    fun `validateSurahDetail validates verses correctly`() {
        val ayahs = listOf(
            QuranVerse(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
            QuranVerse(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"),
            QuranVerse(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ"),
            QuranVerse(1, 4, "مَالِكِ يَوْمِ الدِّينِ"),
            QuranVerse(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"),
            QuranVerse(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ"),
            QuranVerse(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ")
        )
        val fatiha = QuranSurah(
            number = 1,
            nameArabic = "الفاتحة",
            nameEnglish = "Al-Fatiha",
            totalVerses = 7,
            revelationType = "مكية",
            ayahs = ayahs
        )
        assertTrue(QuranValidator.validateSurahDetail(fatiha))
    }

    @Test
    fun `expected verse counts match Islamic standard`() {
        assertEquals(7, QuranValidator.getExpectedAyahCount(1))
        assertEquals(286, QuranValidator.getExpectedAyahCount(2))
        assertEquals(110, QuranValidator.getExpectedAyahCount(18))
        assertEquals(30, QuranValidator.getExpectedAyahCount(67))
        assertEquals(6, QuranValidator.getExpectedAyahCount(114))
    }
}
