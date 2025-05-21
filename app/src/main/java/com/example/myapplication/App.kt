package com.example.myapplication

import android.content.Context

/**
 * Application context provider for static access
 */
object App {
    private lateinit var context: Context
    
    /**
     * Initialize with application context
     */
    fun initialize(context: Context) {
        App.context = context.applicationContext
    }
    
    /**
     * Get application context
     */
    fun applicationContext(): Context {
        return context
    }
}
