package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.databinding.FragmentShopInfoBinding
import com.example.myapplication.viewmodel.ShopInfoViewModel
import kotlinx.coroutines.launch

class ShopInfoFragment : Fragment() {

    private var _binding: FragmentShopInfoBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopInfoViewModel by viewModels()
    
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
        
        setupListeners()
        observeViewModel()
    }
    
    private fun setupListeners() {
        // Allow calling the shop by tapping on the phone number
        binding.callText.setOnClickListener {
            val phoneNumber = binding.phoneText.text.toString()
            if (phoneNumber.isNotEmpty()) {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:$phoneNumber")
                startActivity(dialIntent)
            }
        }
        
        // Dialog to edit shop information
        binding.editButton.setOnClickListener {
            val dialog = ShopInfoEditDialog(viewModel)
            dialog.show(parentFragmentManager, "ShopInfoEditDialog")
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shopInfo.collect { shopInfo ->
                    binding.apply {
                        shopNameText.text = shopInfo.name
                        phoneText.text = shopInfo.phone
                        addressText.text = shopInfo.address
                        businessHoursText.text = shopInfo.businessHours
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}