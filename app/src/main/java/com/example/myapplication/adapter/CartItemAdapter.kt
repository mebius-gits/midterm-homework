package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemCartBinding
import com.example.myapplication.model.CartItem

/**
 * Adapter for displaying cart items in a RecyclerView
 */
class CartItemAdapter(
    private val onIncreaseClick: (CartItem) -> Unit,
    private val onDecreaseClick: (CartItem) -> Unit,
    private val onRemoveClick: (CartItem) -> Unit = { onDecreaseClick(it) } // Default to decrease if not provided
) : ListAdapter<CartItem, CartItemAdapter.CartItemViewHolder>(CartItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem)
    }

    inner class CartItemViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.increaseButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onIncreaseClick(getItem(position))
                }
            }

            binding.decreaseButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDecreaseClick(getItem(position))
                }
            }
            
            binding.removeItemButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRemoveClick(getItem(position))
                }
            }
        }

        fun bind(cartItem: CartItem) {
            binding.apply {
                val sizeText = when (cartItem.selectedSize) {
                    CartItem.Size.SMALL -> "(小)"
                    CartItem.Size.MEDIUM -> "(中)"
                    CartItem.Size.LARGE -> "(大)"
                }
                
                // Set main item details
                cartItemName.text = cartItem.foodItem.name
                
                // Set variant info (size and any other details)
                cartItemVariant.text = "Size: $sizeText"
                
                // Set price details
                val unitPrice = cartItem.foodItem.price * when(cartItem.selectedSize) {
                    CartItem.Size.SMALL -> 0.8
                    CartItem.Size.MEDIUM -> 1.0
                    CartItem.Size.LARGE -> 1.3
                }
                
                cartItemPrice.text = "NT$${unitPrice.toInt()}"
                
                // Set quantity
                cartItemQuantity.text = cartItem.quantity.toString()
                
                // If we had images, we would load them here
                // For example with Glide or Picasso
                 Glide.with(cartItemImage.context)
                     .load(cartItem.foodItem.imageUrl)
                     .into(cartItemImage)
            }
        }
    }

    object CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.foodItem.id == newItem.foodItem.id && 
                   oldItem.selectedSize == newItem.selectedSize
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}