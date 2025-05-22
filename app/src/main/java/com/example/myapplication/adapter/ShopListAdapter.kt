package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemShopListBinding
import com.example.myapplication.model.ShopInfo

/**
 * Adapter for displaying shops in a list
 */
class ShopListAdapter(
    private val onShopClick: (ShopInfo) -> Unit,
    private val onEditClick: (ShopInfo) -> Unit,
    private val onDeleteClick: (ShopInfo) -> Unit,
    private val onCallClick: (String) -> Unit,
    private val onFavoriteClick: (ShopInfo) -> Unit
) : RecyclerView.Adapter<ShopListAdapter.ViewHolder>() {

    private val shops = mutableListOf<ShopInfo>()
    private var selectedShopId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShopListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shops[position], shops[position].id == selectedShopId)
    }

    override fun getItemCount(): Int = shops.size

    fun submitList(shopList: List<ShopInfo>, selectedId: Int?) {
        shops.clear()
        shops.addAll(shopList)
        selectedShopId = selectedId
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemShopListBinding) : 
            RecyclerView.ViewHolder(binding.root) {
          fun bind(shop: ShopInfo, isSelected: Boolean) {
            binding.apply {
                // Set shop information
                shopNameText.text = shop.name
                shopPhoneText.text = "電話：${shop.phone}"
                shopAddressText.text = "地址：${shop.address}"
                shopBusinessHoursText.text = if (shop.businessHours.isNotEmpty())
                    "營業時間：${shop.businessHours}" 
                else 
                    "營業時間：未設定"
                
                // Set rating
                shopRatingBar.rating = shop.rating
                shopRatingText.text = String.format("%.1f", shop.rating)// Set favorite button state
                favoriteButton.setImageResource(
                    if (shop.isFavorite) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_border
                )
                
                // Set up favorite button click listener
                favoriteButton.setOnClickListener {
                    onFavoriteClick(shop)
                }
                
                // Highlight if selected
                root.setOnClickListener {
                    onShopClick(shop)
                }
                
                // Set up edit and delete buttons
                editButton.setOnClickListener {
                    onEditClick(shop)
                }
                
                deleteButton.setOnClickListener {
                    onDeleteClick(shop)
                }
                  // Set up phone number text to be clickable for calling
                shopPhoneText.setOnClickListener {
                    if (shop.phone.isNotEmpty()) {
                        onCallClick(shop.phone)
                    }
                }
                
                // Visual indication of selection
                root.alpha = if (isSelected) 1.0f else 0.8f
            }
        }
    }
}