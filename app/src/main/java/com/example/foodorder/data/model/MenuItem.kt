package com.example.foodorder.data.model

/**
 * Represents a single item on the food menu.
 */
data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double
)
