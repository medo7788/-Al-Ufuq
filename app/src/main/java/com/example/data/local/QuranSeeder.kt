package com.example.data.local

import android.content.Context
import org.json.JSONArray

/**
 * Seeds the local quran_ayahs table from the bundled offline asset
 * (assets/quran_ar.json — 6,236 verses, extracted from a verified local Quran
 * database). Runs once: after this, AlUfuqRepository.getVersesForSurah's
 * existing cache path (dao.getAyahsForSurah) serves the complete Quran with
 * zero internet dependency, for every one of the 114 surahs.
 *
 * juz/page/sajda are not present in this asset (that data lives in the
 * ayas_ar-only extract) and are seeded as 0/false — they're display-only
 * enrichment fields, not required for verse text to be correct or complete.
 */
object QuranSeeder {

    private const val PREFS_NAME = "quran_seed_prefs"
    private const val KEY_SEEDED = "quran_seeded_v1"
    private const val EXPECTED_VERSE_COUNT = 6236

    suspend fun seedIfNeeded(context: Context, dao: AlUfuqDao) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        val json = context.assets.open("quran_ar.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        val arr = JSONArray(json)
        if (arr.length() < EXPECTED_VERSE_COUNT) {
            // Asset looks incomplete/corrupted — don't mark as seeded, so we retry next launch
            // instead of silently locking in partial data.
            return
        }

        val entities = ArrayList<QuranAyahEntity>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val surah = obj.getInt("surah")
            val ayah = obj.getInt("ayah")
            entities.add(
                QuranAyahEntity(
                    compositeId = "${surah}_${ayah}",
                    surahNumber = surah,
                    numberInSurah = ayah,
                    textArabic = obj.getString("text"),
                    juz = 0,
                    page = 0,
                    sajda = false
                )
            )
        }

        dao.insertAyahs(entities)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }
}
