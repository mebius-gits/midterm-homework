package com.example.myapplication.model

/**
 * Represents an item in the shopping cart
 */
data class CartItem(
    val foodItem: FoodItem,
    val quantity: Int,
    val selectedSize: Size = Size.MEDIUM
) {
    // Calculate the total price for this cart item
    fun getTotalPrice(): Double = foodItem.price * quantity * when(selectedSize) {
        Size.SMALL -> 0.8
        Size.MEDIUM -> 1.0
        Size.LARGE -> 1.3
    }
    
    enum class Size {
        SMALL, MEDIUM, LARGE
    }
}