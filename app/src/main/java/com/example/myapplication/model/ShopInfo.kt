package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents shop information
 */
@Entity(tableName = "shop_info")
data class ShopInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String,    val businessHours: String,
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    @androidx.room.ColumnInfo(name = "image_uri")    val imageUri: String = "" // Added field for storing image URI
) {
    // Add a companion object to force the class structure to be different from the previous version
    companion object {
        // Helper function to create a shop info instance, ensuring clean instantiation
        fun create(
            id: Int,
            name: String,
            phone: String,
            address: String,
            businessHours: String,
            isFavorite: Boolean = false,
            rating: Float = 0f,
            imageUri: String = "" // Added parameter for image URI
        ): ShopInfo {
            return ShopInfo(id, name, phone, address, businessHours, isFavorite, rating, imageUri)
        }
    }
}