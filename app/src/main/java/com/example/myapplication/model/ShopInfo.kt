package com.example.myapplication.model

/**
 * Represents shop information
 */
data class ShopInfo(
    val id: Int,
    val name: String,
    val phone: String,
    val address: String,
    val businessHours: String
) {
    // Add a companion object to force the class structure to be different from the previous version
    companion object {
        // Helper function to create a shop info instance, ensuring clean instantiation
        fun create(
            id: Int,
            name: String,
            phone: String,
            address: String,
            businessHours: String
        ): ShopInfo {
            return ShopInfo(id, name, phone, address, businessHours)
        }
    }
}