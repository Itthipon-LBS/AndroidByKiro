package com.example.foodorder.data.repository

import com.example.foodorder.data.model.MenuItem

/**
 * Abstraction over the data layer. The ViewModel depends on this interface,
 * not on a concrete implementation, which keeps the layers decoupled and testable.
 *
 * [getMenu] is a suspend function so callers stay off the main thread; this shape
 * scales directly to a real network or database backed implementation.
 */
interface FoodRepository {
    suspend fun getMenu(): List<MenuItem>
}
