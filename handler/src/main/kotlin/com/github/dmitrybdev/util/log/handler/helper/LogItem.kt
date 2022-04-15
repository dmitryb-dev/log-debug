package com.github.dmitrybdev.util.log.handler.helper

import org.aspectj.lang.ProceedingJoinPoint
import java.time.Duration
import java.time.Instant

data class LogItem(
    private val joinPoint: ProceedingJoinPoint,
    val isTrace: Boolean,
    val level: Int,
) {
    private val startTime = Instant.now()
    private lateinit var endTime: Instant

    private var result: Any? = null
    private var throwable: Throwable? = null

    private val takenTime: Duration
        get() = Duration.between(startTime, endTime)

    fun afterExecution(result: Any?, throwable: Throwable?) {
        this.endTime = Instant.now()
        this.result = result
        this.throwable = throwable

        currentLevel.set(currentLevel.get() - 1)
    }

    val logText: String
        get() {
            val topLevelMarkerLogText = if (level == 0) "*".yellow() else " "
            val levelLogText = "-" + LogTextProviders.align(level.toString(), 2, false) + "-"

            return LogTextProviders.timeLogText(startTime) +
                    " " + topLevelMarkerLogText +
                    " " + LogTextProviders.takenTimeLogText(takenTime) +
                    " " + levelLogText +
                    " " + LogTextProviders.joinPointLogText(joinPoint, result, throwable, isTrace)
        }

    companion object {
        private val currentLevel = ThreadLocal.withInitial { 0 }

        fun beforeExecution(joinPoint: ProceedingJoinPoint, isTrace: Boolean): LogItem {
            val currentLevel = currentLevel.get()
            Companion.currentLevel.set(Companion.currentLevel.get() + 1)
            return LogItem(joinPoint, isTrace, currentLevel)
        }
    }
}
