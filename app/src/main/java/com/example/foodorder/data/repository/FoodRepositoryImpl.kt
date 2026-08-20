package com.example.foodorder.data.repository

import com.example.foodorder.data.model.MenuItem
import com.example.foodorder.data.source.FoodDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default [FoodRepository] implementation backed by an in-memory data source.
 *
 * Work is moved off the caller's thread via [dispatcher] to model how a real
 * (network/DB) repository would behave and to keep the main thread free.
 */
class FoodRepositoryImpl(
    private val dataSource: FoodDataSource = FoodDataSource(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FoodRepository {

    override suspend fun getMenu(): List<MenuItem> = withContext(dispatcher) {
        dataSource.getMenu()
    }
}
