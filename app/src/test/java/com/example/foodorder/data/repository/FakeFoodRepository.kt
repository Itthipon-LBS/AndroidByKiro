package com.example.foodorder.data.repository

import com.example.foodorder.data.model.MenuItem
import java.math.BigDecimal

/**
 * Test double for [FoodRepository] with a fixed, predictable menu.
 * Lets ViewModel tests run without the real data source or dispatchers.
 */
class FakeFoodRepository(
    private val menu: List<MenuItem> = DEFAULT_MENU
) : FoodRepository {

    override suspend fun getMenu(): List<MenuItem> = menu

    companion object {
        val ITEM_A = MenuItem(1, "ผัดกะเพรา", "เผ็ดร้อน", BigDecimal("55"))
        val ITEM_B = MenuItem(2, "ข้าวมันไก่", "นุ่มลิ้น", BigDecimal("50"))

        val DEFAULT_MENU: List<MenuItem> = listOf(ITEM_A, ITEM_B)
    }
}
