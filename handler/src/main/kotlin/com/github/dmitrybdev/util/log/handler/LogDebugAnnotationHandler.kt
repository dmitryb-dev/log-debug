package com.github.dmitrybdev.util.log.handler

import com.github.dmitrybdev.util.log.handler.helper.LogItem
import org.aspectj.lang.ProceedingJoinPoint
import org.slf4j.LoggerFactory

object LogDebugAnnotationHandler {
    private val logger = LoggerFactory.getLogger("LogDebug")

    private val logsChain = ThreadLocal.withInitial { mutableListOf<LogItem>() }
    private val disablePrintEnabled = ThreadLocal.withInitial { false }

    fun log(thisJoinPoint: ProceedingJoinPoint, isTrace: Boolean): Any? {
        fun logChain() {
            try {
                for (log in logsChain.get()) {
                    if (log.isTrace) {
                        logger.trace(log.logText)
                    } else {
                        logger.debug(log.logText)
                    }
                }
            } finally {
                logsChain.get().clear()
            }
        }

        if (!logger.isDebugEnabled || (isTrace && !logger.isTraceEnabled)) {
            return thisJoinPoint.proceed()
        }

        val currentLog = LogItem.beforeExecution(thisJoinPoint, isTrace)
        logsChain.get().add(currentLog)

        try {
            val result = thisJoinPoint.proceed()
            currentLog.afterExecution(result, null)
            return result
        } catch (th: Throwable) {
            currentLog.afterExecution(null, th)
            throw th
        } finally {
            if (currentLog.level == 0) {
                logChain()
            }
        }
    }

    fun disableLogPrint(block: () -> Any?): Any? = try {
        disablePrintEnabled.set(true)
        block()
    } finally {
        disablePrintEnabled.set(false)
    }
}