package com.example.data.repository

import com.example.data.local.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.*

class AlUfuqRepository(private val dao: AlUfuqDao) {

    // --- Quran Data ---
    val quranProgress: Flow<QuranProgressEntity?> = dao.getQuranProgress()

    suspend fun updateQuranProgress(surahNumber: Int, surahName: String, verseNumber: Int, totalVerses: Int) {
        val current = QuranProgressEntity(
            id = 1,
            surahNumber = surahNumber,
            surahNameArabic = surahName,
            verseNumber = verseNumber,
            totalVersesInSurah = totalVerses,
            lastReadTimestamp = System.currentTimeMillis(),
            streakDays = 4
        )
        dao.saveQuranProgress(current)
    }

    fun getSurahsList(): List<QuranSurah> {
        return listOf(
            QuranSurah(1, "الفاتحة", "Al-Fatiha", 7, "مكية", 1, 1),
            QuranSurah(2, "البقرة", "Al-Baqarah", 286, "مدنية", 1, 2),
            QuranSurah(3, "آل عمران", "Ali 'Imran", 200, "مدنية", 3, 50),
            QuranSurah(4, "النساء", "An-Nisa", 176, "مدنية", 4, 77),
            QuranSurah(5, "المائدة", "Al-Ma'idah", 120, "مدنية", 6, 106),
            QuranSurah(6, "الأنعام", "Al-An'am", 165, "مكية", 7, 128),
            QuranSurah(7, "الأعراف", "Al-A'raf", 206, "مكية", 8, 151),
            QuranSurah(8, "الأنفال", "Al-Anfal", 75, "مدنية", 9, 177),
            QuranSurah(9, "التوبة", "At-Tawbah", 129, "مدنية", 10, 187),
            QuranSurah(10, "يونس", "Yunus", 109, "مكية", 11, 208),
            QuranSurah(11, "هود", "Hud", 123, "مكية", 11, 221),
            QuranSurah(12, "يوسف", "Yusuf", 111, "مكية", 12, 235),
            QuranSurah(13, "الرعد", "Ar-Ra'd", 43, "مدنية", 13, 249),
            QuranSurah(14, "إبراهيم", "Ibrahim", 52, "مكية", 13, 255),
            QuranSurah(15, "الحجر", "Al-Hijr", 99, "مكية", 14, 262),
            QuranSurah(16, "النحل", "An-Nahl", 128, "مكية", 14, 267),
            QuranSurah(17, "الإسراء", "Al-Isra", 111, "مكية", 15, 282),
            QuranSurah(18, "الكهف", "Al-Kahf", 110, "مكية", 15, 293),
            QuranSurah(19, "مريم", "Maryam", 98, "مكية", 16, 305),
            QuranSurah(20, "طه", "Taha", 135, "مكية", 16, 312),
            QuranSurah(21, "الأنبياء", "Al-Anbiya", 112, "مكية", 17, 322),
            QuranSurah(22, "الحج", "Al-Hajj", 78, "مدنية", 17, 332),
            QuranSurah(23, "المؤمنون", "Al-Mu'minun", 118, "مكية", 18, 342),
            QuranSurah(24, "النور", "An-Nur", 64, "مدنية", 18, 350),
            QuranSurah(25, "الفرقان", "Al-Furqan", 77, "مكية", 18, 359),
            QuranSurah(26, "الشعراء", "Ash-Shu'ara", 227, "مكية", 19, 367),
            QuranSurah(27, "النمل", "An-Naml", 93, "مكية", 19, 377),
            QuranSurah(28, "القصص", "Al-Qasas", 88, "مكية", 20, 385),
            QuranSurah(29, "العنكبوت", "Al-'Ankabut", 69, "مكية", 20, 396),
            QuranSurah(30, "الروم", "Ar-Rum", 60, "مكية", 21, 404),
            QuranSurah(36, "يس", "Ya-Sin", 83, "مكية", 22, 440),
            QuranSurah(55, "الرحمن", "Ar-Rahman", 78, "مدنية", 27, 531),
            QuranSurah(56, "الواقعة", "Al-Waqi'ah", 96, "مكية", 27, 534),
            QuranSurah(67, "الملك", "Al-Mulk", 30, "مكية", 29, 562),
            QuranSurah(112, "الإخلاص", "Al-Ikhlas", 4, "مكية", 30, 604),
            QuranSurah(113, "الفلق", "Al-Falaq", 5, "مكية", 30, 604),
            QuranSurah(114, "الناس", "An-Nas", 6, "مكية", 30, 604)
        )
    }

    fun getVersesForSurah(surahNumber: Int): List<QuranVerse> {
        return when (surahNumber) {
            1 -> listOf(
                QuranVerse(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, Most Gracious, Most Merciful"),
                QuranVerse(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Praise be to Allah, Lord of the Universe"),
                QuranVerse(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Most Gracious, Most Merciful"),
                QuranVerse(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Master of the Day of Judgment"),
                QuranVerse(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "You alone we worship, and You alone we ask for help"),
                QuranVerse(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path"),
                QuranVerse(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those You have blessed, not of those who earned anger, nor of those who went astray")
            )
            18 -> listOf(
                QuranVerse(18, 1, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا ۜ", "تفسير: الحمد لله الذي أنزل القرآن على عبده محمد صلى الله عليه وسلم كتاباً مستقيماً"),
                QuranVerse(18, 2, "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", "مكثاً فيه أبداً وبشرى بالنعيم المقيم"),
                QuranVerse(18, 17, "وَتَرَى الشَّمْسَ إِذَا طَلَعَت تَّزَاوَرُ عَن كَهْفِهِمْ ذَاتَ الْيَمِينِ وَإِذَا غَرَبَت تَّقْرِضُهُمْ ذَاتَ الشِّمَالِ وَهُمْ فِي فَجْوَةٍ مِّنْهُ ۚ", "من آيات الله العظيمة في حماية فتية الكهف"),
                QuranVerse(18, 18, "وَتَحْسَبُهُمْ أَيْقَاظًا وَهُمْ رُقُودٌ ۚ وَنُقَلِّبُهُمْ ذَاتَ الْيَمِينِ وَذَاتَ الشِّمَالِ ۖ وَكَلْبُهُم بَاسِطٌ ذِرَاعَيْهِ بِالْوَصِيدِ ۚ", "الآية التي توقفت عندها - تابع القراءة للتفكر في تدبير الله")
            )
            67 -> listOf(
                QuranVerse(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا"),
                QuranVerse(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "سورة الملك المنجية من عذاب القبر")
            )
            else -> listOf(
                QuranVerse(surahNumber, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "تفسير وتدبر معاني السورة الكريمة"),
                QuranVerse(surahNumber, 2, "آيات بينات من كلام الله عز وجل تجلو القلوب وتهدي للتي هي أقوم", "متابعة تلاوة كلام الله")
            )
        }
    }

    // --- Adhkar Data ---
    fun getAdhkarCategories(): List<AdhkarCategory> {
        return listOf(
            AdhkarCategory("أذكار الصباح", 12, 4, "wb_sunny"),
            AdhkarCategory("أذكار المساء", 12, 4, "nights_stay"),
            AdhkarCategory("أذكار بعد الصلاة", 8, 3, "mosque"),
            AdhkarCategory("أذكار النوم", 6, 2, "bed"),
            AdhkarCategory("أذكار السفر والركوب", 5, 2, "directions_car")
        )
    }

    fun getAdhkarItems(category: String): List<AdhkarItem> {
        return when (category) {
            "أذكار الصباح" -> listOf(
                AdhkarItem(1, category, "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ.", 1, 0, "مسلم", "تجديد التوحيد والحمد في مطلع اليوم"),
                AdhkarItem(2, category, "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ.", 1, 0, "الترمذي", "الاعتراف بفضل الله في الحياة والنشور"),
                AdhkarItem(3, category, "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ.", 1, 0, "البخاري - سيد الاستغفار", "من قالها موقنا بها ومات دخل الجنة"),
                AdhkarItem(4, category, "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.", 3, 0, "مسلم", "تعدل أذكاراً كثيرة في الأجر والفضل"),
                AdhkarItem(5, category, "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ.", 3, 0, "أبو داود", "حفظ العافية والأنوار في الجسد والقلب"),
                AdhkarItem(6, category, "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.", 3, 0, "الترمذي", "حماية من فجأة البلاء وشرور المخلوقات")
            )
            "أذكار المساء" -> listOf(
                AdhkarItem(10, category, "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ.", 1, 0, "مسلم", "شكر الله على إتمام اليوم وسكون الليل"),
                AdhkarItem(11, category, "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ.", 1, 0, "الترمذي", "تسليم الأمر لله في إقبال الليل"),
                AdhkarItem(12, category, "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.", 3, 0, "مسلم", "حرز من كل حشرة وضرر حتى يصبح"),
                AdhkarItem(13, category, "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلاَئِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لاَ إِلَهَ إِلاَّ أَنْتَ.", 4, 0, "أبو داود", "من قالها أعتقه الله من النار")
            )
            else -> listOf(
                AdhkarItem(20, category, "أَسْتَغْفِرُ اللَّهَ العَظِيمَ الَّذِي لاَ إِلَهَ إِلاَّ هُوَ الحَيُّ القَيُّومُ وَأَتُوبُ إِلَيْهِ.", 3, 0, "الترمذي", "مغفرة الذنوب ورفع الدرجات"),
                AdhkarItem(21, category, "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ.", 1, 0, "أبو داود", "دعاء الإعانة على الطاعة بعد الصلاة"),
                AdhkarItem(22, category, "سُبْحَانَ اللَّهِ (33) ، الْحَمْدُ لِلَّهِ (33) ، اللَّهُ أَكْبَرُ (33)", 99, 0, "مسلم", "تسبيح دبر كل صلاة مكتوبة")
            )
        }
    }

    // --- Tasbeeh Routines ---
    val tasbeehRoutines: Flow<List<TasbeehRoutineEntity>> = dao.getTasbeehRoutines()

    suspend fun updateTasbeeh(id: Int, count: Int) {
        dao.updateTasbeehCount(id, count)
    }

    suspend fun saveRoutine(routine: TasbeehRoutineEntity) {
        dao.saveTasbeehRoutine(routine)
    }

    // --- Zakat Calculations ---
    val zakatRecords: Flow<List<ZakatRecordEntity>> = dao.getZakatRecords()

    suspend fun saveZakatRecord(cash: Double, goldGrams: Double, goldPrice: Double, debts: Double, totalZakat: Double) {
        dao.insertZakatRecord(
            ZakatRecordEntity(
                cashAmount = cash,
                gold24kGrams = goldGrams,
                goldPriceGram = goldPrice,
                debtsAmount = debts,
                totalZakatPayable = totalZakat
            )
        )
    }

    // --- Dynamic Prayer Engine ---
    fun calculateTodayPrayers(): PrayerDayState {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val totalCurrentMinutes = currentHour * 60 + currentMinute

        // Preset accurate prayer schedule for today
        // Fajr 04:15, Sunrise 05:42, Dhuhr 12:04, Asr 15:38, Maghrib 18:26, Isha 19:48
        val fajrMinutes = 4 * 60 + 15      // 04:15 AM
        val sunriseMinutes = 5 * 60 + 42   // 05:42 AM
        val dhuhrMinutes = 12 * 60 + 4     // 12:04 PM
        val asrMinutes = 15 * 60 + 38      // 03:38 PM
        val maghribMinutes = 18 * 60 + 26  // 06:26 PM
        val ishaMinutes = 19 * 60 + 48     // 07:48 PM

        val prayers = listOf(
            PrayerTimeInfo("fajr", "الفجر", "Fajr", "04:15 ص", getMillisForTime(4, 15), totalCurrentMinutes > fajrMinutes),
            PrayerTimeInfo("sunrise", "الشروق", "Sunrise", "05:42 ص", getMillisForTime(5, 42), totalCurrentMinutes > sunriseMinutes),
            PrayerTimeInfo("dhuhr", "الظهر", "Dhuhr", "12:04 م", getMillisForTime(12, 4), totalCurrentMinutes > dhuhrMinutes),
            PrayerTimeInfo("asr", "العصر", "Asr", "03:38 م", getMillisForTime(15, 38), totalCurrentMinutes > asrMinutes),
            PrayerTimeInfo("maghrib", "المغرب", "Maghrib", "06:26 م", getMillisForTime(18, 26), totalCurrentMinutes > maghribMinutes),
            PrayerTimeInfo("isha", "العشاء", "Isha", "07:48 م", getMillisForTime(19, 48), totalCurrentMinutes > ishaMinutes)
        )

        val nextIndex = when {
            totalCurrentMinutes < fajrMinutes -> 0
            totalCurrentMinutes < sunriseMinutes -> 1
            totalCurrentMinutes < dhuhrMinutes -> 2
            totalCurrentMinutes < asrMinutes -> 3
            totalCurrentMinutes < maghribMinutes -> 4
            totalCurrentMinutes < ishaMinutes -> 5
            else -> 0 // Next day Fajr
        }

        val nextPrayerInfo = prayers[nextIndex].copy(isNext = true)

        val targetMinutes = when (nextIndex) {
            0 -> fajrMinutes + if (totalCurrentMinutes >= ishaMinutes) 24 * 60 else 0
            1 -> sunriseMinutes
            2 -> dhuhrMinutes
            3 -> asrMinutes
            4 -> maghribMinutes
            else -> ishaMinutes
        }

        val prevMinutes = when (nextIndex) {
            0 -> if (totalCurrentMinutes >= ishaMinutes) ishaMinutes else 0
            1 -> fajrMinutes
            2 -> sunriseMinutes
            3 -> dhuhrMinutes
            4 -> asrMinutes
            else -> maghribMinutes
        }

        val diffMinutes = (targetMinutes - totalCurrentMinutes).coerceAtLeast(0)
        val remainingHours = diffMinutes / 60
        val remainingMins = diffMinutes % 60
        val remainingSecs = 59 - currentSecond
        val remainingFormatted = String.format(Locale.US, "%02d:%02d:%02d", remainingHours, remainingMins, remainingSecs)

        val totalIntervalMinutes = (targetMinutes - prevMinutes).coerceAtLeast(1)
        val elapsedIntervalMinutes = (totalCurrentMinutes - prevMinutes).coerceAtLeast(0)
        val intervalPercentage = (elapsedIntervalMinutes.toFloat() / totalIntervalMinutes.toFloat()).coerceIn(0f, 1f)

        val currentPrayerName = when {
            totalCurrentMinutes < fajrMinutes -> "العشاء"
            totalCurrentMinutes < sunriseMinutes -> "الفجر"
            totalCurrentMinutes < dhuhrMinutes -> "الشروق"
            totalCurrentMinutes < asrMinutes -> "الظهر"
            totalCurrentMinutes < maghribMinutes -> "العصر"
            totalCurrentMinutes < ishaMinutes -> "المغرب"
            else -> "العشاء"
        }

        return PrayerDayState(
            currentPrayerName = currentPrayerName,
            nextPrayer = nextPrayerInfo,
            remainingTimeString = remainingFormatted,
            intervalProgressPercentage = intervalPercentage,
            prayers = prayers,
            hijriDateString = "23 صفار 1448 هـ",
            gregorianDateString = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar")).format(Date())
        )
    }

    fun parseApiTimingsToState(
        timings: com.example.data.api.AladhanTimings,
        hijriStr: String,
        gregorianStr: String
    ): PrayerDayState {
        fun cleanTime(raw: String): String {
            return raw.split(" ")[0] // remove (EET) etc.
        }

        fun formatAmPm(time24: String): String {
            val parts = cleanTime(time24).split(":")
            if (parts.size < 2) return time24
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            val ampm = if (h >= 12) "م" else "ص"
            val h12 = if (h % 12 == 0) 12 else h % 12
            return String.format(Locale.US, "%d:%02d %s", h12, m, ampm)
        }

        val cal = Calendar.getInstance()
        val curHour = cal.get(Calendar.HOUR_OF_DAY)
        val curMin = cal.get(Calendar.MINUTE)
        val curTotalMins = curHour * 60 + curMin

        fun toMins(time24: String): Int {
            val parts = cleanTime(time24).split(":")
            if (parts.size < 2) return 0
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }

        val fajrM = toMins(timings.Fajr)
        val sunriseM = toMins(timings.Sunrise)
        val dhuhrM = toMins(timings.Dhuhr)
        val asrM = toMins(timings.Asr)
        val maghribM = toMins(timings.Maghrib)
        val ishaM = toMins(timings.Isha)

        val timingsList = listOf(
            Triple("1", "الفجر", timings.Fajr to fajrM),
            Triple("2", "الشروق", timings.Sunrise to sunriseM),
            Triple("3", "الظهر", timings.Dhuhr to dhuhrM),
            Triple("4", "العصر", timings.Asr to asrM),
            Triple("5", "المغرب", timings.Maghrib to maghribM),
            Triple("6", "العشاء", timings.Isha to ishaM)
        )

        val nextIndex = when {
            curTotalMins < fajrM -> 0
            curTotalMins < sunriseM -> 1
            curTotalMins < dhuhrM -> 2
            curTotalMins < asrM -> 3
            curTotalMins < maghribM -> 4
            curTotalMins < ishaM -> 5
            else -> 0
        }

        val prayers = timingsList.mapIndexed { idx, item ->
            val isNext = idx == nextIndex
            val isCompleted = if (nextIndex == 0 && curTotalMins >= ishaM) true else idx < nextIndex
            PrayerTimeInfo(
                id = item.first,
                nameArabic = item.second,
                nameEnglish = when (item.second) {
                    "الفجر" -> "Fajr"
                    "الشروق" -> "Sunrise"
                    "الظهر" -> "Dhuhr"
                    "العصر" -> "Asr"
                    "المغرب" -> "Maghrib"
                    else -> "Isha"
                },
                timeFormatted = formatAmPm(item.third.first),
                timestampMillis = System.currentTimeMillis(),
                isNext = isNext,
                isCompleted = isCompleted
            )
        }

        val targetM = timingsList[nextIndex].third.second
        val diffM = (if (targetM >= curTotalMins) targetM - curTotalMins else (1440 - curTotalMins + targetM)).coerceAtLeast(0)
        val remH = diffM / 60
        val remM = diffM % 60
        val remainingFormatted = String.format(Locale.US, "%02d:%02d:00", remH, remM)

        return PrayerDayState(
            currentPrayerName = timingsList.getOrNull(if (nextIndex > 0) nextIndex - 1 else 5)?.second ?: "العشاء",
            nextPrayer = prayers[nextIndex],
            remainingTimeString = remainingFormatted,
            intervalProgressPercentage = 0.5f,
            prayers = prayers,
            hijriDateString = hijriStr,
            gregorianDateString = gregorianStr
        )
    }

    private fun getMillisForTime(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }

    // --- Your Moment Engine ---
    fun getYourMomentContext(prayerState: PrayerDayState, quranProgress: QuranProgressEntity?): YourMomentContext {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val nextName = prayerState.nextPrayer.nameArabic
        val remaining = prayerState.remainingTimeString

        return when {
            // Close to prayer (less than 30 mins remaining)
            prayerState.intervalProgressPercentage > 0.8f -> YourMomentContext(
                titleArabic = "اقتربت صلاة $nextName",
                subtitleArabic = "تبقى $remaining فقط • استعد وتوضأ للصلاة",
                actionLabelArabic = "عرض مواقيت الصلاة والقبلة",
                targetDestination = MomentTarget.PRAYER,
                badgeTagArabic = "تنبيه الصلاة"
            )
            // Morning time (5 AM to 9 AM)
            hour in 5..9 -> YourMomentContext(
                titleArabic = "بداية هادئة ليومك",
                subtitleArabic = "أنر صباحك بالذكر والقرآن الكريم",
                actionLabelArabic = "بدء أذكار الصباح (4 دقائق)",
                targetDestination = MomentTarget.MORNING_ADHKAR,
                badgeTagArabic = "أذكار الصباح"
            )
            // Afternoon/Evening (4 PM to 8 PM)
            hour in 16..20 -> YourMomentContext(
                titleArabic = "لحظة سكون وإقبال",
                subtitleArabic = "حصّن نفسك بأذكار المساء قبل غروب الشمس",
                actionLabelArabic = "بدء أذكار المساء (4 دقائق)",
                targetDestination = MomentTarget.EVENING_ADHKAR,
                badgeTagArabic = "أذكار المساء"
            )
            // Quran continuation
            else -> {
                val surah = quranProgress?.surahNameArabic ?: "سورة الكهف"
                val ayah = quranProgress?.verseNumber ?: 18
                YourMomentContext(
                    titleArabic = "تابع وردك القرآني",
                    subtitleArabic = "$surah • الآية $ayah • تبقّى حوالي 7 دقائق",
                    actionLabelArabic = "تابع القراءة من حيث توقفت",
                    targetDestination = MomentTarget.QURAN_CONTINUE,
                    badgeTagArabic = "وردك الحالي"
                )
            }
        }
    }
}
