package com.example.myapplication.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.ShopListAdapter
import com.example.myapplication.databinding.FragmentShopInfoBinding
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.viewmodel.ShopInfoViewModel
import com.example.myapplication.viewmodel.ShopInfoViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShopInfoFragment : Fragment() {    private var _binding: FragmentShopInfoBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ShopInfoViewModel
    private lateinit var shopListAdapter: ShopListAdapter
    
    // Image selection launcher
    private lateinit var imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private var selectedShopForImage: ShopInfo? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Check if we have a selected shop to update
                selectedShopForImage?.let { shop ->
                    // Save the image URI persistently
                    val persistedUri = persistUriPermission(uri)
                    
                    // Update the shop with the persisted URI
                    viewModel.updateShopImage(shop.id, persistedUri.toString())
                    
                    // Clear the selected shop after updating
                    selectedShopForImage = null
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel with factory for AndroidViewModel
        viewModel = ViewModelProvider(this, ShopInfoViewModelFactory(requireActivity().application))
            .get(ShopInfoViewModel::class.java)
        
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }    private fun setupRecyclerView() {        shopListAdapter = ShopListAdapter(
            onShopClick = { /* Not needed for list-only view */ },
            onEditClick = { shop ->
                showEditDialog(shop)
            },
            onDeleteClick = { shop ->
                confirmDeleteShop(shop)
            },
            onCallClick = { phoneNumber ->
                makePhoneCall(phoneNumber)
            },
            onFavoriteClick = { shop ->
                viewModel.toggleFavoriteShop(shop.id)
            },
            onImageClick = { shop ->
                handleImageSelection(shop)
            },
            onRatingChanged = { shop, rating ->
                viewModel.updateShopRating(shop.id, rating)
            }
        )
        
        binding.shopsRecyclerView.apply {
            adapter = shopListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun makePhoneCall(phoneNumber: String) {
        val cleanPhoneNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        if (cleanPhoneNumber.isNotEmpty()) {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$cleanPhoneNumber")
            startActivity(dialIntent)
        } else {
            Toast.makeText(context, "無效的電話號碼", Toast.LENGTH_SHORT).show()
        }
    }
      private fun setupListeners() {
        // Add new shop button
        binding.addShopButton.setOnClickListener {
            showAddShopDialog()
        }
        
        // Long press on title to show debug info
        binding.titleText.setOnLongClickListener {
            viewModel.debugShowAllShops(requireContext())
            true
        }
    }
      private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.shopInfoList.collectLatest { shops ->
                        updateShopList(shops)
                        android.util.Log.d("ShopInfoFragment", "Shop list updated. Count: ${shops.size}")
                    }
                }
                  // Also observe current shop info to log changes
                launch {
                    viewModel.currentShopId.collect { shopId ->
                        android.util.Log.d("ShopInfoFragment", "Current shop ID changed to: $shopId")
                    }
                }
            }
        }
    }
      private fun updateShopList(shops: List<ShopInfo>) {
        // First check for obvious issues
        if (shops.size > 0) {
            android.util.Log.d("ShopInfoFragment", "First shop: ${shops[0].name}, ID: ${shops[0].id}")
        }
        
        // Submit list with current selected shop (if any)
        val currentId = viewModel.currentShopId.value
        shopListAdapter.submitList(shops, currentId)
        
        // Show empty message if there are no shops
        binding.emptyListMessage.isVisible = shops.isEmpty()
    }
    
    private fun showAddShopDialog() {
        val dialog = ShopInfoEditDialog(viewModel, isNewShop = true)
        dialog.show(parentFragmentManager, "AddShopDialog")
    }    private fun showEditDialog(shop: ShopInfo) {
        // Show a loading toast
        val loadingToast = Toast.makeText(context, "準備編輯: ${shop.name}", Toast.LENGTH_SHORT)
        loadingToast.show()
        
        // Log for debugging
        android.util.Log.d("ShopInfoFragment", "Preparing to edit shop: ${shop.name}, ID: ${shop.id}")
        
        // First set current shop for the edit dialog and ensure it's loaded
        viewModel.setCurrentShop(shop.id)
        
        // Use a small delay to ensure the data is set before opening the dialog
        view?.postDelayed({
            loadingToast.cancel()
            
            // Verify that the shop was set correctly before opening the dialog
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val currentShopId = viewModel.currentShopId.value
                    android.util.Log.d("ShopInfoFragment", "Current shop ID before opening dialog: $currentShopId (Expected: ${shop.id})")
                    
                    if (currentShopId == shop.id) {
                        val dialog = ShopInfoEditDialog(viewModel, isNewShop = false)
                        dialog.show(parentFragmentManager, "EditShopDialog")
                    } else {
                        Toast.makeText(context, "無法編輯，商家 ID 不符", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ShopInfoFragment", "Error before opening dialog", e)
                    Toast.makeText(context, "開啟編輯對話框時發生錯誤", Toast.LENGTH_SHORT).show()
                }
            }
        }, 500) // Increased delay to ensure data is ready
    }    private fun confirmDeleteShop(shop: ShopInfo) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("確認刪除")
            .setMessage("確定要刪除「${shop.name}」嗎？此操作無法恢復。")
            .setPositiveButton("刪除") { _, _ ->
                // Show a loading indicator
                val loadingToast = Toast.makeText(context, "刪除中...", Toast.LENGTH_SHORT)
                loadingToast.show()
                
                viewModel.deleteShop(shop.id)
                
                loadingToast.cancel()
                Toast.makeText(context, "商家「${shop.name}」已刪除", Toast.LENGTH_SHORT).show()
                
                // Refresh the shop list by triggering a reobservation
                observeViewModel()
            }
            .setNegativeButton("取消", null)
            .create()
        dialog.show()
    }
    
    /**
     * Handles shop image selection
     * @param shop The shop to update with the selected image
     */
    private fun handleImageSelection(shop: ShopInfo) {
        // Store the selected shop for later use
        selectedShopForImage = shop
        
        // Show image source selection dialog
        val options = arrayOf("從相簿選擇")
        
        AlertDialog.Builder(requireContext())
            .setTitle("選擇店家圖片")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Check and request permissions if needed
                        if (checkImagePermission()) {
                            launchImagePicker()
                        } else {
                            requestImagePermission()
                        }
                    }
                }
            }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Persists URI permission so it can be accessed later
     * @param uri The URI to persist
     * @return The persisted URI
     */
    private fun persistUriPermission(uri: Uri): Uri {
        val contentResolver = requireContext().contentResolver
        
        // Take persistable permission to use this URI across app restarts
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
        
        return uri
    }
    
    /**
     * Checks if the necessary permissions for image selection are granted
     * @return True if permissions are granted, false otherwise
     */
    private fun checkImagePermission(): Boolean {
        val context = requireContext()
        val readImagesPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context, readImagesPermission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Requests necessary permissions for image selection
     */
    private fun requestImagePermission() {
        val readImagesPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        requestPermissions(arrayOf(readImagesPermission), IMAGE_PERMISSION_REQUEST_CODE)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            IMAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, launch image picker
                    launchImagePicker()
                } else {
                    // Permission denied
                    Toast.makeText(context, "需要權限才能選擇圖片", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    
    private fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }
    
    companion object {
        private const val IMAGE_PERMISSION_REQUEST_CODE = 100
    }
}