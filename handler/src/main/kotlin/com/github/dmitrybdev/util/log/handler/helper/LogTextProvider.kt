package com.github.dmitrybdev.util.log.handler.helper

import com.github.dmitrybdev.util.log.LogDebug
import com.github.dmitrybdev.util.log.LogDebugHide
import com.github.dmitrybdev.util.log.LogTrace
import com.github.dmitrybdev.util.log.LogTraceHide
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

sealed interface LogTextProvider {
    fun logText(value: Any, isTrace: Boolean): String?

    companion object {
        private val providers = arrayOf(
            SimpleTypeLogTextProvider,
            StringLogTextProvider,
            ThrowableLogTextProvider,
            MapLogTextProvider,
            CollectionLogTextProvider,
            ObjectLogTextProvider,
        )

        fun objectLogText(value: Any?, isTrace: Boolean): String = when (value) {
            null -> "null"
            Unit -> ""
            else -> providers.firstNotNullOfOrNull { it.logText(value, isTrace) } ?: value.toString().yellow()
        }
    }
}

object SimpleTypeLogTextProvider : LogTextProvider {
    override fun logText(value: Any, isTrace: Boolean): String? = value.takeIf {
        it is Number || it is Boolean
    }?.toString()?.yellow()
}

object StringLogTextProvider : LogTextProvider {
    override fun logText(value: Any, isTrace: Boolean): String? = (value as? String)?.let {
        "\"$it\"".green()
    }
}

object MapLogTextProvider : LogTextProvider {
    override fun logText(value: Any, isTrace: Boolean): String? = (value as? Map<*, *>)?.let {
        val limit = if (isTrace) 10 else 2
        val tooLong = it.size > limit
        val tooLongMarker = if (tooLong) {
            (", .." + (limit - it.size)).red()
        } else {
            ""
        }

        val entriesLogText = it.entries.take(limit).joinToString(", ", "{", "}") { (key, value) ->
            LogTextProvider.objectLogText(key, isTrace) + " -> " + LogTextProvider.objectLogText(value, isTrace)
        }

        return entriesLogText + tooLongMarker
    }
}

object CollectionLogTextProvider : LogTextProvider {
    override fun logText(value: Any, isTrace: Boolean): String? = (value as? Collection<*>)?.let {
        val limit = if (isTrace) 10 else 2
        val tooLong = it.size > limit
        val tooLongMarker = if (tooLong) {
            (", .." + (limit - it.size)).red()
        } else {
            ""
        }

        val itemsLogText = it.joinToString(", ", "[", "]") { item ->
            LogTextProvider.objectLogText(item, isTrace)
        }

        return itemsLogText + tooLongMarker
    }
}

object ThrowableLogTextProvider : LogTextProvider {
    override fun logText(value: Any, isTrace: Boolean): String? = (value as? Throwable)?.let { originTh ->
        generateSequence(originTh) { th ->
            th.cause.takeIf { th != th.cause }
        }.joinToString(" -> ") { th ->
            ("" + th::class.simpleName + "(").red() +
                    (th.message?.let { StringLogTextProvider.logText(it, isTrace) } ?: "") +
                    ")".red()
        }
    }
}

object ObjectLogTextProvider : LogTextProvider {
    override fun logText(value: Any, isTrace: Boolean): String? = value.takeIf {
        value::class.annotations.any { it is LogDebug || it is LogDebugHide || it is LogTrace || it is LogTraceHide }
    }?.let {
        value::class.memberProperties
            .filter { member ->
                ParamVisibilityHelper.isVisible(
                    isTrace,
                    member,
                    value::class.primaryConstructor?.findParameterByName(member.name),
                    value::class
                )
            }.joinToString(
                ", ",
                value::class.simpleName?.yellow() + "(",
                ")"
            ) {
                it.name.yellow() + "=" + LogTextProvider.objectLogText(it.getter.call(value), isTrace)
            }
    }
}