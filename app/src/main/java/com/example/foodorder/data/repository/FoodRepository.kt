package com.example.foodorder.data.repository

import com.example.foodorder.data.model.MenuItem

/**
 * Abstraction over the data layer. The ViewModel depends on this interface,
 * not on a concrete implementation, which keeps the layers decoupled and testable.
 */
interface FoodRepository {
    fun getMenu(): List<MenuItem>
}
