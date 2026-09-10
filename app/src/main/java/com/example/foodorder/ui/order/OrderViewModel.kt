package com.example.foodorder.ui.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodorder.data.model.CartItem
import com.example.foodorder.data.model.MenuItem
import com.example.foodorder.data.repository.FoodRepository
import com.example.foodorder.di.ServiceLocator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * Shared ViewModel for the ordering flow.
 *
 * Scoped to the navigation graph so the menu and cart screens share one instance.
 * Exposes a single immutable [OrderUiState] via [StateFlow] as the source of truth,
 * plus a stream of one-shot [OrderEvent]s for actions the UI should handle once.
 *
 * The cart (menu-item id -> quantity) is persisted in [SavedStateHandle] so it
 * survives both configuration changes and process death.
 */
class OrderViewModel(
    private val repository: FoodRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Cart persisted as an ordered id -> quantity map. Item details are resolved
    // from the loaded menu when building the UI state.
    private val quantities: LinkedHashMap<Int, Int> =
        LinkedHashMap(savedStateHandle.get<HashMap<Int, Int>>(KEY_CART).orEmpty())

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _events = Channel<OrderEvent>(Channel.BUFFERED)
    val events: Flow<OrderEvent> = _events.receiveAsFlow()

    init {
        loadMenu()
    }

    private fun loadMenu() {
        viewModelScope.launch {
            val menu = repository.getMenu()
            _uiState.update { it.copy(menu = menu, cart = buildCart(menu)) }
        }
    }

    /** Adds one unit of [menuItem] to the cart. */
    fun addToCart(menuItem: MenuItem) {
        quantities[menuItem.id] = (quantities[menuItem.id] ?: 0) + 1
        persistAndPublish()
    }

    /** Removes one unit of [menuItem]; drops the line entirely when it reaches zero. */
    fun decreaseQuantity(menuItem: MenuItem) {
        val current = quantities[menuItem.id] ?: return
        if (current <= 1) {
            quantities.remove(menuItem.id)
        } else {
            quantities[menuItem.id] = current - 1
        }
        persistAndPublish()
    }

    /**
     * Finalizes the order: emits an [OrderEvent.OrderPlaced] with the total and
     * clears the cart. Does nothing when the cart is empty.
     */
    fun placeOrder() {
        if (quantities.isEmpty()) return
        val total = _uiState.value.cartTotal
        quantities.clear()
        persistAndPublish()
        _events.trySend(OrderEvent.OrderPlaced(total))
    }

    private fun persistAndPublish() {
        // LinkedHashMap is Serializable and preserves insertion order across process death.
        savedStateHandle[KEY_CART] = LinkedHashMap(quantities)
        _uiState.update { it.copy(cart = buildCart(it.menu)) }
    }

    /** Resolves the persisted quantities against [menu] into displayable cart lines. */
    private fun buildCart(menu: List<MenuItem>): List<CartItem> {
        if (menu.isEmpty() || quantities.isEmpty()) return emptyList()
        val menuById = menu.associateBy { it.id }
        return quantities.mapNotNull { (id, quantity) ->
            menuById[id]?.let { CartItem(it, quantity) }
        }
    }

    companion object {
        private const val KEY_CART = "cart_quantities"

        /**
         * Factory that wires the repository and a [SavedStateHandle]. Works with
         * navGraphViewModels because Navigation supplies the required CreationExtras.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                OrderViewModel(
                    repository = ServiceLocator.foodRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
