package com.example.foodorder.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.foodorder.R
import com.example.foodorder.databinding.FragmentMenuBinding
import com.example.foodorder.ui.order.OrderUiState
import com.example.foodorder.ui.order.OrderViewModel
import kotlinx.coroutines.launch

/**
 * Displays the food menu. Adding an item delegates to the shared [OrderViewModel].
 */
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // Scoped to the navigation graph so MenuFragment and CartFragment share one instance.
    private val viewModel: OrderViewModel by navGraphViewModels(R.id.nav_graph) {
        OrderViewModel.Factory
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
        observeState()

        binding.buttonViewCart.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_cart)
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(onAddClick = viewModel::addToCart)
        binding.recyclerMenu.adapter = adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: OrderUiState) {
        adapter.submitList(state.menu)
        binding.buttonViewCart.text = getString(R.string.action_view_cart, state.cartCount)
        binding.buttonViewCart.isEnabled = state.cartCount > 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerMenu.adapter = null
        _binding = null
    }
}
