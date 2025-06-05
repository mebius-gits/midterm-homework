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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentItemDetailBinding
import com.example.myapplication.model.CartItem
import com.example.myapplication.model.FoodItem
import com.example.myapplication.repository.FoodRepository
import com.example.myapplication.util.SnackbarUtils
import com.example.myapplication.viewmodel.MenuViewModel
import com.example.myapplication.viewmodel.MenuViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ItemDetailFragment : Fragment() {    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MenuViewModel
    private val args: ItemDetailFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel with factory for AndroidViewModel
        viewModel = ViewModelProvider(this, MenuViewModelFactory(requireActivity().application))
            .get(MenuViewModel::class.java)
            
        // Load food item details
        val foodItem = FoodRepository.getInstance(requireContext()).menuItems.value.find { it.id == args.foodItemId }
        foodItem?.let { viewModel.selectFoodItem(it) }
        
        setupView()
        setupListeners()
        observeViewModel()
    }
    
    private fun setupView() {
        binding.sizeGroup.setOnCheckedChangeListener { _, checkedId ->
            val size = when (checkedId) {
                binding.sizeSmall.id -> CartItem.Size.SMALL
                binding.sizeMedium.id -> CartItem.Size.MEDIUM
                binding.sizeLarge.id -> CartItem.Size.LARGE
                else -> CartItem.Size.MEDIUM
            }
            viewModel.updateSize(size)
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.increaseButton.setOnClickListener {
            viewModel.updateQuantity(1)
        }
        
        binding.decreaseButton.setOnClickListener {
            viewModel.updateQuantity(-1)
        }
          binding.addToCartButton.setOnClickListener {
            viewModel.addToCart()
            SnackbarUtils.showSuccess(binding.root, "🛒 商品已成功加入購物車", requireContext())
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedFoodItem.filterNotNull().collect { foodItem ->
                        updateFoodItemDetails(foodItem)
                    }
                }
                
                launch {
                    viewModel.quantity.collect { quantity ->
                        binding.quantityText.text = quantity.toString()
                    }
                }
                
                launch {
                    viewModel.selectedSize.collect { size ->
                        val buttonId = when (size) {
                            CartItem.Size.SMALL -> binding.sizeSmall.id
                            CartItem.Size.MEDIUM -> binding.sizeMedium.id
                            CartItem.Size.LARGE -> binding.sizeLarge.id
                        }
                        binding.sizeGroup.check(buttonId)
                    }
                }
                
                // Observe adjusted price based on size
                launch {
                    viewModel.adjustedPrice.collect { price ->
                        binding.foodPrice.text = "NT$${price.toInt()}"
                    }
                }
            }
        }
    }
    
    private fun updateFoodItemDetails(foodItem: FoodItem) {
        binding.apply {
            foodName.text = foodItem.name
            // Price will be updated through the adjustedPrice StateFlow
            foodDescription.text = foodItem.description
            ratingBar.rating = foodItem.rating
            
            // For simplicity, we're not loading images from a URL in this example
            // In a real app, you'd use Glide or similar library
            Glide.with(requireContext()).load(foodItem.imageUrl).into(foodImage)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}