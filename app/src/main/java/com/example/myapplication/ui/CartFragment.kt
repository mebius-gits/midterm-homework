package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.CartItemAdapter
import com.example.myapplication.databinding.FragmentCartBinding
import com.example.myapplication.util.SnackbarUtils
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.CartViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CartFragment : Fragment() {    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CartViewModel
    private lateinit var adapter: CartItemAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel with factory for AndroidViewModel
        viewModel = ViewModelProvider(this, CartViewModelFactory(requireActivity().application))
            .get(CartViewModel::class.java)
            
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = CartItemAdapter(
            onIncreaseClick = { cartItem -> viewModel.increaseQuantity(cartItem) },
            onDecreaseClick = { cartItem -> viewModel.decreaseQuantity(cartItem) },
            onRemoveClick = { cartItem -> viewModel.removeItem(cartItem) }
        )
        
        binding.cartRecyclerView.apply {
            adapter = this@CartFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }
      private fun setupListeners() {
        binding.checkoutButton.setOnClickListener {
            if (viewModel.isCartEmpty()) {                SnackbarUtils.showWarning(binding.root, "🛒 購物車空空如也，快去挑選喜歡的商品吧！", requireContext())
                return@setOnClickListener
            }
            
            // In a real app, this would navigate to a checkout screen            SnackbarUtils.showSuccess(binding.root, "🎉 結帳成功！感謝您的購買", requireContext())
            viewModel.clearCart()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.cartItems.collect { items ->
                        adapter.submitList(items)
                        
                        // Show empty view if cart is empty
                        binding.emptyCartText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                        binding.cartHeaderCard.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                        binding.totalCard.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                
                launch {
                    viewModel.totalPrice.collect { total ->
                        binding.totalAmountText.text = "NT$${total.toInt()}"
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