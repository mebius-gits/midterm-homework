package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.model.ShopInfo

/**
 * Room database for the application
 */
@Database(entities = [ShopInfo::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Get the ShopInfoDao
     */
    abstract fun shopInfoDao(): ShopInfoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
          /**
         * Get the database instance
         * Creates the database if it doesn't exist
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration() // This will recreate the database if migration fails
                .addMigrations(
                    object : androidx.room.migration.Migration(1, 2) {
                        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE shop_info ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                        }
                    },
                    object : androidx.room.migration.Migration(2, 3) {
                        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE shop_info ADD COLUMN rating REAL NOT NULL DEFAULT 0")
                            database.execSQL("ALTER TABLE shop_info ADD COLUMN image_uri TEXT NOT NULL DEFAULT ''")
                        }
                    },
                    object : androidx.room.migration.Migration(3, 4) {
                        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                            // Migration for the latest schema changes
                            // Add the image_uri column if it doesn't exist
                            try {
                                // Check if the image_uri column already exists by querying the table info
                                val cursor = database.query("PRAGMA table_info(shop_info)")
                                var hasImageUri = false
                                
                                while (cursor.moveToNext()) {
                                    val columnName = cursor.getString(cursor.getColumnIndex("name"))
                                    if (columnName == "image_uri") {
                                        hasImageUri = true
                                        break
                                    }
                                }
                                cursor.close()
                                
                                // Only add the column if it doesn't exist
                                if (!hasImageUri) {
                                    database.execSQL("ALTER TABLE shop_info ADD COLUMN image_uri TEXT NOT NULL DEFAULT ''")
                                }
                            } catch (e: Exception) {
                                // Log the error but don't crash
                                android.util.Log.e("AppDatabase", "Error during migration from 3 to 4", e)
                            }
                        }
                    }
                )
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}

