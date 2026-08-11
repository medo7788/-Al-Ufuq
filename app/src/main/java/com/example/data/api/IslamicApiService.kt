package com.example.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- Aladhan API Models ---
data class AladhanResponse(
    val code: Int = 200,
    val status: String = "",
    val data: AladhanData? = null
)

data class AladhanData(
    val timings: AladhanTimings,
    val date: AladhanDate
)

data class AladhanTimings(
    val Fajr: String = "04:15",
    val Sunrise: String = "05:42",
    val Dhuhr: String = "12:04",
    val Asr: String = "15:38",
    val Sunset: String = "18:26",
    val Maghrib: String = "18:26",
    val Isha: String = "19:48",
    val Imsak: String = "04:05"
)

data class AladhanDate(
    val readable: String = "",
    val hijri: HijriDateData,
    val gregorian: GregorianDateData
)

data class HijriDateData(
    val date: String = "",
    val day: String = "",
    val weekday: HijriWeekday? = null,
    val month: HijriMonth,
    val year: String = ""
)

data class HijriWeekday(
    val en: String? = "",
    val ar: String? = ""
)

data class HijriMonth(
    val number: Int? = 1,
    val en: String? = "",
    val ar: String? = ""
)

data class GregorianDateData(
    val date: String = "",
    val weekday: GregorianWeekday? = null
)

data class GregorianWeekday(
    val en: String? = ""
)

// --- Quran Cloud API Models ---
data class QuranSurahsResponse(
    val code: Int = 200,
    val status: String = "",
    val data: List<QuranSurahDto>? = null
)

data class QuranSurahDto(
    val number: Int = 1,
    val name: String = "",
    val englishName: String = "",
    val englishNameTranslation: String = "",
    val numberOfAyahs: Int = 7,
    val revelationType: String = ""
)

data class QuranSurahDetailResponse(
    val code: Int = 200,
    val status: String = "",
    val data: QuranSurahDetailData? = null
)

data class QuranSurahDetailData(
    val number: Int = 1,
    val name: String = "",
    val englishName: String = "",
    val numberOfAyahs: Int = 7,
    val ayahs: List<QuranAyahDto> = emptyList()
)

data class QuranAyahDto(
    val number: Int = 1,
    val audio: String? = null,
    val audioSecondary: List<String>? = null,
    val text: String = "",
    val numberInSurah: Int = 1,
    val juz: Int = 1,
    val page: Int = 1
)

// --- Retrofit Interface ---
interface AladhanApi {
    @GET("v1/timingsByCity")
    suspend fun getTimingsByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 5,
        @Query("school") school: Int = 0 // 0 = Shafi/Standard, 1 = Hanafi
    ): Response<AladhanResponse>

    @GET("v1/timings")
    suspend fun getTimingsByCoordinates(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 5,
        @Query("school") school: Int = 0
    ): Response<AladhanResponse>
}

interface QuranApi {
    @GET("v1/surah")
    suspend fun getSurahsList(): Response<QuranSurahsResponse>

    @GET("v1/surah/{number}/ar.alafasy")
    suspend fun getSurahDetailWithAudio(
        @Path("number") surahNumber: Int
    ): Response<QuranSurahDetailResponse>
}
