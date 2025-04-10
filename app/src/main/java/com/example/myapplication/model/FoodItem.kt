package com.example.myapplication.model

/**
 * Represents a food item in the menu
 */
data class FoodItem(
    val id: Int,
    val name: String,
    val category: FoodCategory,
    val description: String,
    val rating: Float, // Rating from 0 to 5
    val price: Double,
    val imageUrl: String = "" // Placeholder for image
) {
    enum class FoodCategory {
        MAIN_DISH, SIDE_DISH, DRINK, OTHER
    }
}