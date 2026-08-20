package com.example.foodorder.data.repository

import com.example.foodorder.data.model.MenuItem
import com.example.foodorder.data.source.FoodDataSource

/**
 * Default [FoodRepository] implementation backed by an in-memory data source.
 */
class FoodRepositoryImpl(
    private val dataSource: FoodDataSource = FoodDataSource()
) : FoodRepository {

    override fun getMenu(): List<MenuItem> = dataSource.getMenu()
}
