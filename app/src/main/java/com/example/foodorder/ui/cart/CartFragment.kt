package com.example.foodorder.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.foodorder.R
import com.example.foodorder.databinding.FragmentCartBinding
import com.example.foodorder.ui.order.OrderEvent
import com.example.foodorder.ui.order.OrderUiState
import com.example.foodorder.ui.order.OrderViewModel
import kotlinx.coroutines.launch

/**
 * Shows the current cart, lets the user adjust quantities, and place the order.
 */
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by navGraphViewModels(R.id.nav_graph) {
        OrderViewModel.Factory
    }

    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.buttonPlaceOrder.setOnClickListener { viewModel.placeOrder() }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            onIncrease = { viewModel.addToCart(it.menuItem) },
            onDecrease = { viewModel.decreaseQuantity(it.menuItem) }
        )
        binding.recyclerCart.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(it) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }

    private fun render(state: OrderUiState) {
        adapter.submitList(state.cart)
        binding.textEmpty.visibility = if (state.isCartEmpty) View.VISIBLE else View.GONE
        binding.recyclerCart.visibility = if (state.isCartEmpty) View.GONE else View.VISIBLE
        binding.buttonPlaceOrder.isEnabled = !state.isCartEmpty
        binding.textTotal.text = getString(R.string.cart_total, state.cartTotal)
    }

    private fun handleEvent(event: OrderEvent) {
        when (event) {
            is OrderEvent.OrderPlaced -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.order_success, event.total),
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerCart.adapter = null
        _binding = null
    }
}
