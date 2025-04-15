package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for shop information screen
 */
class ShopInfoViewModel : ViewModel() {
    private val repository = FoodRepository.getInstance()
    
    // List of all shops
    val shopInfoList: StateFlow<List<ShopInfo>> = repository.shopInfoList
    
    // Current shop ID
    val currentShopId: StateFlow<Int> = repository.currentShopId
    
    // Current shop information
    val currentShopInfo = repository.shopInfoList.map { shops ->
        shops.find { it.id == repository.currentShopId.value } ?: ShopInfo(
            id = 0,
            name = "",
            phone = "",
            address = "",
            businessHours = ""
        )
    }.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000), 
        repository.getCurrentShopInfo()
    )
    
    // Update shop information
    fun updateShopInfo(
        name: String, 
        phone: String, 
        address: String, 
        businessHours: String,
        shopId: Int = repository.currentShopId.value
    ) {
        val updatedInfo = ShopInfo(
            id = shopId,
            name = name,
            phone = phone,
            address = address,
            businessHours = businessHours
        )
        repository.updateShopInfo(updatedInfo)
    }
    
    // Add new shop
    fun addNewShop(
        name: String,
        phone: String,
        address: String,
        businessHours: String
    ) {
        val newShopId = repository.generateShopId()
        val newShop = ShopInfo(
            id = newShopId,
            name = name,
            phone = phone,
            address = address,
            businessHours = businessHours
        )
        repository.addShop(newShop)
        repository.setCurrentShop(newShopId)
    }
    
    // Delete shop
    fun deleteShop(shopId: Int) {
        repository.deleteShop(shopId)
    }
    
    // Change current shop
    fun setCurrentShop(shopId: Int) {
        repository.setCurrentShop(shopId)
    }
}