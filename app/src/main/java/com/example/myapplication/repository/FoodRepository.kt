package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.R
import com.example.myapplication.database.ShopInfoRepository
import com.example.myapplication.model.CartItem
import com.example.myapplication.model.FoodItem
import com.example.myapplication.model.ShopInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repository for managing food menu and cart data
 */
class FoodRepository private constructor(context: Context) {
    // Database repository for shop info
    private val shopInfoRepository = ShopInfoRepository(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    // Sample food items
    private val _menuItems = MutableStateFlow<List<FoodItem>>(
        listOf(
            // Main dishes
            FoodItem(
                id = 1,
                name = "招牌牛肉麵",
                category = FoodItem.FoodCategory.MAIN_DISH,
                description = "特級牛肉搭配自製麵條和湯頭，香氣十足。",
                rating = 4.5f,
                price = 200.0,
                imageUrl = R.drawable.p000,
            ),
            FoodItem(
                id = 2,
                name = "紅燒排骨飯",
                category = FoodItem.FoodCategory.MAIN_DISH,
                description = "嚴選新鮮豬排，特調醬汁燉煮入味，搭配白飯更美味。",
                rating = 4.0f,
                price = 180.0,
                imageUrl = R.drawable.p001,
            ),
            FoodItem(
                id = 3,
                name = "三杯雞飯",
                category = FoodItem.FoodCategory.MAIN_DISH,
                description = "使用三杯醬汁烹調的嫩雞肉，香氣四溢、回味無窮。",
                rating = 4.2f,
                price = 160.0,
                imageUrl = R.drawable.p002,
            ),
            
            // Side dishes
            FoodItem(
                id = 4,
                name = "椒鹽雞翅",
                category = FoodItem.FoodCategory.SIDE_DISH,
                description = "酥脆多汁的雞翅，香辣可口。",
                rating = 3.5f,
                price = 100.0,
                imageUrl = R.drawable.p002,
            ),
            FoodItem(
                id = 5,
                name = "蒜香炸薯條",
                category = FoodItem.FoodCategory.SIDE_DISH,
                description = "新鮮馬鈴薯製成，外酥內軟，灑上特製蒜香調味粉。",
                rating = 4.0f,
                price = 90.0,
                imageUrl = R.drawable.p000,
            ),
            FoodItem(
                id = 6,
                name = "涼拌小黃瓜",
                category = FoodItem.FoodCategory.SIDE_DISH,
                description = "清脆爽口的小黃瓜，淋上特調醬汁，開胃解膩。",
                rating = 3.7f,
                price = 60.0,
                imageUrl = R.drawable.p000,
            ),
            
            // Drinks
            FoodItem(
                id = 7,
                name = "珍珠奶茶",
                category = FoodItem.FoodCategory.DRINK,
                description = "台灣特色飲品，香濃奶茶搭配QQ珍珠。",
                rating = 5.0f,
                price = 50.0,
                imageUrl = R.drawable.p000,
            ),
            FoodItem(
                id = 8,
                name = "芒果冰沙",
                category = FoodItem.FoodCategory.DRINK,
                description = "使用新鮮芒果製成，香甜清涼，炎夏必備。",
                rating = 4.8f,
                price = 70.0,
                imageUrl = R.drawable.p000,
            ),
            FoodItem(
                id = 9,
                name = "檸檬綠茶",
                category = FoodItem.FoodCategory.DRINK,
                description = "清爽的綠茶搭配新鮮檸檬片，消暑解渴。",
                rating = 4.2f,
                price = 45.0,
                imageUrl = R.drawable.p000,
            ),
            
            // Others
            FoodItem(
                id = 10,
                name = "手作芋圓甜品",
                category = FoodItem.FoodCategory.OTHER,
                description = "每日新鮮製作的芋圓，搭配香濃奶茶和紅豆，口感豐富。",
                rating = 4.5f,
                price = 80.0,
                imageUrl = R.drawable.p000,
            ),
            FoodItem(
                id = 11,
                name = "豆花",
                category = FoodItem.FoodCategory.OTHER,
                description = "滑嫩的豆花，配上花生、紅豆等多種配料，甜度可調。",
                rating = 4.3f,
                price = 65.0,
                imageUrl = R.drawable.p000,
            ),
            FoodItem(
                id = 12,
                name = "提拉米蘇",
                category = FoodItem.FoodCategory.OTHER,
                description = "義式經典甜點，層次豐富，咖啡香濃郁。",
                rating = 4.7f,
                price = 90.0,
                imageUrl = R.drawable.p000,
            )
        )
    )
    val menuItems: StateFlow<List<FoodItem>> = _menuItems.asStateFlow()

    // Cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()    // Multiple shop information entries from database
    private val _shopInfoList = MutableStateFlow<List<ShopInfo>>(emptyList())
    val shopInfoList: StateFlow<List<ShopInfo>> = _shopInfoList.asStateFlow()
    
    // Currently selected shop
    private val _currentShopId = MutableStateFlow(1)
    val currentShopId: StateFlow<Int> = _currentShopId.asStateFlow()
      // Current shop info for backward compatibility
    val shopInfo: StateFlow<ShopInfo> = MutableStateFlow(
        ShopInfo(
            id = 1,
            name = "",
            phone = "",
            address = "",
            businessHours = "",
            isFavorite = false
        )
    )

    init {
    // Initialize with shop info from database
        coroutineScope.launch {
            // Collect shop info from database
            shopInfoRepository.getAllShops().collect { shops ->
                _shopInfoList.value = shops
                
                // If there are any shops, update current shop ID if needed
                if (shops.isNotEmpty() && (_currentShopId.value !in shops.map { it.id })) {
                    _currentShopId.value = shops.first().id
                }
            }
        }
    }

    // Add item to cart
    fun addToCart(foodItem: FoodItem, quantity: Int, size: CartItem.Size) {
        _cartItems.update { currentItems ->
            val existingItemIndex = currentItems.indexOfFirst { 
                it.foodItem.id == foodItem.id && it.selectedSize == size 
            }
            
            if (existingItemIndex != -1) {
                // Update existing item
                currentItems.toMutableList().apply {
                    val existingItem = get(existingItemIndex)
                    set(existingItemIndex, existingItem.copy(quantity = existingItem.quantity + quantity))
                }
            } else {
                // Add new item
                currentItems + CartItem(foodItem, quantity, size)
            }
        }
    }

    // Remove item from cart
    fun removeFromCart(cartItem: CartItem) {
        _cartItems.update { currentItems ->
            currentItems.filter { it != cartItem }
        }
    }

    // Update item quantity in cart
    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
            return
        }

        _cartItems.update { currentItems ->
            currentItems.map {
                if (it == cartItem) it.copy(quantity = newQuantity) else it
            }
        }
    }    // Update shop information
    fun updateShopInfo(shopInfo: ShopInfo) {
        coroutineScope.launch {
            try {
                // Update in database
                val success = shopInfoRepository.updateShop(shopInfo)
                if (success) {
                    // Refresh shop list to update UI
                    val updatedShops = shopInfoRepository.getAllShops().first()
                    _shopInfoList.value = updatedShops
                    
                    // Log success
                    android.util.Log.d("FoodRepository", "Shop updated successfully: ${shopInfo.name}")
                } else {
                    // Log or handle unsuccessful update
                    android.util.Log.e("FoodRepository", "Failed to update shop with ID: ${shopInfo.id}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("FoodRepository", "Error updating shop: ${e.message}")
            }
        }
    }
      // Add new shop
    fun addShop(shopInfo: ShopInfo) {
        coroutineScope.launch {
            try {
                // Insert into database
                val id = shopInfoRepository.insertShop(shopInfo)
                if (id > 0) {
                    // Refresh shop list to update UI
                    val updatedShops = shopInfoRepository.getAllShops().first()
                    _shopInfoList.value = updatedShops
                    
                    // Log success
                    android.util.Log.d("FoodRepository", "New shop added successfully: ${shopInfo.name}, ID: $id")
                } else {
                    android.util.Log.e("FoodRepository", "Failed to add shop: ${shopInfo.name}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("FoodRepository", "Error adding shop: ${e.message}")
            }
        }
    }    // Delete shop
    fun deleteShop(shopId: Int) {
        coroutineScope.launch {
            try {
                // Delete from database
                val success = shopInfoRepository.deleteShopById(shopId)
                
                if (success) {
                    // Refresh shop list to update UI
                    val updatedShops = shopInfoRepository.getAllShops().first()
                    _shopInfoList.value = updatedShops
                    
                    // If the deleted shop was the current one, select another one if available
                    if (shopId == _currentShopId.value && _shopInfoList.value.isNotEmpty()) {
                        _currentShopId.value = _shopInfoList.value.first().id
                    }
                    
                    android.util.Log.d("FoodRepository", "Shop deleted successfully: ID=$shopId")
                } else {
                    // Log or handle unsuccessful deletion
                    android.util.Log.e("FoodRepository", "Failed to delete shop with ID: $shopId")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("FoodRepository", "Error deleting shop: ${e.message}")
            }
        }
    }
      // Set current shop
    fun setCurrentShop(shopId: Int) {
        android.util.Log.d("FoodRepository", "Setting current shop ID: $shopId")
        android.util.Log.d("FoodRepository", "Available shop IDs: ${_shopInfoList.value.map { it.id }}")
        
        if (_shopInfoList.value.any { it.id == shopId }) {
            _currentShopId.value = shopId
            android.util.Log.d("FoodRepository", "Current shop ID set to: $shopId")
        } else {
            android.util.Log.w("FoodRepository", "Shop ID $shopId not found in available shops")
            // If shop not found, but we have other shops, set to the first one
            if (_shopInfoList.value.isNotEmpty()) {
                _currentShopId.value = _shopInfoList.value.first().id
                android.util.Log.d("FoodRepository", "Fallback: Set current shop ID to: ${_currentShopId.value}")
            }
        }
    }    // Get current shop info
    fun getCurrentShopInfo(): ShopInfo {
        // Log for debugging
        android.util.Log.d("FoodRepository", "Getting current shop info, current ID: ${_currentShopId.value}")
        android.util.Log.d("FoodRepository", "Available shops: ${_shopInfoList.value.size}")
        
        val shop = _shopInfoList.value.find { it.id == _currentShopId.value }
        
        if (shop != null) {
            android.util.Log.d("FoodRepository", "Found current shop: ${shop.name}, ID: ${shop.id}")
            return shop
        } else if (_shopInfoList.value.isNotEmpty()) {
            val firstShop = _shopInfoList.value.first()
            android.util.Log.d("FoodRepository", "Current shop not found, using first available: ${firstShop.name}, ID: ${firstShop.id}")
            _currentShopId.value = firstShop.id // Update current ID to match
            return firstShop
        } else {
            android.util.Log.d("FoodRepository", "No shops available, returning empty shop")
            return ShopInfo.create(
                id = 0,
                name = "",
                phone = "", 
                address = "",
                businessHours = "",
                isFavorite = false
            )
        }
    }// Calculate total price of all items in cart
    fun getCartTotal(): Double {
        return cartItems.value.sumOf { it.getTotalPrice() }
    }
    
    // Toggle favorite status of a shop
    fun toggleFavoriteShop(shopId: Int) {
        coroutineScope.launch {
            try {
                shopInfoRepository.toggleFavorite(shopId)
                // Update will be reflected in shopInfoList flow automatically
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("FoodRepository", "Error toggling favorite: ${e.message}")
            }
        }
    }

    // Get menu items by category
    fun getMenuItemsByCategory(category: FoodItem.FoodCategory): List<FoodItem> {
        return menuItems.value.filter { it.category == category }
    }    // Generate new unique shop ID
    suspend fun generateShopId(): Int {
        return shopInfoRepository.generateShopId()
    }    // Update shop rating
    fun updateShopRating(shopId: Int, rating: Float) {
        coroutineScope.launch {
            try {
                // Update in database
                shopInfoRepository.updateRating(shopId, rating)
                
                // Refresh shop list to update UI
                val updatedShops = shopInfoRepository.getAllShops().first()
                _shopInfoList.value = updatedShops
                
                android.util.Log.d("FoodRepository", "Shop rating updated successfully: ID=$shopId, Rating=$rating")
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("FoodRepository", "Error updating shop rating: ${e.message}")
            }
        }
    }    // Singleton pattern
    companion object {
        @Volatile
        private var instance: FoodRepository? = null

        fun getInstance(context: Context): FoodRepository {
            return instance ?: synchronized(this) {
                instance ?: FoodRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}