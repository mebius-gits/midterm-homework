package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.DbDebugger
import com.example.myapplication.database.ShopInfoRepository
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.repository.FoodRepository
import com.example.myapplication.util.UIEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for shop information screen
 */
class ShopInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FoodRepository.getInstance(application)
    
    // UI Events channel
    private val _uiEvent = Channel<UIEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    
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
        )    // Update shop information
    fun updateShopInfo(
        name: String, 
        phone: String, 
        address: String, 
        businessHours: String,
        shopId: Int = repository.currentShopId.value
    ) {
        viewModelScope.launch {
            try {
                // Get the current shop to preserve the imageUri and other fields
                val currentShop = shopInfoList.value.find { it.id == shopId }
                
                val updatedInfo = ShopInfo(
                    id = shopId,
                    name = name,
                    phone = phone,
                    address = address,
                    businessHours = businessHours,
                    isFavorite = currentShop?.isFavorite ?: false,
                    rating = currentShop?.rating ?: 0f,
                    imageUri = currentShop?.imageUri ?: ""
                )
                repository.updateShopInfo(updatedInfo)
                  // Send success UI event
                _uiEvent.send(UIEvent.ShowSnackbar("✅ 商家「$name」資訊更新成功"))
            } catch (e: Exception) {
                // Log error and send error UI event
                e.printStackTrace()
                _uiEvent.send(UIEvent.ShowSnackbar("❌ 更新商家資訊失敗，請檢查網路連線後重試", true))
            }
        }
    }    // Add new shop
    fun addNewShop(
        name: String,
        phone: String,
        address: String,
        businessHours: String
    ) {
        viewModelScope.launch {
            try {
                val newShopId = repository.generateShopId()
                val newShop = ShopInfo(
                    id = newShopId,
                    name = name,
                    phone = phone,
                    address = address,
                    businessHours = businessHours,
                    isFavorite = false,
                    rating = 0f,
                    imageUri = ""  // Default empty image URI
                )
                repository.addShop(newShop)
                repository.setCurrentShop(newShopId)
                  // Send success UI event
                _uiEvent.send(UIEvent.ShowSnackbar("🎉 商家「$name」新增成功，已設為當前商家"))
            } catch (e: Exception) {
                // Log error and send error UI event
                e.printStackTrace()
                _uiEvent.send(UIEvent.ShowSnackbar("❌ 新增商家失敗，請檢查資料格式後重試", true))
            }
        }
    }// Delete shop
    fun deleteShop(shopId: Int) {
        viewModelScope.launch {
            try {
                // Get shop name for the success message
                val shopToDelete = repository.shopInfoList.value.find { it.id == shopId }
                val shopName = shopToDelete?.name ?: "商家"
                
                // Log shop details before deletion for debugging
                val shopBeforeDelete = repository.getCurrentShopInfo()
                if (shopBeforeDelete.id == shopId) {
                    // This is the current shop, make sure we handle it properly
                    repository.deleteShop(shopId)
                } else {
                    // Not the current shop
                    repository.deleteShop(shopId)
                }
                  // Send success UI event
                _uiEvent.send(UIEvent.ShowSnackbar("🗑️ 商家「$shopName」已成功刪除"))
            } catch (e: Exception) {
                // Log error and send error UI event
                e.printStackTrace()
                _uiEvent.send(UIEvent.ShowSnackbar("❌ 刪除商家失敗，請稍後重試", true))
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
            android.util.Log.d("ShopInfoViewModel", "Set current shop: $shopId, Shop: ${shopInfo.name}")        }
    }
    
    // Toggle favorite status of a shop
    fun toggleFavoriteShop(shopId: Int) {
        viewModelScope.launch {
            // Get current shop to show message with the name and new status
            val shop = repository.shopInfoList.value.find { it.id == shopId }
            val newStatus = !(shop?.isFavorite ?: false)
            
            repository.toggleFavoriteShop(shopId)            // Send UI event instead of showing Toast directly
            val message = if (newStatus) "❤️ 「${shop?.name}」已加入我的最愛清單"
                         else "💔 「${shop?.name}」已從最愛清單中移除"
            _uiEvent.send(UIEvent.ShowSnackbar(message))}
    }
    
    // Update shop rating
    fun updateShopRating(shopId: Int, rating: Float) {
        viewModelScope.launch {
            try {
                // Get shop name for message
                val shop = repository.shopInfoList.value.find { it.id == shopId }
                
                // Update the rating
                repository.updateShopRating(shopId, rating)
                  // Send UI event instead of showing Toast directly
                val ratingText = "%.1f".format(rating)
                val stars = when {
                    rating >= 4.5f -> "⭐⭐⭐⭐⭐"
                    rating >= 3.5f -> "⭐⭐⭐⭐"
                    rating >= 2.5f -> "⭐⭐⭐"
                    rating >= 1.5f -> "⭐⭐"
                    rating >= 0.5f -> "⭐"
                    else -> ""
                }
                val message = "⭐ 已將「${shop?.name}」評分設為 $ratingText 分 $stars"
                _uiEvent.send(UIEvent.ShowSnackbar(message))
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvent.send(UIEvent.ShowSnackbar("❌ 評分更新失敗，請重試", true))
            }
        }
    }    // Update shop image
    fun updateShopImage(shopId: Int, imageUri: String) {
        viewModelScope.launch {
            try {                repository.updateShopImage(shopId, imageUri)
                
                // Send UI event instead of showing Toast directly
                _uiEvent.send(UIEvent.ShowSnackbar("📸 店家圖片更新成功"))
            } catch (e: Exception) {
                e.printStackTrace()
                
                // Send UI event for error instead of showing Toast directly
                _uiEvent.send(UIEvent.ShowSnackbar("❌ 圖片更新失敗: ${e.message}", true))
            }
        }
    }
}