package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.DbDebugger
import com.example.myapplication.database.ShopInfoRepository
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.repository.FoodRepository
import kotlinx.coroutines.flow.*
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
            shops.find { it.id == currentId } ?: ShopInfo.create(
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
            repository.getCurrentShopInfo()
        )

    // Update shop information
    fun updateShopInfo(
        name: String,
        phone: String,
        address: String,
        businessHours: String,
        shopId: Int = currentShopId.value
    ) {
        viewModelScope.launch {
            try {
                val updatedInfo = ShopInfo.create(
                    id = shopId,
                    name = name,
                    phone = phone,
                    address = address,
                    businessHours = businessHours,
                    isFavorite = repository.getCurrentShopInfo().isFavorite // Preserve favorite status
                )
                repository.updateShopInfo(updatedInfo)
            } catch (e: Exception) {
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
            val newShop = ShopInfo.create(
                id = newShopId,
                name = name,
                phone = phone,
                address = address,
                businessHours = businessHours,
                isFavorite = false
            )
            repository.addShop(newShop)
            repository.setCurrentShop(newShopId)
        }
    }

    // Delete shop
    fun deleteShop(shopId: Int) {
        viewModelScope.launch {
            try {
                val shopBeforeDelete = repository.getCurrentShopInfo()
                repository.deleteShop(shopId)
                // You could reset currentShopId here if the deleted shop was the current one
            } catch (e: Exception) {
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
        viewModelScope.launch {
            val shopInfo = repository.getCurrentShopInfo()
            Log.d("ShopInfoViewModel", "Set current shop: $shopId, Shop: ${shopInfo.name}")
        }
    }

    // Toggle favorite status of a shop
    fun toggleFavoriteShop(shopId: Int) {
        viewModelScope.launch {
            val shopList = shopInfoList.value
            val shop = shopList.find { it.id == shopId }
            val newStatus = !(shop?.isFavorite ?: false)

            repository.toggleFavoriteShop(shopId)

            val context = getApplication<Application>().applicationContext
            val message = if (newStatus) {
                "已將「${shop?.name}」加入收藏"
            } else {
                "已將「${shop?.name}」移出收藏"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
