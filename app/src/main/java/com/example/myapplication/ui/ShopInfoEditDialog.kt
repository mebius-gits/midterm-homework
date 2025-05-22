package com.example.myapplication.ui

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogEditShopInfoBinding
import com.example.myapplication.viewmodel.ShopInfoViewModel
import com.vicmikhailau.maskededittext.MaskedEditText
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private var shopInfoJob: Job? = null

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
        super.onStart()        // 設定對話框更大的尺寸
        dialog?.window?.apply {
            val displayMetrics = DisplayMetrics()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // For Android 11+
                val display = context?.display
                if (display != null) {
                    @Suppress("DEPRECATION")
                    display.getRealMetrics(displayMetrics)
                }
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getMetrics(displayMetrics)
            }

            val width = (displayMetrics.widthPixels * 0.9).toInt() // 螢幕寬度的90%
            val height = WindowManager.LayoutParams.WRAP_CONTENT

            setLayout(width, height)
        }

        // Start updating the current time
        startTimeUpdates()
    }    private fun startTimeUpdates() {
        // Timer for updating time if needed
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    // Update time display if needed
                }
            }
        }, 0, 1000)
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set dialog title
        binding.titleText.text = if (isNewShop) "新增商家" else "編輯商家資訊"
          
        // Add text watchers for form fields
        setupTextWatchers()
          
        if (!isNewShop) {
            // Show loading state
            binding.apply {
                submitButton.isEnabled = false
                submitButton.text = "讀取資料中..."
            }
              // Cancel any previous job
            shopInfoJob?.cancel()
            
            // Log the debug info
            android.util.Log.d("ShopInfoEditDialog", "Starting to collect shop info...")
            
            // Use a flow-based approach with proper lifecycle handling
            shopInfoJob = viewModel.currentShopInfo
                .onEach { shopInfo ->
                    // Enhanced debugging
                    android.util.Log.d("ShopInfoEditDialog", "Received shop info: ${shopInfo.name}, ID: ${shopInfo.id}, Phone: ${shopInfo.phone}, Address: ${shopInfo.address}, Hours: ${shopInfo.businessHours}, Favorite: ${shopInfo.isFavorite}")
                    
                    // Check if we have valid shop info
                    if (shopInfo.id == 0 && shopInfo.name.isEmpty()) {
                        // This is an empty shop, possibly due to database error
                        android.util.Log.e("ShopInfoEditDialog", "Received empty shop data")
                        throw Exception("無法取得商家資料")
                    }
                    
                    // Ensure the fragment is still attached before updating UI
                    if (isAdded && _binding != null) {
                        // Populate fields with current shop info
                        binding.apply {
                            nameEditText.setText(shopInfo.name)
                            
                            // Try to use MaskedEditText for phone if available
                            try {
                                val maskedPhoneEdit = phoneEditText as? MaskedEditText
                                if (maskedPhoneEdit != null) {
                                    // Format the phone number to fit the mask
                                    maskedPhoneEdit.setText(shopInfo.phone)
                                } else {
                                    // Fallback for regular EditText
                                    phoneEditText.setText(shopInfo.phone)
                                }
                            } catch (e: Exception) {
                                // In case of errors, just use standard setText
                                phoneEditText.setText(shopInfo.phone)
                            }
                            
                            // Set address and business hours
                            addressEditText.setText(shopInfo.address)
                            businessHoursEditText.setText(shopInfo.businessHours)
                            
                            // Re-enable submit button
                            submitButton.isEnabled = true
                            submitButton.text = "儲存"
                        }
                    }
                }                .catch { e ->
                    // Only show error if the fragment is still attached
                    if (isAdded) {
                        android.util.Log.e("ShopInfoEditDialog", "Error collecting shop info", e)
                        if (e !is kotlinx.coroutines.CancellationException) {
                            Toast.makeText(context, "讀取商家資料錯誤", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
          binding.apply {

            cancelButton.setOnClickListener {
                dismiss()
            }
            
            submitButton.setOnClickListener {
                handleSubmit()
            }
        }
    }
    
    /**
     * Normalize phone number for storage
     * Removes unnecessary characters and formats it consistently
     */
    private fun normalizePhoneNumber(phone: String): String {
        // Remove all non-digit characters except +
        return phone.replace(Regex("[^0-9+]"), "")
    }
    
    /**
     * Setup text watchers for real-time validation
     */
    private fun setupTextWatchers() {
        binding.apply {
            // For phone number validation
            phoneEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Clear error when user starts typing
                    if (!s.isNullOrEmpty()) {
                        phoneLayout.error = null
                    }
                }
            })
            
            // For business hours validation
            businessHoursEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Clear error when user starts typing
                    if (!s.isNullOrEmpty()) {
                        businessHoursLayout.error = null
                    }
                }
            })
        }
    }
    
    /**
     * Handle form submission
     */
    private fun handleSubmit() {
        val name = binding.nameEditText.text.toString().trim()
        
        // Get phone number with special handling for MaskedEditText
        val phoneText = try {
            val maskedEdit = binding.phoneEditText as? MaskedEditText
            maskedEdit?.unMaskedText?.trim() ?: binding.phoneEditText.text.toString().trim()
        } catch (e: Exception) {
            binding.phoneEditText.text.toString().trim()
        }
        
        // Normalize the phone number (remove formatting characters)
        val phone = normalizePhoneNumber(phoneText)
        
        val address = binding.addressEditText.text.toString().trim()
        val businessHours = binding.businessHoursEditText.text.toString().trim()
        
        // Reset any previous error states
        binding.apply {
            nameLayout.error = null
            phoneLayout.error = null
            addressLayout.error = null
            businessHoursLayout.error = null
        }
        
        // Validate inputs
        var hasError = false
        
        if (name.isEmpty()) {
            binding.nameLayout.error = context?.getString(R.string.validation_required_fields)
            hasError = true
        }
        
        if (phone.isEmpty()) {
            binding.phoneLayout.error = context?.getString(R.string.validation_required_fields)
            hasError = true
        } else if (phone.length < 10) {
            binding.phoneLayout.error = context?.getString(R.string.validation_phone)
            hasError = true
        }
        
        if (address.isEmpty()) {
            binding.addressLayout.error = context?.getString(R.string.validation_required_fields)
            hasError = true
        }
        
        if (businessHours.isEmpty()) {
            binding.businessHoursLayout.error = context?.getString(R.string.validation_required_fields)
            hasError = true
        } else if (!businessHours.matches(Regex("\\d{2}:\\d{2} - \\d{2}:\\d{2}"))) {
            binding.businessHoursLayout.error = context?.getString(R.string.validation_business_hours)
            hasError = true
        }
        
        if (hasError) {
            Toast.makeText(context, context?.getString(R.string.validation_required_fields), Toast.LENGTH_SHORT).show()
            return
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
                Log.e("ShopInfoEditDialog", "Error adding shop", e)
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
                Log.e("ShopInfoEditDialog", "Error updating shop", e)
                Toast.makeText(context, "更新商家資訊時出錯，請再試一次", Toast.LENGTH_SHORT).show()
                
                // Re-enable the submit button
                binding.submitButton.isEnabled = true
                binding.submitButton.text = "儲存"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop timer when dialog is dismissed
        timer?.cancel()
        timer = null
        shopInfoJob?.cancel() // Cancel the coroutine job if it's still active
        shopInfoJob = null
        _binding = null
    }
}