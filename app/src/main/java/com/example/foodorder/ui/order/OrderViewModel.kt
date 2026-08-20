package com.example.foodorder.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodorder.data.model.CartItem
import com.example.foodorder.data.model.MenuItem
import com.example.foodorder.data.repository.FoodRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Shared ViewModel for the ordering flow.
 *
 * Scoped to the navigation graph so the menu and cart screens share one instance.
 * Exposes a single immutable [OrderUiState] as the source of truth, plus a stream
 * of one-shot [OrderEvent]s for actions the UI should handle once.
 */
class OrderViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    // Backing cart, keyed by menu item id for O(1) updates. Kept private so the
    // only way to change state is through the public intent functions below.
    private val cartItems = LinkedHashMap<Int, CartItem>()

    private val _uiState = MutableLiveData(OrderUiState())
    val uiState: LiveData<OrderUiState> = _uiState

    private val _events = Channel<OrderEvent>(Channel.BUFFERED)
    val events: Flow<OrderEvent> = _events.receiveAsFlow()

    init {
        loadMenu()
    }

    private fun loadMenu() {
        _uiState.value = currentState().copy(menu = repository.getMenu())
    }

    /** Adds one unit of [menuItem] to the cart. */
    fun addToCart(menuItem: MenuItem) {
        val existing = cartItems[menuItem.id]
        val newQuantity = (existing?.quantity ?: 0) + 1
        cartItems[menuItem.id] = CartItem(menuItem, newQuantity)
        publishCart()
    }

    /** Removes one unit of [menuItem]; drops the line entirely when it reaches zero. */
    fun decreaseQuantity(menuItem: MenuItem) {
        val existing = cartItems[menuItem.id] ?: return
        val newQuantity = existing.quantity - 1
        if (newQuantity <= 0) {
            cartItems.remove(menuItem.id)
        } else {
            cartItems[menuItem.id] = existing.copy(quantity = newQuantity)
        }
        publishCart()
    }

    /**
     * Finalizes the order: emits an [OrderEvent.OrderPlaced] with the total and
     * clears the cart. Does nothing when the cart is empty.
     */
    fun placeOrder() {
        if (cartItems.isEmpty()) return
        val total = cartItems.values.sumOf { it.lineTotal }
        cartItems.clear()
        publishCart()
        _events.trySend(OrderEvent.OrderPlaced(total))
    }

    private fun publishCart() {
        _uiState.value = currentState().copy(cart = cartItems.values.toList())
    }

    private fun currentState(): OrderUiState = _uiState.value ?: OrderUiState()
}
