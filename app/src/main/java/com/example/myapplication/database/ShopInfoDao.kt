package com.example.myapplication.database

import androidx.room.*
import com.example.myapplication.model.ShopInfo
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for shop_info table
 */
@Dao
interface ShopInfoDao {
    /**
     * Get all shops as a Flow
     */
    @Query("SELECT * FROM shop_info")
    fun getAllShops(): Flow<List<ShopInfo>>

    /**
     * Get a specific shop by ID
     */
    @Query("SELECT * FROM shop_info WHERE id = :shopId")
    suspend fun getShopById(shopId: Int): ShopInfo?

    /**
     * Insert a new shop
     * @return the ID of the inserted shop
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopInfo): Long
    
    /**
     * Insert multiple shops at once
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllShops(shops: List<ShopInfo>)
    
    /**
     * Update an existing shop
     */
    @Update
    suspend fun updateShop(shop: ShopInfo)
    
    /**
     * Delete a shop
     */
    @Delete
    suspend fun deleteShop(shop: ShopInfo)
      /**
     * Delete a shop by ID
     */
    
    /**
     * Toggle favorite status for a shop
     */
    @Query("UPDATE shop_info SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :shopId")
    suspend fun toggleFavorite(shopId: Int)
    
    /**
     * Get all favorite shops
     */
    @Query("SELECT * FROM shop_info WHERE isFavorite = 1")
    fun getFavoriteShops(): Flow<List<ShopInfo>>
    @Query("DELETE FROM shop_info WHERE id = :shopId")
    suspend fun deleteShopById(shopId: Int)
      /**
     * Get the highest shop ID
     */
    @Query("SELECT MAX(id) FROM shop_info")
    suspend fun getMaxShopId(): Int?
    
    /**
     * Update the rating for a shop
     */
    @Query("UPDATE shop_info SET rating = :rating WHERE id = :shopId")
    suspend fun updateShopRating(shopId: Int, rating: Float)    /**
     * Update image URI for a shop
     */
    @Query("UPDATE shop_info SET image_uri = :imageUri WHERE id = :shopId")
    suspend fun updateShopImage(shopId: Int, imageUri: String)
}
