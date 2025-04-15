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
import com.example.myapplication.databinding.DialogEditShopInfoBinding
import com.example.myapplication.viewmodel.ShopInfoViewModel
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set dialog title
        binding.titleText.text = if (isNewShop) "新增商家" else "編輯商家資訊"
        
        // Populate fields with current shop info if editing
        val currentShopInfo = if (isNewShop) {
            null
        } else {
            viewModel.currentShopInfo.value
        }
        
        binding.apply {
            // Populate fields if editing existing shop
            if (currentShopInfo != null) {
                nameEditText.setText(currentShopInfo.name)
                phoneEditText.setText(currentShopInfo.phone)
                addressEditText.setText(currentShopInfo.address)
                businessHoursEditText.setText(currentShopInfo.businessHours)
            }

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
                    viewModel.addNewShop(
                        name = name,
                        phone = phone,
                        address = address,
                        businessHours = businessHours
                    )
                    Toast.makeText(context, "商家已新增", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateShopInfo(
                        name = name,
                        phone = phone,
                        address = address,
                        businessHours = businessHours
                    )
                    Toast.makeText(context, "資訊已更新", Toast.LENGTH_SHORT).show()
                }
                
                dismiss()
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