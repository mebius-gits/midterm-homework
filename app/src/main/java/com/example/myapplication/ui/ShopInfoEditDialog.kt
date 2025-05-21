package com.example.myapplication.ui

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.myapplication.databinding.DialogEditShopInfoBinding
import com.example.myapplication.viewmodel.ShopInfoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

/**
 * Dialog for editing shop information
 */
class ShopInfoEditDialog(
    private val viewModel: ShopInfoViewModel,
    private val isNewShop: Boolean = false
) : DialogFragment() {

    private var _binding: DialogEditShopInfoBinding? = null
    private val binding get() = _binding!!
    private var timer: Timer? = null

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

    override fun onStart() {
        super.onStart()

        // 設定對話框更大的尺寸
        dialog?.window?.apply {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val width = (displayMetrics.widthPixels * 0.9).toInt() // 螢幕寬度的90%
            val height = WindowManager.LayoutParams.WRAP_CONTENT

            setLayout(width, height)
        }

        // Start updating the current time
        startTimeUpdates()
    }

    private fun startTimeUpdates() {
        updateCurrentTime()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    updateCurrentTime()
                }
            }
        }, 0, 1000)
    }

    private fun updateCurrentTime() {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set dialog title
        binding.titleText.text = if (isNewShop) "新增商家" else "編輯商家資訊"
          if (!isNewShop) {
            // Show loading state
            binding.apply {
                submitButton.isEnabled = false
                submitButton.text = "讀取資料中..."
            }
            
            // Use viewLifecycleOwner to observe the StateFlow for current shop info
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.currentShopInfo.collect { shopInfo ->
                        // Log for debugging
                        android.util.Log.d("ShopInfoEditDialog", "Received shop info: ${shopInfo.name}, ID: ${shopInfo.id}")
                        
                        // Populate fields with current shop info
                        binding.apply {
                            nameEditText.setText(shopInfo.name)
                            phoneEditText.setText(shopInfo.phone)
                            addressEditText.setText(shopInfo.address)
                            businessHoursEditText.setText(shopInfo.businessHours)
                            
                            // Re-enable submit button
                            submitButton.isEnabled = true
                            submitButton.text = "儲存"
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ShopInfoEditDialog", "Error collecting shop info", e)
                    Toast.makeText(context, "讀取商家資訊失敗", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
        
        binding.apply {

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
                    Toast.makeText(context, "請填寫所有必填欄位", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Add or update shop info
                if (isNewShop) {
                    // Show loading state
                    binding.submitButton.isEnabled = false
                    binding.submitButton.text = "處理中..."
                    try {
                        viewModel.addNewShop(
                            name = name,
                            phone = phone,
                            address = address,
                            businessHours = businessHours
                        )
                        
                        // Add a delay to ensure the add completes before showing message
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            Toast.makeText(context, "商家「$name」已新增", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }, 300)
                    } catch (e: Exception) {
                        android.util.Log.e("ShopInfoEditDialog", "Error adding shop", e)
                        Toast.makeText(context, "新增商家時出錯，請再試一次", Toast.LENGTH_SHORT).show()
                        
                        // Re-enable the submit button
                        binding.submitButton.isEnabled = true
                        binding.submitButton.text = "儲存"
                    }
                } else {
                    // Show loading state
                    binding.submitButton.isEnabled = false
                    binding.submitButton.text = "處理中..."
                      try {
                        viewModel.updateShopInfo(
                            name = name,
                            phone = phone,
                            address = address,
                            businessHours = businessHours
                        )
                        
                        // Add a delay to ensure the update completes before showing message
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            Toast.makeText(context, "「$name」資訊已更新", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }, 300)
                    } catch (e: Exception) {
                        android.util.Log.e("ShopInfoEditDialog", "Error updating shop", e)
                        Toast.makeText(context, "更新商家資訊時出錯，請再試一次", Toast.LENGTH_SHORT).show()
                        
                        // Re-enable the submit button
                        binding.submitButton.isEnabled = true
                        binding.submitButton.text = "儲存"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop timer when dialog is dismissed
        timer?.cancel()
        timer = null
        _binding = null
    }
}