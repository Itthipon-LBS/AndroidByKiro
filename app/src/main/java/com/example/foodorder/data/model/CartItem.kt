package com.example.foodorder.data.model

/**
 * Represents a menu item that has been added to the cart along with its quantity.
 */
data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    /** Total price for this line (unit price * quantity). */
    val lineTotal: Double
        get() = menuItem.price * quantity
}
