package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemShopListBinding
import com.example.myapplication.model.ShopInfo

/**
 * Adapter for displaying shops in a list
 */
class ShopListAdapter(
    private val onShopClick: (ShopInfo) -> Unit,
    private val onEditClick: (ShopInfo) -> Unit,
    private val onDeleteClick: (ShopInfo) -> Unit,
    private val onCallClick: (String) -> Unit
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
                shopBusinessHoursText.text = "營業時間：${shop.businessHours ?: "未設定"}"
                
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
                    shop.phone?.let { phoneNumber ->
                        onCallClick(phoneNumber)
                    }
                }
                
                // Visual indication of selection
                root.alpha = if (isSelected) 1.0f else 0.8f
            }
        }
    }
}