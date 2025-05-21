package com.example.myapplication.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    }
      private fun setupRecyclerView() {
        shopListAdapter = ShopListAdapter(
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
                    viewModel.currentShopInfo.collect { shop ->
                        android.util.Log.d("ShopInfoFragment", "Current shop changed: ${shop.name}, ID: ${shop.id}")
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
    }
      private fun showEditDialog(shop: ShopInfo) {
        // Show a loading toast
        val loadingToast = Toast.makeText(context, "準備編輯: ${shop.name}", Toast.LENGTH_SHORT)
        loadingToast.show()
        
        // First set current shop for the edit dialog and ensure it's loaded
        viewModel.setCurrentShop(shop.id)
        
        // Use a small delay to ensure the data is set before opening the dialog
        view?.postDelayed({
            loadingToast.cancel()
            val dialog = ShopInfoEditDialog(viewModel, isNewShop = false)
            dialog.show(parentFragmentManager, "EditShopDialog")
        }, 300) // Small delay to ensure data is ready
    }
      private fun confirmDeleteShop(shop: ShopInfo) {
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}