package com.example.myapplication

import android.app.Application
import android.util.Log
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.DatabaseInitializer
import com.example.myapplication.repository.FoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Custom Application class to initialize the database
 */
class MyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize App context provider
        App.initialize(this)
        
        // Initialize database
        val database = AppDatabase.getDatabase(this)
        
        applicationScope.launch(Dispatchers.IO) {
            try {                // Verify database connection
                val shopDao = database.shopInfoDao()
                // Using first() on the Flow to get the first emitted value
                val shops = shopDao.getAllShops().firstOrNull() ?: emptyList()
                val shopCount = shops.size
                Log.d("MyApplication", "DB initialized with $shopCount shops")
                
                // Initialize database with default data if needed
                DatabaseInitializer.initializeDatabase(this@MyApplication)
            } catch (e: Exception) {
                Log.e("MyApplication", "Error initializing database", e)
            }
        }
        
        // Initialize repository
        FoodRepository.getInstance(this)
    }
}
