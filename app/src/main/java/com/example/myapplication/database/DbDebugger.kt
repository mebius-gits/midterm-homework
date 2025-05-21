package com.example.myapplication.database

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.myapplication.model.ShopInfo
import kotlinx.coroutines.flow.first

/**
 * Utility class for debugging database operations
 */
object DbDebugger {
    private const val TAG = "DbDebugger"
    
    /**
     * Log all shops in the database
     * @param context The application context
     * @param shopInfoRepository The repository to use
     * @param showToast Whether to show a toast with the results
     */
    suspend fun logAllShops(
        context: Context, 
        shopInfoRepository: ShopInfoRepository,
        showToast: Boolean = false
    ) {        try {
            // Collect the first value from the Flow
            val shops = shopInfoRepository.getAllShops().first()
            val message = if (shops.isEmpty()) {
                "資料庫中無商家資料"
            } else {
                buildString {
                    append("資料庫中有 ${shops.size} 個商家:\n")
                    for (shop in shops) {
                        append("ID=${shop.id}, 名稱=${shop.name}\n")
                    }
                }
            }
            
            Log.d(TAG, message)
            
            if (showToast) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "讀取商家資料時發生錯誤", e)
            if (showToast) {
                Toast.makeText(context, "無法讀取商家資料: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Log a specific shop in the database
     * @param context The application context
     * @param shopInfoRepository The repository to use
     * @param shopId The ID of the shop to log
     * @param showToast Whether to show a toast with the results
     */
    suspend fun logShopDetails(
        context: Context, 
        shopInfoRepository: ShopInfoRepository,
        shopId: Int,
        showToast: Boolean = false
    ) {
        try {
            val shop = shopInfoRepository.getShopById(shopId)
            val message = if (shop == null) {
                "找不到 ID=$shopId 的商家"
            } else {
                "商家資訊：\n" +
                "ID: ${shop.id}\n" +
                "名稱: ${shop.name}\n" +
                "電話: ${shop.phone}\n" +
                "地址: ${shop.address}\n" +
                "營業時間: ${shop.businessHours}"
            }
            
            Log.d(TAG, message)
            
            if (showToast) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "讀取商家資料時發生錯誤", e)
            if (showToast) {
                Toast.makeText(context, "無法讀取商家資料: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Verify if a shop exists in the database
     * @param shopInfoRepository The repository to use
     * @param shopId The ID of the shop to check
     * @return True if the shop exists, false otherwise
     */
    suspend fun verifyShopExists(
        shopInfoRepository: ShopInfoRepository,
        shopId: Int
    ): Boolean {
        return try {
            val shop = shopInfoRepository.getShopById(shopId)
            shop != null
        } catch (e: Exception) {
            Log.e(TAG, "驗證商家存在時發生錯誤", e)
            false
        }
    }
}
