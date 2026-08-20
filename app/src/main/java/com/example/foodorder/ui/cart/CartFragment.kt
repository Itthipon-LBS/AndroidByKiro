package com.example.foodorder.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import androidx.navigation.navGraphViewModels
import com.example.foodorder.R
import com.example.foodorder.databinding.FragmentCartBinding
import com.example.foodorder.di.ServiceLocator
import com.example.foodorder.ui.order.OrderViewModel
import com.example.foodorder.ui.order.OrderViewModelFactory

/**
 * Shows the current cart, lets the user adjust quantities, and place the order.
 */
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by navGraphViewModels(R.id.nav_graph) {
        OrderViewModelFactory(ServiceLocator.foodRepository)
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
        setupObservers()

        binding.buttonPlaceOrder.setOnClickListener { placeOrder() }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            onIncrease = { viewModel.addToCart(it.menuItem) },
            onDecrease = { viewModel.decreaseQuantity(it.menuItem) }
        )
        binding.recyclerCart.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.cart.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            val isEmpty = items.isEmpty()
            binding.textEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.buttonPlaceOrder.isEnabled = !isEmpty
        }
        viewModel.cartTotal.observe(viewLifecycleOwner) { total ->
            binding.textTotal.text = getString(R.string.cart_total, total)
        }
    }

    private fun placeOrder() {
        val total = viewModel.placeOrder() ?: return
        Toast.makeText(
            requireContext(),
            getString(R.string.order_success, total),
            Toast.LENGTH_LONG
        ).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerCart.adapter = null
        _binding = null
    }
}
