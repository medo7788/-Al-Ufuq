package com.example.utils

import com.example.data.datasource.AdhkarDataset
import com.example.data.models.AdhkarItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhkarValidatorTest {

    @Test
    fun `validateDataset returns true for verified AdhkarDataset`() {
        val items = AdhkarDataset.ALL_ITEMS
        assertTrue(AdhkarValidator.validateDataset(items))
    }

    @Test
    fun `validateItem returns false for empty text`() {
        val invalidItem = AdhkarItem(
            id = 999,
            category = "أذكار الصباح",
            textArabic = "",
            targetCount = 1,
            reference = "البخاري"
        )
        assertFalse(AdhkarValidator.validateItem(invalidItem))
    }

    @Test
    fun `validateItem returns false for invalid repeat count`() {
        val invalidItem = AdhkarItem(
            id = 999,
            category = "أذكار الصباح",
            textArabic = "سبحان الله",
            targetCount = 0,
            reference = "مسلم"
        )
        assertFalse(AdhkarValidator.validateItem(invalidItem))
    }

    @Test
    fun `validateItem returns false for missing or invalid category`() {
        val invalidItem = AdhkarItem(
            id = 999,
            category = "فئة غير معروفة",
            textArabic = "الحمد لله",
            targetCount = 1,
            reference = "الترمذي"
        )
        assertFalse(AdhkarValidator.validateItem(invalidItem))
    }

    @Test
    fun `validateDataset returns false for duplicate identifier`() {
        val duplicateItems = listOf(
            AdhkarItem(1, "أذكار الصباح", "أصبحنا وأصبح الملك لله", 1, reference = "مسلم"),
            AdhkarItem(1, "أذكار الصباح", "اللهم بك أصبحنا", 1, reference = "الترمذي")
        )
        assertFalse(AdhkarValidator.validateDataset(duplicateItems))
    }

    @Test
    fun `validateCategories returns true for verified categories`() {
        val categories = AdhkarDataset.CATEGORIES.map { it.titleArabic }
        val items = AdhkarDataset.ALL_ITEMS
        assertTrue(AdhkarValidator.validateCategories(categories, items))
    }
}
