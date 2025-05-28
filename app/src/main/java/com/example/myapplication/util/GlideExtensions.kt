package com.example.myapplication.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R

/**
 * Helper function to load an image with rounded corners using Glide
 * @param imageView The ImageView to load the image into
 * @param imageUrl The URL or URI of the image to load
 * @param cornerRadius Corner radius in pixels
 */
fun loadImageWithRoundedCorners(imageView: ImageView, imageUrl: Any, cornerRadius: Int = 16) {
    // Ensure the ImageView has clipToOutline set
    imageView.clipToOutline = true
    imageView.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
    
    // Set the background to the rounded corners drawable
    imageView.setBackgroundResource(R.drawable.rounded_image_corners)
    
    val requestOptions = RequestOptions()
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .placeholder(R.drawable.ic_image_placeholder)
        .error(R.drawable.ic_image_placeholder)

    Glide.with(imageView.context)
        .load(imageUrl)
        .apply(requestOptions)
        .into(imageView)
}
