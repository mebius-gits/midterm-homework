package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.model.ShopInfo

/**
 * Room database for the application
 */
@Database(entities = [ShopInfo::class], version = 2, exportSchema = false)
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
            return INSTANCE ?: synchronized(this) {                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration()
                .addMigrations(
                    object : androidx.room.migration.Migration(1, 2) {
                        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE shop_info ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
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
