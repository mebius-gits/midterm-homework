package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.CartItem
import com.example.myapplication.model.FoodItem
import com.example.myapplication.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the menu screen
 */
class MenuViewModel : ViewModel() {
    private val repository = FoodRepository.getInstance()

    // Categories with their food items
    val mainDishes = repository.menuItems.map { items ->
        items.filter { it.category == FoodItem.FoodCategory.MAIN_DISH }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sideDishes = repository.menuItems.map { items ->
        items.filter { it.category == FoodItem.FoodCategory.SIDE_DISH }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val drinks = repository.menuItems.map { items ->
        items.filter { it.category == FoodItem.FoodCategory.DRINK }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val others = repository.menuItems.map { items ->
        items.filter { it.category == FoodItem.FoodCategory.OTHER }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Selected food item for details
    private val _selectedFoodItem = MutableStateFlow<FoodItem?>(null)
    val selectedFoodItem: StateFlow<FoodItem?> = _selectedFoodItem

    // Quantity for selected item
    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity

    // Selected size for selected item
    private val _selectedSize = MutableStateFlow(CartItem.Size.MEDIUM)
    val selectedSize: StateFlow<CartItem.Size> = _selectedSize

    // Select a food item for viewing details
    fun selectFoodItem(foodItem: FoodItem) {
        _selectedFoodItem.value = foodItem
        _quantity.value = 1
        _selectedSize.value = CartItem.Size.MEDIUM
    }

    // Clear selected food item
    fun clearSelectedFoodItem() {
        _selectedFoodItem.value = null
    }

    // Update quantity
    fun updateQuantity(amount: Int) {
        val newQuantity = _quantity.value + amount
        if (newQuantity >= 1) {
            _quantity.value = newQuantity
        }
    }

    // Update size
    fun updateSize(size: CartItem.Size) {
        _selectedSize.value = size
    }

    // Add current selected item to cart
    fun addToCart() {
        val foodItem = _selectedFoodItem.value ?: return
        val quantity = _quantity.value
        val size = _selectedSize.value

        repository.addToCart(foodItem, quantity, size)
        clearSelectedFoodItem()
    }
}