package com.example.data.local

import android.content.Context
import com.example.data.models.HisnCategory
import com.example.data.models.HisnItem
import org.json.JSONArray

/**
 * Loads the complete Hisn Al-Muslim reference from assets/hisn_almuslim.json.
 * Read-only reference content — parsed once per process and cached in memory
 * (132 categories / ~267 items, ~110KB, trivial to hold as plain objects).
 */
object HisnAlMuslimLoader {

    @Volatile
    private var cache: List<HisnCategory>? = null

    fun load(context: Context): List<HisnCategory> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val json = context.assets.open("hisn_almuslim.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val arr = JSONArray(json)
            val categories = ArrayList<HisnCategory>(arr.length())
            for (i in 0 until arr.length()) {
                val catObj = arr.getJSONObject(i)
                val itemsArr = catObj.getJSONArray("items")
                val items = ArrayList<HisnItem>(itemsArr.length())
                for (j in 0 until itemsArr.length()) {
                    val itemObj = itemsArr.getJSONObject(j)
                    items.add(
                        HisnItem(
                            id = itemObj.getInt("id"),
                            text = itemObj.getString("text"),
                            count = itemObj.optInt("count", 1)
                        )
                    )
                }
                categories.add(
                    HisnCategory(
                        id = catObj.getInt("id"),
                        category = catObj.getString("category"),
                        items = items
                    )
                )
            }
            cache = categories
            return categories
        }
    }
}
