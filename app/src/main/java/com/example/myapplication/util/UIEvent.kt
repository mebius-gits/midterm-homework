package com.example.myapplication.util

/**
 * Sealed class for UI events that need to be handled by the UI layer
 */
sealed class UIEvent {
    data class ShowSnackbar(
        val message: String,
        val isError: Boolean = false
    ) : UIEvent()
    
    data class ShowToast(
        val message: String,
        val isError: Boolean = false
    ) : UIEvent()
}
