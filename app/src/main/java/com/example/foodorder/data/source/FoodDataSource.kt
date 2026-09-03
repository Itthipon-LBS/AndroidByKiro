package com.example.foodorder.data.source

import com.example.foodorder.data.model.MenuItem
import java.math.BigDecimal

/**
 * Simple in-memory data source that provides the food menu.
 *
 * In a real app this would be backed by a remote API or a local Room database.
 * Keeping it isolated behind this class makes it easy to swap the implementation later.
 */
class FoodDataSource {

    fun getMenu(): List<MenuItem> = listOf(
        MenuItem(1, "ข้าวผัดกะเพราไก่", "ข้าวผัดกะเพราไก่ไข่ดาว รสจัดจ้าน", BigDecimal("55")),
        MenuItem(2, "ข้าวมันไก่", "ข้าวมันไก่ต้ม พร้อมน้ำจิ้มสูตรเด็ด", BigDecimal("50")),
        MenuItem(3, "ผัดไทยกุ้งสด", "ผัดไทยเส้นเหนียวนุ่ม กุ้งสดตัวโต", BigDecimal("70")),
        MenuItem(4, "ต้มยำกุ้ง", "ต้มยำกุ้งน้ำข้น รสเผ็ดร้อน", BigDecimal("90")),
        MenuItem(5, "ส้มตำไทย", "ส้มตำไทยรสชาติกลมกล่อม", BigDecimal("45")),
        MenuItem(6, "ข้าวหมูกรอบ", "ข้าวหมูกรอบราดน้ำจิ้มแจ่ว", BigDecimal("60")),
        MenuItem(7, "ก๋วยเตี๋ยวเรือ", "ก๋วยเตี๋ยวเรือน้ำตกเข้มข้น", BigDecimal("40")),
        MenuItem(8, "ชาไทยเย็น", "ชาไทยเย็นหอมมัน", BigDecimal("25"))
    )
}
