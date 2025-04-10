package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.adapter.FoodItemAdapter
import com.example.myapplication.databinding.FragmentCategoryBinding
import com.example.myapplication.model.FoodItem
import com.example.myapplication.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MenuViewModel by activityViewModels()
    private val category: FoodItem.FoodCategory by lazy {
        arguments?.getSerializable(ARG_CATEGORY) as FoodItem.FoodCategory
    }
    
    private lateinit var adapter: FoodItemAdapter
    private lateinit var parentMenuFragment: MenuFragment
    
    companion object {
        private const val ARG_CATEGORY = "category"
        
        fun newInstance(category: FoodItem.FoodCategory, parent: MenuFragment): CategoryFragment {
            return CategoryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                }
                parentMenuFragment = parent
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeMenuItems()
    }
    
    private fun setupRecyclerView() {
        adapter = FoodItemAdapter { foodItem ->
            parentMenuFragment.navigateToFoodDetail(foodItem)
        }
        
        binding.recyclerView.apply {
            adapter = this@CategoryFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 1)
        }
    }
    
    private fun observeMenuItems() {
        val itemsFlow: Flow<List<FoodItem>> = when (category) {
            FoodItem.FoodCategory.MAIN_DISH -> viewModel.mainDishes
            FoodItem.FoodCategory.SIDE_DISH -> viewModel.sideDishes
            FoodItem.FoodCategory.DRINK -> viewModel.drinks
            FoodItem.FoodCategory.OTHER -> viewModel.others
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                itemsFlow.collect { items ->
                    adapter.submitList(items)
                    binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}