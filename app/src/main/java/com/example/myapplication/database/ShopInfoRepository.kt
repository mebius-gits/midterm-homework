package com.example.myapplication.database

import android.content.Context
import com.example.myapplication.model.ShopInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repository for Shop Information database operations
 */
class ShopInfoRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val shopInfoDao = database.shopInfoDao()
    
    /**
     * Get all shops as a Flow
     */
    fun getAllShops(): Flow<List<ShopInfo>> {
        return shopInfoDao.getAllShops()
    }
    
    /**
     * Get a shop by ID
     */
    suspend fun getShopById(shopId: Int): ShopInfo? {
        return shopInfoDao.getShopById(shopId)
    }
    
    /**
     * Insert a new shop
     * @return the ID of the inserted shop
     */
    suspend fun insertShop(shop: ShopInfo): Long {
        return shopInfoDao.insertShop(shop)
    }
    
    /**
     * Insert multiple shops at once
     */
    suspend fun insertAllShops(shops: List<ShopInfo>) {
        shopInfoDao.insertAllShops(shops)
    }    /**
     * Update an existing shop
     * @return true if update was successful
     */
    suspend fun updateShop(shop: ShopInfo): Boolean {
        try {
            // Log the update attempt
            android.util.Log.d("ShopInfoRepository", "Attempting to update shop: ID=${shop.id}, Name=${shop.name}")
            
            // First check if the shop exists
            val existingShop = shopInfoDao.getShopById(shop.id)
            if (existingShop == null) {
                android.util.Log.e("ShopInfoRepository", "Failed to update: shop with ID ${shop.id} doesn't exist")
                return false
            }
            
            // Perform update
            shopInfoDao.updateShop(shop)
            
            // Check if the update was successful by retrieving the updated shop
            val updatedShop = shopInfoDao.getShopById(shop.id)
            val success = updatedShop != null && updatedShop.name == shop.name
            
            if (success) {
                android.util.Log.d("ShopInfoRepository", "Shop updated successfully: ID=${shop.id}, Name=${shop.name}")
            } else {
                android.util.Log.e("ShopInfoRepository", "Shop update verification failed: ID=${shop.id}")
            }
            
            return success
        } catch (e: Exception) {
            android.util.Log.e("ShopInfoRepository", "Error updating shop: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
      /**
     * Delete a shop
     * @return true if deletion was successful
     */
    suspend fun deleteShop(shop: ShopInfo): Boolean {
        try {
            val existingShop = shopInfoDao.getShopById(shop.id)
            if (existingShop != null) {
                shopInfoDao.deleteShop(shop)
                // Verify deletion
                return shopInfoDao.getShopById(shop.id) == null
            }
            return false // Shop not found
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
      /**
     * Delete a shop by ID
     * @return true if deletion was successful
     */
    suspend fun deleteShopById(shopId: Int): Boolean {
        try {
            // Log the deletion attempt
            android.util.Log.d("ShopInfoRepository", "Attempting to delete shop with ID: $shopId")
            
            val existingShop = shopInfoDao.getShopById(shopId)
            if (existingShop != null) {
                // Log shop details before deletion
                android.util.Log.d("ShopInfoRepository", "Found shop to delete: ID=$shopId, Name=${existingShop.name}")
                
                shopInfoDao.deleteShopById(shopId)
                
                // Verify deletion
                val success = shopInfoDao.getShopById(shopId) == null
                if (success) {
                    android.util.Log.d("ShopInfoRepository", "Successfully deleted shop with ID: $shopId")
                } else {
                    android.util.Log.e("ShopInfoRepository", "Failed to delete shop with ID: $shopId (verification failed)")
                }
                return success
            }
            
            android.util.Log.e("ShopInfoRepository", "Cannot delete: shop with ID $shopId not found")
            return false // Shop not found
        } catch (e: Exception) {
            android.util.Log.e("ShopInfoRepository", "Error deleting shop with ID $shopId: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Generate a new shop ID
     */    suspend fun generateShopId(): Int {
        try {
            val maxId = shopInfoDao.getMaxShopId() ?: 0
            return maxId + 1
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to a safe default if there's a database error
            return System.currentTimeMillis().toInt() % 10000 + 1 // Use timestamp as a unique ID fallback
        }
    }
}
