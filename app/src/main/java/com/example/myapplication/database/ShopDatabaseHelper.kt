package com.example.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication.model.ShopInfo

/**
 * A direct SQLite helper class for raw SQLite operations
 * This provides an alternative to Room for direct SQL access if needed
 */
class ShopDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {    companion object {        private const val DATABASE_NAME = "shop_database"
        private const val DATABASE_VERSION = 4 // Incremented version for the database schema change
        private const val TABLE_SHOPS = "shops"
        
        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_BUSINESS_HOURS = "business_hours"
        private const val COLUMN_IS_FAVORITE = "is_favorite"
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_IMAGE_URI = "image_uri" // New column for storing image URI
    }
      override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_SHOPS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_BUSINESS_HOURS TEXT,
                $COLUMN_IS_FAVORITE INTEGER DEFAULT 0,
                $COLUMN_RATING REAL DEFAULT 0,
                $COLUMN_IMAGE_URI TEXT DEFAULT ''
            )
        """.trimIndent()
        
        try {
            db.execSQL(createTableQuery)
        } catch (e: Exception) {
            Log.e("ShopDatabaseHelper", "Error creating database", e)
        }
    }
      override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add isFavorite column if upgrading from version 1 to 2
            try {
                db.execSQL("ALTER TABLE $TABLE_SHOPS ADD COLUMN $COLUMN_IS_FAVORITE INTEGER DEFAULT 0")
                Log.d("ShopDatabaseHelper", "Successfully added $COLUMN_IS_FAVORITE column")
            } catch (e: Exception) {
                Log.e("ShopDatabaseHelper", "Error adding isFavorite column", e)
                // If column addition fails, recreate the table (data loss will occur)
                db.execSQL("DROP TABLE IF EXISTS $TABLE_SHOPS")
                onCreate(db)
            }
        }
          if (oldVersion < 3) {
            // Add IMAGE_URI and RATING columns if upgrading from version 2 to 3
            try {
                db.execSQL("ALTER TABLE $TABLE_SHOPS ADD COLUMN $COLUMN_RATING REAL DEFAULT 0")
                db.execSQL("ALTER TABLE $TABLE_SHOPS ADD COLUMN $COLUMN_IMAGE_URI TEXT DEFAULT ''")
                Log.d("ShopDatabaseHelper", "Successfully added $COLUMN_RATING and $COLUMN_IMAGE_URI columns")
            } catch (e: Exception) {
                Log.e("ShopDatabaseHelper", "Error adding new columns", e)
                // If column addition fails, recreate the table (data loss will occur)
                db.execSQL("DROP TABLE IF EXISTS $TABLE_SHOPS")
                onCreate(db)
            }
        }
          if (oldVersion < 4) {
            // Handle migration from version 3 to 4
            // Check if the image_uri column exists and add it if necessary
            try {
                // Check if image_uri column already exists
                val cursor = db.rawQuery("PRAGMA table_info($TABLE_SHOPS)", null)
                var hasImageUri = false
                
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndex("name"))
                    if (columnName == COLUMN_IMAGE_URI) {
                        hasImageUri = true
                        break
                    }
                }
                cursor.close()
                
                // Add the column if it doesn't exist
                if (!hasImageUri) {
                    db.execSQL("ALTER TABLE $TABLE_SHOPS ADD COLUMN $COLUMN_IMAGE_URI TEXT DEFAULT ''")
                    Log.d("ShopDatabaseHelper", "Added missing $COLUMN_IMAGE_URI column during migration to version 4")
                }
                
                Log.d("ShopDatabaseHelper", "Successfully migrated from version 3 to 4")
            } catch (e: Exception) {
                Log.e("ShopDatabaseHelper", "Error migrating to version 4", e)
                // In case of a critical error, consider recreating the table
                // db.execSQL("DROP TABLE IF EXISTS $TABLE_SHOPS")
                // onCreate(db)
            }
        }
    }
      /**
     * Insert a new shop into the database
     */
    fun insertShop(shop: ShopInfo): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            // Don't include ID if it's 0 (let SQLite auto-increment)
            if (shop.id > 0) {
                put(COLUMN_ID, shop.id)
            }
            put(COLUMN_NAME, shop.name)
            put(COLUMN_PHONE, shop.phone)
            put(COLUMN_ADDRESS, shop.address)
            put(COLUMN_BUSINESS_HOURS, shop.businessHours)
            put(COLUMN_IS_FAVORITE, if (shop.isFavorite) 1 else 0)
            put(COLUMN_RATING, shop.rating)
            put(COLUMN_IMAGE_URI, shop.imageUri)
        }
        
        return db.insert(TABLE_SHOPS, null, values)
    }
    
    /**
     * Get all shops from the database
     */
    fun getAllShops(): List<ShopInfo> {
        val shopList = mutableListOf<ShopInfo>()
        val query = "SELECT * FROM $TABLE_SHOPS"
        val db = this.readableDatabase
        
        val cursor = db.rawQuery(query, null)
        
        if (cursor.moveToFirst()) {            val idColumnIndex = cursor.getColumnIndex(COLUMN_ID)
            val nameColumnIndex = cursor.getColumnIndex(COLUMN_NAME)
            val phoneColumnIndex = cursor.getColumnIndex(COLUMN_PHONE)
            val addressColumnIndex = cursor.getColumnIndex(COLUMN_ADDRESS)
            val businessHoursColumnIndex = cursor.getColumnIndex(COLUMN_BUSINESS_HOURS)
            val isFavoriteColumnIndex = cursor.getColumnIndex(COLUMN_IS_FAVORITE)
            val ratingColumnIndex = cursor.getColumnIndex(COLUMN_RATING)
            val imageUriColumnIndex = cursor.getColumnIndex(COLUMN_IMAGE_URI)
            
            do {
                val id = cursor.getInt(idColumnIndex)
                val name = cursor.getString(nameColumnIndex)
                val phone = cursor.getString(phoneColumnIndex)
                val address = cursor.getString(addressColumnIndex)
                val businessHours = cursor.getString(businessHoursColumnIndex)
                val isFavorite = if (isFavoriteColumnIndex != -1) cursor.getInt(isFavoriteColumnIndex) == 1 else false
                val rating = if (ratingColumnIndex != -1) cursor.getFloat(ratingColumnIndex) else 0f
                val imageUri = if (imageUriColumnIndex != -1) cursor.getString(imageUriColumnIndex) ?: "" else ""
                
                val shop = ShopInfo(
                    id = id,
                    name = name,
                    phone = phone,
                    address = address,
                    businessHours = businessHours,
                    isFavorite = isFavorite,
                    rating = rating,
                    imageUri = imageUri
                )
                shopList.add(shop)
            } while (cursor.moveToNext())
        }
        
        cursor.close()
        return shopList
    }
    
    /**
     * Get a shop by ID
     */
    fun getShopById(shopId: Int): ShopInfo? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_SHOPS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(shopId.toString()),
            null,
            null,
            null
        )
        
        var shop: ShopInfo? = null
        
        if (cursor.moveToFirst()) {            val idColumnIndex = cursor.getColumnIndex(COLUMN_ID)
            val nameColumnIndex = cursor.getColumnIndex(COLUMN_NAME)
            val phoneColumnIndex = cursor.getColumnIndex(COLUMN_PHONE)
            val addressColumnIndex = cursor.getColumnIndex(COLUMN_ADDRESS)
            val businessHoursColumnIndex = cursor.getColumnIndex(COLUMN_BUSINESS_HOURS)
            val isFavoriteColumnIndex = cursor.getColumnIndex(COLUMN_IS_FAVORITE)
            val ratingColumnIndex = cursor.getColumnIndex(COLUMN_RATING)
            val imageUriColumnIndex = cursor.getColumnIndex(COLUMN_IMAGE_URI)
            
            val id = cursor.getInt(idColumnIndex)
            val name = cursor.getString(nameColumnIndex)
            val phone = cursor.getString(phoneColumnIndex)
            val address = cursor.getString(addressColumnIndex)
            val businessHours = cursor.getString(businessHoursColumnIndex)
            val isFavorite = if (isFavoriteColumnIndex != -1) cursor.getInt(isFavoriteColumnIndex) == 1 else false
            val rating = if (ratingColumnIndex != -1) cursor.getFloat(ratingColumnIndex) else 0f
            val imageUri = if (imageUriColumnIndex != -1) cursor.getString(imageUriColumnIndex) ?: "" else ""
            
            shop = ShopInfo(
                id = id,
                name = name,
                phone = phone,
                address = address,
                businessHours = businessHours,
                isFavorite = isFavorite,
                rating = rating,
                imageUri = imageUri
            )
        }
        
        cursor.close()
        return shop
    }
      /**
     * Update an existing shop
     */
    fun updateShop(shop: ShopInfo): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, shop.name)
            put(COLUMN_PHONE, shop.phone)
            put(COLUMN_ADDRESS, shop.address)
            put(COLUMN_BUSINESS_HOURS, shop.businessHours)
            put(COLUMN_IS_FAVORITE, if (shop.isFavorite) 1 else 0)
            put(COLUMN_RATING, shop.rating)
            put(COLUMN_IMAGE_URI, shop.imageUri)
        }
        
        return db.update(
            TABLE_SHOPS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(shop.id.toString())
        )
    }
      /**
     * Delete a shop by ID
     */
    fun deleteShop(shopId: Int): Int {
        val db = this.writableDatabase
        return db.delete(
            TABLE_SHOPS,
            "$COLUMN_ID = ?",
            arrayOf(shopId.toString())
        )
    }
    
    /**
     * Toggle favorite status for a shop
     */
    fun toggleFavorite(shopId: Int): Int {
        val shop = getShopById(shopId)
        return if (shop != null) {
            val updatedShop = shop.copy(isFavorite = !shop.isFavorite)
            updateShop(updatedShop)
        } else {
            0  // No rows affected if shop doesn't exist
        }
    }
}
