package com.example.foodorder.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.R
import com.example.foodorder.data.model.CartItem
import com.example.foodorder.databinding.ItemCartBinding

/**
 * RecyclerView adapter for cart lines. Exposes increase/decrease callbacks
 * that the fragment wires to the shared ViewModel.
 */
class CartAdapter(
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding, onIncrease, onDecrease)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        private val binding: ItemCartBinding,
        private val onIncrease: (CartItem) -> Unit,
        private val onDecrease: (CartItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val context = binding.root.context
            binding.textName.text = item.menuItem.name
            binding.textQuantity.text = context.getString(
                R.string.quantity_format, item.quantity
            )
            binding.textLineTotal.text = context.getString(
                R.string.price_format, item.lineTotal
            )
            binding.buttonIncrease.setOnClickListener { onIncrease(item) }
            binding.buttonDecrease.setOnClickListener { onDecrease(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
                oldItem.menuItem.id == newItem.menuItem.id

            override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
                oldItem == newItem
        }
    }
}
