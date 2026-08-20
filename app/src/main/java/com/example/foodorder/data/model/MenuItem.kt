package com.example.foodorder.data.model

import java.math.BigDecimal

/**
 * Represents a single item on the food menu.
 *
 * Money is modelled with [BigDecimal] rather than [Double] to avoid binary
 * floating-point rounding errors in price calculations.
 */
data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: BigDecimal
)
