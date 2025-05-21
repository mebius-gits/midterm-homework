package com.example.myapplication.database

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Utility class for database operations
 */
object DatabaseUtils {
    private val TAG = "DatabaseUtils"
    private val isTransactionInProgress = AtomicBoolean(false)
    
    /**
     * Execute a database operation with safety checks
     * @param operation The database operation to execute
     * @param errorMessage The error message to log if the operation fails
     * @return True if the operation was successful, false otherwise
     */
    suspend fun <T> executeSafeDatabaseOperation(
        operation: suspend () -> T,
        errorMessage: String
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val result = operation()
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "$errorMessage: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Execute a database transaction
     * @param operations The database operations to execute
     * @return True if all operations were successful, false otherwise
     */
    suspend fun executeTransaction(vararg operations: suspend () -> Boolean): Boolean {
        if (isTransactionInProgress.get()) {
            Log.w(TAG, "Transaction already in progress, cannot start a new one")
            return false
        }
        
        isTransactionInProgress.set(true)
        try {
            // Execute all operations
            for (operation in operations) {
                val success = operation()
                if (!success) {
                    Log.e(TAG, "Transaction failed, rolling back")
                    return false
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error in transaction: ${e.message}", e)
            return false
        } finally {
            isTransactionInProgress.set(false)
        }
    }
}
