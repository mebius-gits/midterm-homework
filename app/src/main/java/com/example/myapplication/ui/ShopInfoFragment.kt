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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.ShopListAdapter
import com.example.myapplication.databinding.FragmentShopInfoBinding
import com.example.myapplication.model.ShopInfo
import com.example.myapplication.viewmodel.ShopInfoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShopInfoFragment : Fragment() {

    private var _binding: FragmentShopInfoBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopInfoViewModel by viewModels()
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
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.shopInfoList.collectLatest { shops ->
                        updateShopList(shops)
                    }
                }
            }
        }
    }
    
    private fun updateShopList(shops: List<ShopInfo>) {
        shopListAdapter.submitList(shops, null) // No selected shop in list-only view
        
        // Show empty message if there are no shops
        binding.emptyListMessage.isVisible = shops.isEmpty()
    }
    
    private fun showAddShopDialog() {
        val dialog = ShopInfoEditDialog(viewModel, isNewShop = true)
        dialog.show(parentFragmentManager, "AddShopDialog")
    }
    
    private fun showEditDialog(shop: ShopInfo) {
        // First set current shop for the edit dialog
        viewModel.setCurrentShop(shop.id)
        val dialog = ShopInfoEditDialog(viewModel, isNewShop = false)
        dialog.show(parentFragmentManager, "EditShopDialog")
    }
    
    private fun confirmDeleteShop(shop: ShopInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("確認刪除")
            .setMessage("確定要刪除「${shop.name}」嗎？此操作無法恢復。")
            .setPositiveButton("刪除") { _, _ ->
                viewModel.deleteShop(shop.id)
                Toast.makeText(context, "商家已刪除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}