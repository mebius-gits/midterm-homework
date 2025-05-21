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
    @Query("DELETE FROM shop_info WHERE id = :shopId")
    suspend fun deleteShopById(shopId: Int)
    
    /**
     * Get the highest shop ID
     */
    @Query("SELECT MAX(id) FROM shop_info")
    suspend fun getMaxShopId(): Int?
}
