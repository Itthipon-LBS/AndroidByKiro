package com.example.foodorder.ui.order

import com.example.foodorder.data.model.CartItem
import com.example.foodorder.data.model.MenuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class OrderUiStateTest {

    private val itemA = MenuItem(1, "ผัดกะเพรา", "เผ็ดร้อน", BigDecimal("55"))
    private val itemB = MenuItem(2, "ข้าวมันไก่", "นุ่มลิ้น", BigDecimal("50"))

    @Test
    fun `empty state has zero derived values`() {
        val state = OrderUiState()

        assertTrue(state.isCartEmpty)
        assertEquals(0, state.cartCount)
        assertEquals(0, BigDecimal.ZERO.compareTo(state.cartTotal))
    }

    @Test
    fun `cartCount sums quantities across lines`() {
        val state = OrderUiState(
            cart = listOf(CartItem(itemA, 2), CartItem(itemB, 3))
        )

        assertFalse(state.isCartEmpty)
        assertEquals(5, state.cartCount)
    }

    @Test
    fun `cartTotal sums line totals`() {
        val state = OrderUiState(
            cart = listOf(CartItem(itemA, 2), CartItem(itemB, 1)) // 110 + 50
        )

        assertEquals(0, BigDecimal("160").compareTo(state.cartTotal))
    }
}
