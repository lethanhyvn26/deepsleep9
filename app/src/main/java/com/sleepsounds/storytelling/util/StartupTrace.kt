package com.sleepsounds.storytelling.util

import android.os.Process
import android.os.SystemClock
import com.sleepsounds.storytelling.BuildConfig

/**
 * Lightweight debug-only startup timing tracer anchored to process creation time.
 */
object StartupTrace {
    private val processStartElapsedMs: Long = Process.getStartElapsedRealtime()

    fun mark(label: String) {
        if (!BuildConfig.DEBUG) return
        val deltaMs = SystemClock.elapsedRealtime() - processStartElapsedMs
        R2Log.d("Startup +${deltaMs}ms: $label [thread=${Thread.currentThread().name}]")
    }

    inline fun <T> span(label: String, block: () -> T): T {
        if (!BuildConfig.DEBUG) return block()
        mark("$label (start)")
        val t0 = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val durationMs = SystemClock.elapsedRealtime() - t0
            mark("$label (completed in ${durationMs}ms)")
        }
    }
}
