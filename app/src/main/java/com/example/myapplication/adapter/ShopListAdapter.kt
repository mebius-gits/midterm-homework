package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemShopListBinding
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.util.loadImageWithRoundedCorners

/**
 * Adapter for displaying shops in a list
 */
class ShopListAdapter(
    private val onShopClick: (ShopInfo) -> Unit,
    private val onEditClick: (ShopInfo) -> Unit,
    private val onDeleteClick: (ShopInfo) -> Unit,
    private val onCallClick: (String) -> Unit,
    private val onFavoriteClick: (ShopInfo) -> Unit,
    private val onImageClick: (ShopInfo) -> Unit,
    private val onRatingChanged: (ShopInfo, Float) -> Unit = { _, _ -> }
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
            RecyclerView.ViewHolder(binding.root) {          fun bind(shop: ShopInfo, isSelected: Boolean) {
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
                shopRatingText.text = String.format("%.1f", shop.rating)
                
                // Set up rating bar change listener
                shopRatingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
                    if (fromUser) {
                        onRatingChanged(shop, rating)
                    }
                }
                
                // Set favorite button state
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
                
                imageButton.setOnClickListener {
                    onImageClick(shop)
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
                  // Load shop image if available
                if (shop.imageUri.isNotEmpty()) {
                    try {
                        // Use extension function to load image with rounded corners
                        val uri = android.net.Uri.parse(shop.imageUri)
                        com.example.myapplication.util.loadImageWithRoundedCorners(
                            shopImageView,
                            uri,
                            shopImageView.context.resources.getDimensionPixelSize(R.dimen.shop_image_corner_radius)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        shopImageView.setImageResource(R.drawable.ic_image_placeholder)
                    }
                } else {
                    shopImageView.setImageResource(R.drawable.ic_image_placeholder)
                }
                
                // Visual indication of selection
                root.alpha = if (isSelected) 1.0f else 0.8f
            }
        }
    }
}