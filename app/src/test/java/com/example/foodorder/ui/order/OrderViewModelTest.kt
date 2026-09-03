package com.example.foodorder.ui.order

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.foodorder.data.repository.FakeFoodRepository
import com.example.foodorder.data.repository.FakeFoodRepository.Companion.ITEM_A
import com.example.foodorder.data.repository.FakeFoodRepository.Companion.ITEM_B
import com.example.foodorder.data.repository.FoodRepository
import com.example.foodorder.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class OrderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        repository: FoodRepository = FakeFoodRepository(),
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) = OrderViewModel(repository, savedStateHandle)

    @Test
    fun `loads menu into ui state on init`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(FakeFoodRepository.DEFAULT_MENU, viewModel.uiState.value.menu)
    }

    @Test
    fun `starts with an empty cart`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isCartEmpty)
        assertEquals(0, state.cartCount)
        assertEquals(0, BigDecimal.ZERO.compareTo(state.cartTotal))
    }

    @Test
    fun `addToCart increments quantity and totals`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addToCart(ITEM_A)
        viewModel.addToCart(ITEM_A)

        val state = viewModel.uiState.value
        assertEquals(1, state.cart.size)
        assertEquals(2, state.cart.first().quantity)
        assertEquals(2, state.cartCount)
        // 55 * 2 = 110
        assertEquals(0, BigDecimal("110").compareTo(state.cartTotal))
    }

    @Test
    fun `cartTotal sums different items`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addToCart(ITEM_A) // 55
        viewModel.addToCart(ITEM_B) // 50

        assertEquals(2, viewModel.uiState.value.cartCount)
        assertEquals(0, BigDecimal("105").compareTo(viewModel.uiState.value.cartTotal))
    }

    @Test
    fun `decreaseQuantity removes the line when it reaches zero`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addToCart(ITEM_A)
            viewModel.decreaseQuantity(ITEM_A)

            assertTrue(viewModel.uiState.value.isCartEmpty)
        }

    @Test
    fun `decreaseQuantity keeps the line when quantity above one`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addToCart(ITEM_A)
            viewModel.addToCart(ITEM_A)
            viewModel.decreaseQuantity(ITEM_A)

            val state = viewModel.uiState.value
            assertEquals(1, state.cart.size)
            assertEquals(1, state.cart.first().quantity)
        }

    @Test
    fun `placeOrder on empty cart emits no event`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.placeOrder()
                expectNoEvents()
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `placeOrder emits OrderPlaced with total and clears cart`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.addToCart(ITEM_A) // 55
            viewModel.addToCart(ITEM_B) // 50

            viewModel.events.test {
                viewModel.placeOrder()
                val event = awaitItem()
                assertTrue(event is OrderEvent.OrderPlaced)
                assertEquals(
                    0,
                    BigDecimal("105").compareTo((event as OrderEvent.OrderPlaced).total)
                )
                cancelAndConsumeRemainingEvents()
            }

            assertTrue(viewModel.uiState.value.isCartEmpty)
        }

    @Test
    fun `restores cart from SavedStateHandle`() = runTest(mainDispatcherRule.testDispatcher) {
        val saved = SavedStateHandle(mapOf("cart_quantities" to linkedMapOf(ITEM_A.id to 3)))

        val viewModel = createViewModel(savedStateHandle = saved)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCartEmpty)
        assertEquals(1, state.cart.size)
        assertEquals(ITEM_A, state.cart.first().menuItem)
        assertEquals(3, state.cart.first().quantity)
        assertEquals(3, state.cartCount)
    }

    @Test
    fun `cart survives ViewModel recreation through SavedStateHandle`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // A shared handle stands in for the one the framework keeps across
            // configuration change / process death.
            val saved = SavedStateHandle()

            val original = createViewModel(savedStateHandle = saved)
            advanceUntilIdle()
            original.addToCart(ITEM_A)
            original.addToCart(ITEM_A)
            original.addToCart(ITEM_B)

            // Recreate the ViewModel from the same handle.
            val recreated = createViewModel(savedStateHandle = saved)
            advanceUntilIdle()

            val state = recreated.uiState.value
            assertEquals(2, state.cart.size)
            assertEquals(3, state.cartCount)
            assertEquals(2, state.cart.first { it.menuItem == ITEM_A }.quantity)
            assertEquals(1, state.cart.first { it.menuItem == ITEM_B }.quantity)
        }

    @Test
    fun `decreaseQuantity on an item not in the cart is a no-op`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.decreaseQuantity(ITEM_A)

            assertTrue(viewModel.uiState.value.isCartEmpty)
        }

    @Test
    fun `cart preserves insertion order`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addToCart(ITEM_B)
        viewModel.addToCart(ITEM_A)

        val orderedIds = viewModel.uiState.value.cart.map { it.menuItem.id }
        assertEquals(listOf(ITEM_B.id, ITEM_A.id), orderedIds)
    }
}
