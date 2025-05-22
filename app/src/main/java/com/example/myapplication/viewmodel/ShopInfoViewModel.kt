package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.DbDebugger
import com.example.myapplication.database.ShopInfoRepository
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for shop information screen
 */
class ShopInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FoodRepository.getInstance(application)
    
    // List of all shops
    val shopInfoList: StateFlow<List<ShopInfo>> = repository.shopInfoList
      // Current shop ID
    val currentShopId: StateFlow<Int> = repository.currentShopId
      // Current shop information - combine shopInfoList and currentShopId for reactive updates
    val currentShopInfo: StateFlow<ShopInfo> = shopInfoList
        .combine(currentShopId) { shops, currentId ->
            shops.find { it.id == currentId } ?: ShopInfo(
                id = 0,
                name = "",
                phone = "",
                address = "",
                businessHours = "",
                isFavorite = false
            )
        }
        .stateIn(
            viewModelScope, 
            SharingStarted.WhileSubscribed(5000), 
            ShopInfo(
                id = 0,
                name = "",
                phone = "",
                address = "",
                businessHours = "",
                isFavorite = false
            )
        )
      // Update shop information
    fun updateShopInfo(
        name: String, 
        phone: String, 
        address: String, 
        businessHours: String,
        shopId: Int = repository.currentShopId.value
    ) {
        viewModelScope.launch {
            try {
                val updatedInfo = ShopInfo(
                    id = shopId,
                    name = name,
                    phone = phone,
                    address = address,
                    businessHours = businessHours
                )
                repository.updateShopInfo(updatedInfo)
            } catch (e: Exception) {
                // Log error or handle it
                e.printStackTrace()
            }
        }
    }
      // Add new shop
    fun addNewShop(
        name: String,
        phone: String,
        address: String,
        businessHours: String
    ) {
        viewModelScope.launch {
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
    }    // Delete shop
    fun deleteShop(shopId: Int) {
        viewModelScope.launch {
            try {
                // Log shop details before deletion for debugging
                val shopBeforeDelete = repository.getCurrentShopInfo()
                if (shopBeforeDelete.id == shopId) {
                    // This is the current shop, make sure we handle it properly
                    repository.deleteShop(shopId)
                } else {
                    // Not the current shop
                    repository.deleteShop(shopId)
                }
            } catch (e: Exception) {
                // Log error or handle it
                e.printStackTrace()
            }
        }
    }
    
    // Debug function to show all shops
    fun debugShowAllShops(context: Context) {
        viewModelScope.launch {
            val shopInfoRepository = ShopInfoRepository(context)
            DbDebugger.logAllShops(context, shopInfoRepository, true)
        }
    }
      // Change current shop
    fun setCurrentShop(shopId: Int) {
        repository.setCurrentShop(shopId)
        // Log the current shop info for debugging
        viewModelScope.launch {
            val shopInfo = repository.getCurrentShopInfo()
            android.util.Log.d("ShopInfoViewModel", "Set current shop: $shopId, Shop: ${shopInfo.name}")
        }
    }    // Toggle favorite status of a shop
    fun toggleFavoriteShop(shopId: Int) {
        viewModelScope.launch {
            // Get current shop to show message with the name and new status
            val shop = repository.shopInfoList.value.find { it.id == shopId }
            val newStatus = !(shop?.isFavorite ?: false)
            
            repository.toggleFavoriteShop(shopId)
            
            // Show toast with the result
            val context = getApplication<android.app.Application>().applicationContext
            val message = if (newStatus) "已將「${shop?.name}」加入收藏"
                         else "已將「${shop?.name}」移出收藏"
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }    // Update shop rating
    fun updateShopRating(shopId: Int, rating: Float) {
        viewModelScope.launch {
            try {
                // Get shop name for toast message
                val shop = repository.shopInfoList.value.find { it.id == shopId }
                
                // Update the rating
                repository.updateShopRating(shopId, rating)
                
                // Show toast with the result
                val context = getApplication<android.app.Application>().applicationContext
                val message = "已將「${shop?.name}」評分更新為 ${rating}"
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}