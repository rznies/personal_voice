package io.livekit.android.example.voiceassistant.utils

import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * PerformanceMonitor - Tracks app performance metrics
 * "Sir, I'm monitoring system performance. Because even genius code needs optimization."
 *
 * Features:
 * - Response time tracking
 * - Memory usage monitoring
 * - API call performance
 * - Frame rate monitoring
 * - Battery impact analysis
 */
class PerformanceMonitor(private val context: Context) {

    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val activeTimers = ConcurrentHashMap<String, Long>()

    private val _metrics = MutableStateFlow(PerformanceSnapshot())
    val metrics: StateFlow<PerformanceSnapshot> = _metrics

    /**
     * Start timing an operation
     */
    fun startTiming(operationName: String) {
        activeTimers[operationName] = SystemClock.elapsedRealtime()
        Timber.v("⏱️ Started timing: $operationName")
    }

    /**
     * End timing an operation and record it
     */
    fun endTiming(operationName: String) {
        val startTime = activeTimers.remove(operationName)
        if (startTime != null) {
            val duration = SystemClock.elapsedRealtime() - startTime
            recordMetric(operationName, duration)
            Timber.v("⏱️ Completed $operationName in ${duration}ms")
        } else {
            Timber.w("Attempted to end timing for $operationName but it wasn't started")
        }
    }

    /**
     * Measure execution time of a block
     */
    inline fun <T> measureTime(operationName: String, block: () -> T): T {
        startTiming(operationName)
        return try {
            block()
        } finally {
            endTiming(operationName)
        }
    }

    /**
     * Record a performance metric
     */
    private fun recordMetric(name: String, duration: Long) {
        val metric = performanceMetrics.getOrPut(name) {
            PerformanceMetric(name)
        }

        metric.recordExecution(duration)
        updateSnapshot()
    }

    /**
     * Get memory usage statistics
     */
    fun getMemoryUsage(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val nativeHeapSize = Debug.getNativeHeapSize()
        val nativeHeapAllocated = Debug.getNativeHeapAllocatedSize()

        return MemoryStats(
            totalMemoryMB = runtime.totalMemory() / (1024 * 1024),
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
            maxMemoryMB = runtime.maxMemory() / (1024 * 1024),
            nativeHeapMB = nativeHeapSize / (1024 * 1024),
            nativeHeapAllocatedMB = nativeHeapAllocated / (1024 * 1024)
        )
    }

    /**
     * Get performance report for a specific operation
     */
    fun getOperationReport(operationName: String): OperationReport? {
        val metric = performanceMetrics[operationName] ?: return null

        return OperationReport(
            operationName = operationName,
            totalExecutions = metric.executionCount,
            averageDurationMs = metric.averageDuration,
            minDurationMs = metric.minDuration,
            maxDurationMs = metric.maxDuration,
            lastDurationMs = metric.lastDuration
        )
    }

    /**
     * Get all performance metrics
     */
    fun getAllMetrics(): Map<String, OperationReport> {
        return performanceMetrics.mapValues { (name, metric) ->
            OperationReport(
                operationName = name,
                totalExecutions = metric.executionCount,
                averageDurationMs = metric.averageDuration,
                minDurationMs = metric.minDuration,
                maxDurationMs = metric.maxDuration,
                lastDurationMs = metric.lastDuration
            )
        }
    }

    /**
     * Get performance summary
     */
    fun getPerformanceSummary(): String {
        val memory = getMemoryUsage()
        val metrics = getAllMetrics()

        return buildString {
            appendLine("📊 **Performance Report**")
            appendLine()
            appendLine("**Memory Usage:**")
            appendLine("• Used: ${memory.usedMemoryMB} MB / ${memory.maxMemoryMB} MB")
            appendLine("• Native Heap: ${memory.nativeHeapAllocatedMB} MB / ${memory.nativeHeapMB} MB")
            appendLine()

            if (metrics.isNotEmpty()) {
                appendLine("**Operation Timings:**")
                metrics.entries.sortedByDescending { it.value.totalExecutions }.take(10).forEach { (name, report) ->
                    appendLine("• $name: ${report.averageDurationMs}ms avg (${report.totalExecutions}x)")
                }
            }

            appendLine()
            appendLine(getPerformanceVerdict(memory, metrics))
        }
    }

    /**
     * Get Jarvis verdict on performance
     */
    private fun getPerformanceVerdict(memory: MemoryStats, metrics: Map<String, OperationReport>): String {
        val memoryUsagePercent = (memory.usedMemoryMB.toFloat() / memory.maxMemoryMB * 100).toInt()
        val slowOperations = metrics.values.count { it.averageDurationMs > 1000 }

        return when {
            memoryUsagePercent > 90 -> "⚠️ Sir, memory usage is at $memoryUsagePercent%. Consider optimization or I might crash on you."
            memoryUsagePercent > 70 -> "Memory at $memoryUsagePercent%, Boss. Approaching Stark Tower levels of occupancy."
            slowOperations > 5 -> "Sir, $slowOperations operations are running slow. Even a genius notices lag."
            slowOperations > 0 -> "$slowOperations slow operations detected. Acceptable, but Tony would optimize."
            else -> "✅ Performance optimal, Sir. Running smoother than Tony's pickup lines."
        }
    }

    /**
     * Update performance snapshot
     */
    private fun updateSnapshot() {
        val memory = getMemoryUsage()
        val operationCount = performanceMetrics.values.sumOf { it.executionCount }
        val averageResponseTime = performanceMetrics.values
            .filter { it.executionCount > 0 }
            .map { it.averageDuration }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0

        _metrics.value = PerformanceSnapshot(
            memoryStats = memory,
            totalOperations = operationCount,
            averageResponseTimeMs = averageResponseTime.toLong(),
            activeTimers = activeTimers.size
        )
    }

    /**
     * Reset all metrics
     */
    fun resetMetrics() {
        performanceMetrics.clear()
        activeTimers.clear()
        updateSnapshot()
        Timber.i("Performance metrics reset")
    }

    /**
     * Log slow operations
     */
    fun logSlowOperations(thresholdMs: Long = 1000) {
        val slowOps = performanceMetrics.filter { it.value.averageDuration > thresholdMs }

        if (slowOps.isNotEmpty()) {
            Timber.w("🐌 Slow operations detected:")
            slowOps.forEach { (name, metric) ->
                Timber.w("   • $name: ${metric.averageDuration}ms avg (threshold: ${thresholdMs}ms)")
            }
        }
    }

    companion object {
        // Operation names
        const val OP_WAKE_WORD_DETECTION = "wake_word_detection"
        const val OP_GEMINI_REQUEST = "gemini_api_request"
        const val OP_TTS_GENERATION = "tts_generation"
        const val OP_VISION_CAPTURE = "vision_capture"
        const val OP_SCREEN_CAPTURE = "screen_capture"
        const val OP_IMAGE_ANALYSIS = "image_analysis"
        const val OP_DEVICE_ACTION = "device_action"
        const val OP_FULL_RESPONSE = "full_response_cycle"

        @Volatile
        private var instance: PerformanceMonitor? = null

        fun getInstance(context: Context): PerformanceMonitor {
            return instance ?: synchronized(this) {
                instance ?: PerformanceMonitor(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Performance metric for a single operation
 */
private class PerformanceMetric(val name: String) {
    var executionCount: Int = 0
        private set
    var totalDuration: Long = 0
        private set
    var minDuration: Long = Long.MAX_VALUE
        private set
    var maxDuration: Long = 0
        private set
    var lastDuration: Long = 0
        private set

    val averageDuration: Long
        get() = if (executionCount > 0) totalDuration / executionCount else 0

    fun recordExecution(duration: Long) {
        executionCount++
        totalDuration += duration
        lastDuration = duration
        minDuration = minOf(minDuration, duration)
        maxDuration = maxOf(maxDuration, duration)
    }
}

/**
 * Memory usage statistics
 */
data class MemoryStats(
    val totalMemoryMB: Long,
    val usedMemoryMB: Long,
    val maxMemoryMB: Long,
    val nativeHeapMB: Long,
    val nativeHeapAllocatedMB: Long
) {
    val usagePercentage: Int
        get() = ((usedMemoryMB.toFloat() / maxMemoryMB) * 100).toInt()
}

/**
 * Operation performance report
 */
data class OperationReport(
    val operationName: String,
    val totalExecutions: Int,
    val averageDurationMs: Long,
    val minDurationMs: Long,
    val maxDurationMs: Long,
    val lastDurationMs: Long
)

/**
 * Performance snapshot
 */
data class PerformanceSnapshot(
    val memoryStats: MemoryStats = MemoryStats(0, 0, 0, 0, 0),
    val totalOperations: Int = 0,
    val averageResponseTimeMs: Long = 0,
    val activeTimers: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
