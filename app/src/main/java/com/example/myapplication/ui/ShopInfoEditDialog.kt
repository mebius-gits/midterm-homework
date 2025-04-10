package com.example.myapplication.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DialogEditShopInfoBinding
import com.example.myapplication.repository.FoodRepository
import com.example.myapplication.viewmodel.ShopInfoViewModel

/**
 * Dialog for editing shop information
 */
class ShopInfoEditDialog(private val viewModel: ShopInfoViewModel) : DialogFragment() {

    private var _binding: DialogEditShopInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditShopInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate fields with current shop info
        val currentShopInfo = FoodRepository.getInstance().shopInfo.value
        binding.apply {
            nameEditText.setText(currentShopInfo.name)
            phoneEditText.setText(currentShopInfo.phone)
            addressEditText.setText(currentShopInfo.address)
            businessHoursEditText.setText(currentShopInfo.businessHours)

            cancelButton.setOnClickListener {
                dismiss()
            }

            submitButton.setOnClickListener {
                val name = nameEditText.text.toString().trim()
                val phone = phoneEditText.text.toString().trim()
                val address = addressEditText.text.toString().trim()
                val businessHours = businessHoursEditText.text.toString().trim()

                // Validate inputs
                if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || businessHours.isEmpty()) {
                    Toast.makeText(context, "請填寫所有欄位", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update shop info
                viewModel.updateShopInfo(name, phone, address, businessHours)
                Toast.makeText(context, "資訊已更新", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}