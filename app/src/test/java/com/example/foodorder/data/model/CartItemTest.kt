package com.example.foodorder.data.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CartItemTest {

    private val item = MenuItem(1, "ผัดกะเพรา", "เผ็ดร้อน", BigDecimal("55"))

    @Test
    fun `lineTotal multiplies price by quantity`() {
        val cartItem = CartItem(item, quantity = 3)
        assertEquals(0, BigDecimal("165").compareTo(cartItem.lineTotal))
    }

    @Test
    fun `lineTotal is zero when quantity is zero`() {
        val cartItem = CartItem(item, quantity = 0)
        assertEquals(0, BigDecimal.ZERO.compareTo(cartItem.lineTotal))
    }
}
