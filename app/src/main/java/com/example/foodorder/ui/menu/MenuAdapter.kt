package com.example.foodorder.ui.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.data.model.MenuItem
import com.example.foodorder.databinding.ItemMenuBinding

/**
 * RecyclerView adapter for the food menu. Uses [ListAdapter] with [DiffUtil]
 * so list updates are computed off the UI thread and animated automatically.
 */
class MenuAdapter(
    private val onAddClick: (MenuItem) -> Unit
) : ListAdapter<MenuItem, MenuAdapter.MenuViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MenuViewHolder(binding, onAddClick)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MenuViewHolder(
        private val binding: ItemMenuBinding,
        private val onAddClick: (MenuItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {
            val context = binding.root.context
            binding.textName.text = item.name
            binding.textDescription.text = item.description
            binding.textPrice.text = context.getString(
                com.example.foodorder.R.string.price_format, item.price
            )
            binding.buttonAdd.setOnClickListener { onAddClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MenuItem>() {
            override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean =
                oldItem == newItem
        }
    }
}
