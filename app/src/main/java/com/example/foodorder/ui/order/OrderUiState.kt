package com.example.foodorder.ui.order

import com.example.foodorder.data.model.CartItem
import com.example.foodorder.data.model.MenuItem

/**
 * Single source of truth for the ordering screens' UI state.
 *
 * Derived values ([cartCount], [cartTotal], [isCartEmpty]) are computed from the
 * underlying lists so they can never drift out of sync with [cart].
 */
data class OrderUiState(
    val menu: List<MenuItem> = emptyList(),
    val cart: List<CartItem> = emptyList()
) {
    val cartCount: Int
        get() = cart.sumOf { it.quantity }

    val cartTotal: Double
        get() = cart.sumOf { it.lineTotal }

    val isCartEmpty: Boolean
        get() = cart.isEmpty()
}

/**
 * One-shot events emitted by the ViewModel that the UI should handle exactly once
 * (navigation, toasts, etc.). Delivered through a [kotlinx.coroutines.channels.Channel]
 * so they are not re-triggered on configuration change.
 */
sealed interface OrderEvent {
    data class OrderPlaced(val total: Double) : OrderEvent
}
