package com.example.foodorder.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.foodorder.R
import com.example.foodorder.databinding.FragmentMenuBinding
import com.example.foodorder.di.ServiceLocator
import com.example.foodorder.ui.order.OrderViewModel
import com.example.foodorder.ui.order.OrderViewModelFactory

/**
 * Displays the food menu. Adding an item delegates to the shared [OrderViewModel].
 */
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // Scoped to the navigation graph so MenuFragment and CartFragment share one instance.
    private val viewModel: OrderViewModel by navGraphViewModels(R.id.nav_graph) {
        OrderViewModelFactory(ServiceLocator.foodRepository)
    }

    private lateinit var adapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()

        binding.buttonViewCart.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_cart)
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(onAddClick = viewModel::addToCart)
        binding.recyclerMenu.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.menu)
            binding.buttonViewCart.text =
                getString(R.string.action_view_cart, state.cartCount)
            binding.buttonViewCart.isEnabled = state.cartCount > 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerMenu.adapter = null
        _binding = null
    }
}
