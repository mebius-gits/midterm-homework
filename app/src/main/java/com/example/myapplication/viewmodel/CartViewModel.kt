package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.CartItem
import com.example.myapplication.repository.FoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the cart screen
 */
class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FoodRepository.getInstance(application)
    
    // Cart items
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
    
    // Total price calculation
    val totalPrice = repository.cartItems.map { items ->
        items.sumOf { it.getTotalPrice() }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    // Update quantity of an item in the cart
    fun updateItemQuantity(cartItem: CartItem, newQuantity: Int) {
        repository.updateCartItemQuantity(cartItem, newQuantity)
    }
    
    // Remove an item from the cart
    fun removeItem(cartItem: CartItem) {
        repository.removeFromCart(cartItem)
    }
    
    // Clear the cart
    fun clearCart() {
        val items = cartItems.value.toList()
        items.forEach { 
            repository.removeFromCart(it)
        }
    }
    
    // Increase item quantity
    fun increaseQuantity(cartItem: CartItem) {
        repository.updateCartItemQuantity(cartItem, cartItem.quantity + 1)
    }
    
    // Decrease item quantity
    fun decreaseQuantity(cartItem: CartItem) {
        if (cartItem.quantity > 1) {
            repository.updateCartItemQuantity(cartItem, cartItem.quantity - 1)
        } else {
            repository.removeFromCart(cartItem)
        }
    }
    
    // Check if cart is empty
    fun isCartEmpty(): Boolean = cartItems.value.isEmpty()
}