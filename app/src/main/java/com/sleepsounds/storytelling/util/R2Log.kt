package com.sleepsounds.storytelling.util

import android.util.Log

/**
 * Diagnostic logger for Cloudflare R2, Firebase RTDB, and local catalog operations.
 */
object R2Log {
    private const val LOG_TAG = "DeepSleepCDN"

    private fun formatTagMessage(scope: String?, text: String): String =
        if (scope.isNullOrBlank()) text else "[$scope] $text"

    fun i(message: String) {
        Log.i(LOG_TAG, message)
    }

    fun i(section: String, message: String) {
        Log.i(LOG_TAG, formatTagMessage(section, message))
    }

    fun d(message: String) {
        Log.d(LOG_TAG, message)
    }

    fun d(section: String, message: String) {
        Log.d(LOG_TAG, formatTagMessage(section, message))
    }

    fun w(message: String, cause: Throwable? = null) {
        if (cause != null) {
            Log.w(LOG_TAG, message, cause)
        } else {
            Log.w(LOG_TAG, message)
        }
    }

    fun w(section: String, message: String, cause: Throwable? = null) {
        w(formatTagMessage(section, message), cause)
    }

    fun e(message: String, cause: Throwable? = null) {
        if (cause != null) {
            Log.e(LOG_TAG, message, cause)
        } else {
            Log.e(LOG_TAG, message)
        }
    }

    fun e(section: String, message: String, cause: Throwable? = null) {
        e(formatTagMessage(section, message), cause)
    }
}
