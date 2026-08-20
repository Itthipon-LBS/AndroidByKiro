package com.example.foodorder.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodorder.data.model.CartItem
import com.example.foodorder.data.model.MenuItem
import com.example.foodorder.data.repository.FoodRepository

/**
 * Shared ViewModel for the ordering flow.
 *
 * It is scoped to the navigation graph so that both the menu screen and the
 * cart screen observe and mutate the same cart state. UI state is exposed as
 * immutable [LiveData]; mutations happen only through the public functions.
 */
class OrderViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    private val _menu = MutableLiveData<List<MenuItem>>()
    val menu: LiveData<List<MenuItem>> = _menu

    // Cart is kept as an id -> CartItem map internally for O(1) updates,
    // and exposed to the UI as an ordered list.
    private val cartItems = LinkedHashMap<Int, CartItem>()

    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> = _cart

    private val _cartCount = MutableLiveData(0)
    val cartCount: LiveData<Int> = _cartCount

    private val _cartTotal = MutableLiveData(0.0)
    val cartTotal: LiveData<Double> = _cartTotal

    init {
        loadMenu()
    }

    private fun loadMenu() {
        _menu.value = repository.getMenu()
    }

    /** Adds one unit of [menuItem] to the cart. */
    fun addToCart(menuItem: MenuItem) {
        val existing = cartItems[menuItem.id]
        val newQuantity = (existing?.quantity ?: 0) + 1
        cartItems[menuItem.id] = CartItem(menuItem, newQuantity)
        publishCart()
    }

    /** Removes one unit of [menuItem]; removes the line entirely when it hits zero. */
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
     * Finalizes the order. Returns the total that was ordered and clears the cart.
     * Returns null when the cart is empty.
     */
    fun placeOrder(): Double? {
        if (cartItems.isEmpty()) return null
        val total = _cartTotal.value ?: 0.0
        cartItems.clear()
        publishCart()
        return total
    }

    private fun publishCart() {
        val items = cartItems.values.toList()
        _cart.value = items
        _cartCount.value = items.sumOf { it.quantity }
        _cartTotal.value = items.sumOf { it.lineTotal }
    }
}
