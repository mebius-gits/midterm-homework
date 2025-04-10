package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.adapter.CategoryFragmentAdapter
import com.example.myapplication.databinding.FragmentMenuBinding
import com.example.myapplication.model.FoodItem
import com.example.myapplication.viewmodel.MenuViewModel
import com.google.android.material.tabs.TabLayoutMediator

class MenuFragment : Fragment() {
    
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MenuViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up the view pager with tabs for different food categories
        val adapter = CategoryFragmentAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Connect the tab layout with the view pager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.main_dishes)
                1 -> getString(R.string.side_dishes)
                2 -> getString(R.string.drinks)
                3 -> getString(R.string.others)
                else -> null
            }
        }.attach()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    fun navigateToFoodDetail(foodItem: FoodItem) {
        viewModel.selectFoodItem(foodItem)
        val action = MenuFragmentDirections.actionMenuFragmentToItemDetailFragment(foodItem.id)
        findNavController().navigate(action)
    }
}