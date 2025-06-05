package com.example.myapplication.util

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.material.snackbar.Snackbar

/**
 * Utility class for creating styled snackbars with consistent design
 */
object SnackbarUtils {
    
    /**
     * Create a styled snackbar with appropriate colors and design
     * @param view The view to anchor the snackbar to
     * @param message The message to display
     * @param isError Whether this is an error message
     * @param context The context for accessing resources
     * @param duration The duration to show the snackbar (default: LENGTH_SHORT)
     * @return Configured Snackbar instance
     */
    fun createStyledSnackbar(
        view: View,
        message: String,
        isError: Boolean = false,
        context: Context,
        duration: Int = Snackbar.LENGTH_SHORT
    ): Snackbar {
        val snackbar = Snackbar.make(view, message, duration)
        
        // Set background color based on message type
        val backgroundColor = if (isError) {
            ContextCompat.getColor(context, R.color.snackbar_error)
        } else {
            ContextCompat.getColor(context, R.color.snackbar_success)
        }
        
        snackbar.setBackgroundTint(backgroundColor)
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text))
        
        // Style the text
        val textView = snackbar.view.findViewById<android.widget.TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        textView?.apply {
            textSize = 14f
            setTypeface(typeface, Typeface.NORMAL)
            maxLines = 2
        }
        
        return snackbar
    }
    
    /**
     * Create and show a success snackbar
     */
    fun showSuccess(view: View, message: String, context: Context) {
        createStyledSnackbar(view, message, false, context).show()
    }
    
    /**
     * Create and show an error snackbar
     */
    fun showError(view: View, message: String, context: Context) {
        createStyledSnackbar(view, message, true, context, Snackbar.LENGTH_LONG).show()
    }
    
    /**
     * Create and show an info snackbar with custom color
     */
    fun showInfo(view: View, message: String, context: Context) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_info))
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text))
        snackbar.show()
    }
      /**
     * Create and show a warning snackbar
     */
    fun showWarning(view: View, message: String, context: Context) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_warning))
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text))
        snackbar.show()
    }
    
    /**
     * Create a snackbar with an action button
     * @param view The view to anchor the snackbar to
     * @param message The message to display
     * @param actionText The text for the action button
     * @param actionListener The listener for action button clicks
     * @param isError Whether this is an error message
     * @param context The context for accessing resources
     */
    fun showWithAction(
        view: View, 
        message: String, 
        actionText: String, 
        actionListener: View.OnClickListener,
        isError: Boolean = false,
        context: Context
    ) {
        val snackbar = createStyledSnackbar(view, message, isError, context, Snackbar.LENGTH_LONG)
        snackbar.setAction(actionText, actionListener)
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
        snackbar.show()
    }
    
    /**
     * Create a snackbar for loading states
     */
    fun showLoading(view: View, message: String, context: Context): Snackbar {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_info))
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.snackbar_text))
        return snackbar
    }
}
