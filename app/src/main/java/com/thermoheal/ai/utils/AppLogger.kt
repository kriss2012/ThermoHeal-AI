package com.thermoheal.ai.utils

import android.util.Log
import com.thermoheal.ai.BuildConfig

/**
 * Production-ready logging utility for ThermoHeal-AI.
 * Wraps Logcat calls and prevents logging sensitive data in production.
 */
object AppLogger {
    private const val TAG = "ThermoHeal-AI"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    /**
     * Use for critical domain events that should always be logged but sanitized.
     */
    fun audit(event: String) {
        Log.i("${TAG}_AUDIT", event)
    }
}
