package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.model.FoodItem
import com.example.myapplication.ui.CategoryFragment
import com.example.myapplication.ui.MenuFragment

/**
 * Adapter for handling the different food category tabs in the ViewPager
 */
class CategoryFragmentAdapter(fragment: MenuFragment) : FragmentStateAdapter(fragment) {

    private val parentFragment = fragment
    
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        // Create a new category fragment for each tab
        val category = when (position) {
            0 -> FoodItem.FoodCategory.MAIN_DISH
            1 -> FoodItem.FoodCategory.SIDE_DISH
            2 -> FoodItem.FoodCategory.DRINK
            3 -> FoodItem.FoodCategory.OTHER
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
        
        return CategoryFragment.newInstance(category, parentFragment)
    }
}