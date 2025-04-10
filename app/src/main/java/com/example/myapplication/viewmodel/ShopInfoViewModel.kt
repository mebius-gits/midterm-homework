package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.repository.FoodRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for shop information screen
 */
class ShopInfoViewModel : ViewModel() {
    private val repository = FoodRepository.getInstance()
    
    // Shop information
    val shopInfo: StateFlow<ShopInfo> = repository.shopInfo
    
    // Update shop information
    fun updateShopInfo(name: String, phone: String, address: String, businessHours: String) {
        val updatedInfo = ShopInfo(
            name = name,
            phone = phone,
            address = address,
            businessHours = businessHours
        )
        repository.updateShopInfo(updatedInfo)
    }
}