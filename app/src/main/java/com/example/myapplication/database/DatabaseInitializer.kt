package com.example.myapplication.database

import android.content.Context
import com.example.myapplication.model.ShopInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper for migrating initial app data to the database
 */
object DatabaseInitializer {

    private val initialShops = listOf(
        ShopInfo(
            id = 1,
            name = "美食小館",
            phone = "0912345678",
            address = "台中市南區建國北路一段100號",
            businessHours = "10:00 AM - 10:00 PM"
        )
        // Add more initial shops if needed
    )

    /**
     * Initialize the database with default data if it's empty
     */    fun initializeDatabase(context: Context) {
        val shopInfoRepository = ShopInfoRepository(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            // Check if database is empty
            val shops = shopInfoRepository.getAllShops().firstOrNull() ?: emptyList()
            if (shops.isEmpty()) {
                // Insert initial data
                initialShops.forEach { shop ->
                    shopInfoRepository.insertShop(shop)
                }
            }
        }
    }
}
