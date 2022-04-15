package com.github.dmitrybdev.util.log.handler.helper

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.*

object LogTextProviders {
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS")

    fun joinPointLogText(joinPoint: ProceedingJoinPoint, result: Any?, th: Throwable?, isTrace: Boolean,): String {
        fun returnTypeLogText(result: Any?, th: Throwable?, method: Method) = when {
            th != null -> "! " + ThrowableLogTextProvider.logText(th, isTrace)
            method.returnType == Void.TYPE || !ParamVisibilityHelper.isReturnTypeVisible(isTrace, method) -> ""
            else -> "= " + LogTextProvider.objectLogText(result, isTrace)
        }
        fun paramsLogText(
            joinPoint: ProceedingJoinPoint,
            method: Method
        ) = (joinPoint.signature as MethodSignature).method.parameters
            .zip(joinPoint.args)
            .filter { (param, _) -> !param.name.startsWith("this$") }
            .filter { (param, _) -> ParamVisibilityHelper.isVisible(isTrace, param, method) }
            .mapIndexed { i, (param, value) -> (param.name ?: "\$$i") to value }
            .joinToString(", ") { (paramName, value) ->
                paramName.cyan() + "=" + LogTextProvider.objectLogText(value, isTrace)
            }

        fun thisLogText(joinPoint: ProceedingJoinPoint, method: Method): String {
            fun firstThisArg() = joinPoint.args.firstOrNull()
                ?.takeIf { method.parameters.first().name.startsWith("this$") }

            val thisValue = joinPoint.`this` ?: firstThisArg()
            return thisValue?.let { LogTextProvider.objectLogText(it, isTrace) } ?: ""
        }

        val method = (joinPoint.signature as MethodSignature).method

        return thisLogText(joinPoint, method) +
                "." + joinPoint.signature.name.blue() +
                "(" + paramsLogText(joinPoint, method) + ")" +
                " " + returnTypeLogText(result, th, method)
    }

    fun takenTimeLogText(takenTime: Duration) =
        "[" +
        when {
            takenTime.toMillis() <= 999 -> {
                val takenTimeMillis = takenTime.toMillis()
                val millisNumberLogText = align(takenTimeMillis.toString(), 3)
                val coloredMillisNumberLogText = when {
                    takenTimeMillis < 5 -> millisNumberLogText
                    takenTimeMillis < 10 -> millisNumberLogText.green()
                    takenTimeMillis < 25 -> millisNumberLogText.yellow()
                    else -> millisNumberLogText.red()
                }
                coloredMillisNumberLogText + "ms"
            }
            else -> align(takenTime.seconds.toString(), 4, false).red() + "s"
        } +
        "]"

    fun timeLogText(time: Instant): String = timeFormatter.format(Date.from(time))

    fun align(str: String, length: Int, cut: Boolean = true) = when {
        str.length < length -> " ".repeat(length - str.length) + str
        str.length == length || !cut -> str
        else -> str.substring(0, length - 2) + ".."
    }
}