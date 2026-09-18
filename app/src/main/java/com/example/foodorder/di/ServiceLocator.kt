package com.example.foodorder.di

import com.example.foodorder.data.repository.FoodRepository
import com.example.foodorder.data.repository.FoodRepositoryImpl

/**
 * A tiny manual dependency-injection container.
 *
 * For a small project this avoids pulling in a full DI framework (Hilt/Koin)
 * while still keeping construction of dependencies in one place. Swap the
 * implementation here to change the whole app's data source.
 */
object ServiceLocator {

    val foodRepository: FoodRepository by lazy { FoodRepositoryImpl() }
}
